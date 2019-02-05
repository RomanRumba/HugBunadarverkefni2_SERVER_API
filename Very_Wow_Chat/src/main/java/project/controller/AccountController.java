package project.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import project.errors.HttpException;
import project.errors.NotFoundException;
import project.payloads.HttpResponseBody;
import project.payloads.JwtUser;
import project.payloads.PasswordResetRequest;
import project.payloads.UserRegistrationFormReceiver;
import project.persistance.entities.User;
import project.pojo.Mailer;
import project.security.JwtGenerator;
import project.services.AuthenticationService;
import project.services.CryptographyService;
import project.services.TemporaryUserStorageService;
import project.services.UserService;

/**
 * This controller handles requests for registration, login, and the reset of
 * password
 * 
 * @author Róman(ror9@hi.is) and Davíð
 */
@RestController
public class AccountController {

	/**
	 * Temporary storage database, holds the users that still have to be validated,
	 * before being added into the long term storage
	 */
	@Autowired
	private TemporaryUserStorageService temporaryUserStorageService;

	/**
	 * Our long term storage database used to store our user after he has been
	 * validated
	 */
	@Autowired
	private UserService userService;

	/**
	 * This class holds over all the basic functions needed to authenticate or
	 * validate data received from user
	 */
	@Autowired
	private AuthenticationService authenticator;

	/**
	 * Used for creating JTW if the authentication is successful.
	 * */
	@Autowired
	private JwtGenerator jwtGenerator;
    
	/**
	 * Fetch environmental constants from application.properties
	 * the Value annotation sets the variable with the desired constant from 
	 * application.properties file 
	 * */
	/**
	 * Fetch the email server url
	 * */
	@Value("${email.server.url}")
	private String emailServerUrl;

	/**
	 * Since our email server is live to everyone, we add a secret key
	 * to authenticate ourself so only people who have the key can send emails
	 * we dont want random people using our server to spamm others in our name
	 *  */
	@Value("${email.server.secretkey}")
	private String emailServerSecretKey;

	/**
	 * just a link that will be displayed inside the email when you open the link.
	 * We have 3 different servers all running locally and on different ports 
	 * this is just to prevent those annoying times when u register a new user open 
	 * a link and its a different port from what you are using */
	@Value("${email.server.serverRunningOn}")
	private String serverRunningOn;
	
	// -------------- Register --------------

