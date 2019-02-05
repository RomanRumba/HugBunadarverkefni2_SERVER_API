package project.services;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import project.persistance.repositories.TemporaryUserStorageRepository;

/**
 * This class uses the Redis repository to fulfill all Redis based requests by
 * our application
 * 
 * @author Róman(ror9@hi.is)
 */
@Service
public class TemporaryUserStorageService {

	@Autowired
	private TemporaryUserStorageRepository redisRepository;

	/**
	 * Checks if user name <code>username</code> exists in Redis database.
	 * 
	 * @param username
	 * @return <code>True</code> if user name exists in Redis database, otherwise
	 *         <code>False</code>
	 */
	public boolean userNameExists(String username) {
		return this.redisRepository.checkIfKeyExists(username);
	}

	/**
	 * Inserts JSON object (which will be stringified) into Redis ad key
	 * <code>key</code>.
	 * 
	 * NOTE: Assumes <code>data</code> is user details (but doesn't actually have to
	 * be).
	 * 
	 * NOTE: Assumes the entry exists.
	 * 
	 * @param key  key of entry
	 * @param data JSON object to stringify and insert.
	 */
	public String insertUser(String key, JSONObject data) {
		this.redisRepository.insertData(key, data.toString());
		return key;
	}

	/**
	 * Retrieves the stringified JSON entry with key <code>key</code> and deletes
	 * the entry from the database.
	 * 
	 * NOTE: assumes the entry is stringified JSON.
	 * 
	 * @param key of entry.
	 */
	public JSONObject getAndDestroyData(String key) {
		JSONObject data = this.redisRepository.getData(key);
		this.redisRepository.destroyData(key);
		return data;
	}

	/**
	 * Inserts string entry with key <code>key</code>
	 * 
	 * @param key    The key of entry.
	 * @param string The string to insert.
	 */
	public void insertString(String key, String string) {
		redisRepository.insertString(key, string);
	}

	/**
	 * Retrieves string entry with key <code>key</code>.
	 * 
	 * @param key Key of entry.
	 * 
	 * @return The entry itself, which is a string. (or maybe it crashes or
	 *         something...)
	 */
	public String getString(String key) {
		return redisRepository.getString(key);
	}

	/**
	 * Retrieves string entry with key <code>key</code> and deletes the entry
	 * from Redis.
	 * 
	 * @param key Key of entry.
	 * 
	 * @return String entry.
	 */
	public String getAndDestroyString(String key) {
		String string = redisRepository.getString(key);
		redisRepository.destroyData(key);
		return string;
	}
}
