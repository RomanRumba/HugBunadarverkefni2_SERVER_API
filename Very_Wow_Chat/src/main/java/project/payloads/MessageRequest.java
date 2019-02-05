package project.payloads;

import project.services.CryptographyService;

/**
 * Messages that are sent from the client to the server are encapsulated by this
 * class.
 * 
 * NOTE: messages that are inserted into this container are assumed to be in
 * plaintext, and are encrypted so they eventually store ciphertext messages.
 * 
 * @author Davíð Helgason (dah38@hi.is)
 */
public class MessageRequest {

	private String message;

	/**
	 * Returns the message encrypted.
	 * 
	 * @return Encrypted message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Encrypts and stores the message.
	 * 
	 * @param message Unencrypted message.
	 */
	public void setMessage(String message) {
		// NOTE: encrypt message
		this.message = CryptographyService.getCiphertext(message);
	}

	public MessageRequest() {
	}

	/**
	 * Stores the message encrypted.
	 * 
	 * @param message Unencrypted message.
	 */
	public MessageRequest(String message) {
		// NOTE: encrypt message
		this.message = CryptographyService.getCiphertext(message);
	}
}