	/**
	 * Usage: url/register
	 * 
	 * NOTE: POST request should contain a JSON object of the form
	 * 
	 * <pre>
	 * {
	 * 	   "userName": "john",
	 * 	   "displayName": "gladwell",
	 *     "password": "AveryLong$ecurePassword123",
	 *     "passwordReap": "AveryLong$ecurePassword123",
	 *     "email": "john.gladwell@gmail.com" 
	 * }
	 * </pre>
	 * 
	 * Validates the client's POST request and responds with an appropriate status
	 * code along with the data.
	 * 
	 * 
	 * @param payload User registration form.
	 * 
	 * @return Empty response.
	 * 
	 * @throws Exception
	 */
	@RequestMapping(value = "/register", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<String> register(@RequestBody UserRegistrationFormReceiver payload) throws Exception {
		/*
		 * Since this is a restfull controller, we have our own custom way to respond to
		 * the user so our responses would be uniformed, this will make it easier to
		 * work with in the client side
		 */
		HttpResponseBody clientResponse = new HttpResponseBody();

		/*
		 * This is for debuggin, i had problems sometimes when i used Postman where it
		 * would not send the correct json to the server resulting in wierd errors. Its
		 * fine now but we might as well keep it
		 */
		if (!payload.allInfoExists()) {
			/*
			 * reason why this is a single Error not a list of errors is cuz this should
			 * never happen heroku should catch this and stop it from sending an empty
			 * resource this is more for catching with testing proposes like postman and
			 * stuff
			 */
			clientResponse.addSingleError("error", "All information must be filled");
			return new ResponseEntity<>(clientResponse.getErrorResponse(), HttpStatus.BAD_REQUEST);
		}

		/*
		 * After debuggin the project we found out u can have spaces and other things in
		 * your name we dont want that so we prevent spaces in username and Displayname
		 * and special characters
		 */
		if (!authenticator.noSymbolsCheck(payload.getUserName())) {
			clientResponse.addErrorForForm("Username", "Cannot contain spaces or special characters");
			return new ResponseEntity<>(clientResponse.getErrorResponse(), HttpStatus.BAD_REQUEST);
		}

		/*
		 * before starting authenticating we check if the client can have this username
		 * i decided if the username is taken there is no reason to validate rest of the
		 * data
		 */
		if (authenticator.userNameExists(payload.getUserName())) {
			clientResponse.addErrorForForm("Username", "Username already exists");
			return new ResponseEntity<>(clientResponse.getErrorResponse(), HttpStatus.BAD_REQUEST);
		}

		/*
		 * After debuggin the project we found out u can have spaces and other things in
		 * your name we dont want that so we prevent spaces in username and Displayname
		 * and special characters
		 */
		if (!authenticator.noSymbolsCheck(payload.getDisplayName())) {
			clientResponse.addErrorForForm("DisplayName", "Cannot contain spaces or special characters");
		}

		/*
		 * now that we know we have all the data we need and we know the username is
		 * legit we can start validating the data We start by calling the validator to
		 * validate the information and if there is a problem we take note of it
		 */
		if (!authenticator.passwordsMach(payload.getPassword(), payload.getPasswordReap())) {
			clientResponse.addErrorForForm("Password", "Both passwords must match");
		}
		/*
		 * We can split this into individual errors but that would just make the code
		 * bigger and messier also for the validator same thing would be applied. so
		 * insted we have one big password strenght check and if it fails we send a list
		 * of things that have to be corret.
		 */
		if (!authenticator.validatePass(payload.getPassword())) {
			clientResponse.addErrorForForm("Password", "Must be atleast 8 characters long");
			clientResponse.addErrorForForm("Password", "Must contain a upper and a lowercase letter");
			clientResponse.addErrorForForm("Password", "Must contain a specialcase letter");
			clientResponse.addErrorForForm("Password", "Cannot contain spaces or tabs");
		}
		if (!authenticator.validEmail(payload.getEmail())) {
			clientResponse.addErrorForForm("Email", "Email must be of valid form");
		}

		// if errors exists then we return the errors as the response
		if (clientResponse.errorsExist()) {
			return new ResponseEntity<>(clientResponse.getErrorResponse(), HttpStatus.BAD_REQUEST);
		}

		/*
		 * At this point we know that the user data is valid so we proceed creating the
		 * user in our short term storage and sending him a validation link. Bcryptor
		 * for hashing sensitive data
		 */
		BCryptPasswordEncoder privateInfoEncoder = new BCryptPasswordEncoder();

		/*
		 * create a json obj that we can store in the short term database and add all
		 * the information to it along with encrypting the private data
		 */
		JSONObject newUser = new JSONObject();
		newUser.put("userName", payload.getUserName());
		newUser.put("displayName", payload.getDisplayName());
		// NOTE: password is hashed here.
		newUser.put("password", privateInfoEncoder.encode(payload.getPassword()));
		/*
		 * the reason why we encode email is cuz of the new privacy policies any data
		 * that can lead to the user(as a person) has to be secured
		 */

		// NOTE: email is encrypted here.
		newUser.put("email", CryptographyService.getCiphertext(payload.getEmail()));

		/*
		 * Please NOTE hashing and encrypting are not the samething, in short if you
		 * hash you can never get the raw data you can compare and see if it matches
		 * encrypted data can be decrypted to see the its raw form please be carefull
		 * when choosing how to hide sensitive information
		 */

		/*
		 * Store the data in the short term storage, in the short term storage its
		 * defined how long is the period of time its stored until its deleted
		 */
		this.temporaryUserStorageService.insertUser(payload.getUserName(), newUser);

		String key = payload.getUserName();
		String recipientEmail = payload.getEmail();
		String emailContent = "Welcome to VeryWowChat!!! \nbefore you can login please validate your account here : "
				+ this.serverRunningOn+"validation/" + key;
		// the mailer who will call the webServer to send a validation email
		Mailer mailer = new Mailer(recipientEmail, emailContent, emailServerUrl, emailServerSecretKey);
		mailer.send(); // send the email to the user

		// we responde with that the register was successful and dont send any content
		// back
		return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
	}

	/**
	 * Usage: url/validation/{key}
	 * 
	 * <pre>
	 *  Usage : url/validation/{key} 
	 *    For : PUT request key is a String pointer to the data that needs to be validated dosent need to contain any type of json 
	 *   After: checks if the key is points to a unvalidated user, if so it stores the user in neo4j and sets its status as validated
	 * </pre>
	 * 
	 * @param key URL path fragment
	 * 
	 * @return No response.
	 */
	@RequestMapping(path = "/validation/{key}", method = RequestMethod.PUT, headers = "Accept=application/json")
	public ResponseEntity<String> validateUser(@PathVariable String key) {
		// the explanation for this is above in the login controller line : 74
		HttpResponseBody clientResponse = new HttpResponseBody();

		/* We check if the key exists in our short term storage */
		if (!this.temporaryUserStorageService.userNameExists(key)) {
			clientResponse.addSingleError("error",
					"User not found or validation period has expired please register again");
			return new ResponseEntity<>(clientResponse.getErrorResponse(), HttpStatus.NOT_FOUND);
		}

		// NOTE that we know the data exists we fetch it and move it into long term
		/*
		 * NOTE: we assume the email of this JSON object is encrypted (from registration
		 * everything that needs to be hashed/encrypted should be hashed/encrypted).
		 */
		JSONObject tempUrs = this.temporaryUserStorageService.getAndDestroyData(key); // fetch the data and remove the data from
																		// shortterm storage

		// create a new User that will be insert into our long term storage
		User newuser = new User(tempUrs.getString("userName"), tempUrs.getString("password"),
				tempUrs.getString("displayName"), tempUrs.getString("email"));

		// create the user in our long term database
		try {
			this.userService.createUser(newuser);
		} catch (HttpException e) {
			clientResponse.addSingleError("error", e.getMessage());
			return new ResponseEntity<>(clientResponse.getErrorResponse(), HttpStatus.BAD_REQUEST);
		}

		// we responde with that the validation was successful and dont send any content
		// back
		return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
	}

	// -------------- Login --------------

	/**
	 * POST login request on host/login.
	 * 
	 * <pre>
	 * { "userName": "yourNameHere", "password": "yourPasswordHere" }
	 * </pre>
	 * 
	 * @param payload JWT user
	 * 
	 * @return JSON object with user details and token.
	 * 
	 * @throws Exception
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<String> login(@RequestBody JwtUser payload) throws Exception {
		/*
		 * Since this is a restfull controller, we have our own custom way to respond to
		 * the user so our responses would be uniformed, this will make it easier to
		 * work with in the client side
		 */
		HttpResponseBody clientResponse = new HttpResponseBody();

		/*
		 * The initial idea was, when the controllers are called, they call different
		 * services to Fulfill the request that was asked of them, the services return
		 * either errors or data that was requested the controller collects these errors
		 * from all the services and makes a response out of them or sends the data if
		 * the request was successful
		 */

		/*
		 * check if the user exists in the database if dosen't exists then we respond
		 * with error along with 404(not found) HTTP response
		 */
		if (!this.userService.userExistsAndActive(payload.getUserName())) {
			// create a error
			clientResponse.addErrorForForm("Username", "Username not found");
			// return the error
			return new ResponseEntity<>(clientResponse.getErrorResponse(), HttpStatus.NOT_FOUND);
		}

		// at this point we know the user exists we fetch him and then we need to
		// validate the password
		User fetchedUsr = this.userService.findByUsername(payload.getUserName());
		/*
		 * the password is hashed in the database so we need a way to authenticate and
		 * confirmed that the given password from the client is correct.
		 */
		BCryptPasswordEncoder privateInfoEncoder = new BCryptPasswordEncoder();
		// fetch the raw password from the client input
		CharSequence raw_password = payload.getPassword();

		/*
		 * check if password matches the requested login, if the password dosent match
		 * we create an error and responde with it
		 */
		if (!privateInfoEncoder.matches(raw_password, fetchedUsr.getPassword())) {
			clientResponse.addErrorForForm("Password", "Password does not match the username");
			return new ResponseEntity<>(clientResponse.getErrorResponse(), HttpStatus.UNAUTHORIZED);
		}

		/*
		 * create user and jtw to store in session storage This is the obj that will be
		 * stored in the users session storage for now we only need to store his
		 * displayname, username and the JTW for authentication, but later if we need to
		 * store something more its just a couple of adds here and it will work the same
		 */
		JSONObject sessionUsr = new JSONObject();
		sessionUsr.put("username", fetchedUsr.getUsername());
		sessionUsr.put("displayname", fetchedUsr.getDisplayName());
		sessionUsr.put("token", "Token " + this.jwtGenerator.generate(payload));

		clientResponse.addSingleSucc(sessionUsr);// ad the json obj to the response body
		// send user and JTW token back as a succesful response
		return new ResponseEntity<>(clientResponse.getSuccessResponse(), HttpStatus.OK);
	}

