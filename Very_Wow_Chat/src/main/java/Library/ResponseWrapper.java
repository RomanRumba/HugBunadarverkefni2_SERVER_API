package Library;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 *
 */
public class ResponseWrapper {

	/**
	 * 
	 * @param x
	 * @return
	 */
	public static Object wrap(Object x) {
		Map<String, Object> wrapper = new HashMap<>();
		wrapper.put("GoodResp", x);
		return wrapper;
	}

	/**
	 * 
	 * @param x
	 * @return
	 */
	public static Object badWrap(Object x) {
		Map<String, Object> wrapper = new HashMap<>();
		wrapper.put("BadResp", x);
		return wrapper;
	}
}
