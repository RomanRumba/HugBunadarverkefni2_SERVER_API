package project.services;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * class that handles Authentication and encryption of different types of data
 * @author Róman(ror9@hi.is)
 */
@Service
public class AuthenticationService {

	// Our short term storage
	@Autowired
	private TemporaryUserStorageService redisService;

	// Our long term storage
	@Autowired
	private UserService userService;

	/**
	 * Usage : auth.userNameExists(userName) For : auth is AuthenticationService
	 * userName is a String After: returns true if the userName exists in ether
	 * neo4j or redis
	 */
	public boolean userNameExists(String userName) {
		/*
		 * Note that the user could been created temoporary in redis and we want to take
		 * that in account or else you could start having douplicate users since Neo4j
		 * dosent really care for douplicate nodes
		 */
		boolean redisRes = this.redisService.userNameExists(userName);
		boolean neo4Res = this.userService.usernameTaken(userName);
		return redisRes || neo4Res; // one of these has to be true for us to abort
	}

	/**
	 * Usage : auth.passwordsMach(p1,p2) For : auth is AuthenticationService p1,p2
	 * Strings After: returns if p1 and p2 are identical
	 */
	public boolean passwordsMach(String p1, String p2) {
		return p1.equals(p2);
	}

	/**
	 * Usage : auth.validEmail(email) For : auth is AuthenticationService email is a
	 * String that is of a form xxx@xxx.com/org/... After: returns if the email is
	 * of valid form
	 */
	public boolean validEmail(String email) {
		// since i am lazy and i dont want to write regex i will have a class
		// that will do this for me
		EmailValidator eValidator = EmailValidator.getInstance(true);
		return eValidator.isValid(email);
	}

	/**
	 * Usage : auth.validatePass(pass) From : auth is AuthenticationService password
	 * is a string After: checks if the password fullils certain requirements
	 */
	public boolean validatePass(String password) {
		/*
		 * Regex explained for those who want to fresh up ^ = indicates start of string
		 * (?=.*[0-9]) = a digit must occur at least once in the pattern (?=.*[a-z]) = a
		 * lower case letter must occur at least once in the pattern (?=.*[A-Z]) = a
		 * upper case letter must occur at least once in the pattern (?=.*[@#$%^&+=]) =
		 * a special character must occur at least once in the pattern (tinfoil hats on)
		 * (?=\S+$) = no whitespace allowed in the pattern .{8,} = anything, at least
		 * eight letters long $ = indicates end of string
		 */
		String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
		return password.matches(pattern);
	}

	/**
	 * Usage : auth.NoSymbolsCheck(name) For : auth is AuthenticationService name is
	 * a String After: returns true if name fulfills the regex requirements
	 * 
	 * @param name
	 */
	public boolean noSymbolsCheck(String name) {
		/*
		 * I know there is a package for this but i fck love writing regex its so fun.
		 * its much harder to define what is not allowed rathen then what is allowed so
		 * we do just that we define what is allowed
		 */
		String pattern = "^[A-Za-z0-9ÁáÉéðÐþÞæÆóÓöÖ]+$";
		return name.matches(pattern);
	}

}