	// -------------- Password Reset--------------

	/**
	 * Resets password. Sends email to the username.
	 * 
	 * @param prr JSON mapped to PasswordResetRequest.
	 * 
	 * @return NO CONTENT HTTP response
	 */
	@RequestMapping(value = "/password_reset_request/{username}", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Object> passwordReset(@PathVariable String username) {
		try {
			User user = userService.findByUsername(username);
			// NOTE: email of user is assumed to be encrypted so it needs to be decrypted.
			String recipientEmail = CryptographyService.getPlaintext(user.getEmail());
			String randomKey = CryptographyService.getRandomHexString(64);
			temporaryUserStorageService.insertString(randomKey, username);
			String resetUrl = emailServerUrl + "password_reset/" + randomKey;
			String emailContent = "Reset URL: " + resetUrl;
			Mailer mailer = new Mailer(recipientEmail, emailContent, emailServerUrl, emailServerSecretKey);
			mailer.send();
			return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
		} catch (NotFoundException e) {
			return e.getErrorResponseEntity();
		}
	}

	/**
	 * Complete password reset, returns a randomly generated password.
	 * 
	 * @param key path segment of URL.
	 * 
	 * @return Randomly generated password.
	 */
	@RequestMapping(value = "/password_reset/{key}", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Object> passwordResetComplete(@PathVariable String key) {
		try {
			if (!temporaryUserStorageService.userNameExists(key)) {
				return new ResponseEntity<>("not found", HttpStatus.NOT_FOUND);
			}
			String username = temporaryUserStorageService.getAndDestroyString(key);
			User user = userService.findByUsername(username);
			String password = CryptographyService.getStrongRandomPassword(20);
			// Update existing user.
			userService.updateUser(user, null, null, password);
			// Create response.
			JSONObject obj = new JSONObject();
			obj.put("password", password);
			return new ResponseEntity<>(obj.toString(), HttpStatus.OK);
		} catch (NotFoundException e) {
			return e.getErrorResponseEntity();
		}
	}

}
