package project.payloads;

import java.util.ArrayList;

import org.json.JSONObject;

/**
 * This is the HTTP response format we will use i.e many Controllers can return
 * many different things and its important to make some kind of unified way of
 * returning our results to the client. This will create less problems when we
 * will implement the api-communicator in the webApp
 * 
 * @author Róman(ror9@hi.is)
 */
public class HttpResponseBody {

	/**
	 * We want to have some kind of way to denote right away if the response is a
	 * successful or not when u get a response it will bed wrapped in BadResp or
	 * GoodResp this way in the API-communicator this style along with HTTP status
	 * codes gives us a very good way of handling responses late down the line
	 */
	private JSONObject errors_response;

	/**
	 * A response can consist of many thing this Array can store many individual
	 * things so maybe if you only want to send one response it is possible
	 */
	private ArrayList<JSONObject> error; // container for errors
	private ArrayList<JSONObject> response; // container for successes

	/**
	 * Sometimes you want to return an object that has a list of objects that denote
	 * different things, this is also possible !
	 */
	private JSONObject many_in_one_erros; // container for errors
	private JSONObject many_in_one_succ; // container for successes

	public HttpResponseBody() {
		this.errors_response = new JSONObject();
		this.error = new ArrayList<JSONObject>();
		this.response = new ArrayList<JSONObject>();
		this.many_in_one_erros = new JSONObject();
		this.many_in_one_succ = new JSONObject();
	}
	
	/**
	 * Usage : addErrorForForm(FieldVal,Message) 
	 *   For : FieldVal - String input field type (username , password ,email etc..) 
	 *         Message - String error msg for the field 
	 *  After: creates a errors obj that holds many error msg each beeing {field:Fieldtype, message:message}
	 * @param fieldVal
	 * @param message
	 */
	public void addErrorForForm(String fieldVal, String message) {
		JSONObject newobj = new JSONObject();
		newobj.put("field", fieldVal);
		newobj.put("message", message);
		this.many_in_one_erros.append("errors", newobj);
	}
	
	/**
	 * Usage : addToErrorArray(arrayKey,key,value) 
	 *   For : arrayKey(STRING) - ID of the array you want to add this into 
	 *         key(STRING) - key of the json obj that  will be created 
	 *         value - value of key 
	 *  After: adds the a new json obj  {key:value} to the arrayKey list
	 * 
	 * @param arrayKey
	 * @param key
	 * @param value
	 */
	public void addToErrorArray(String arrayKey, String key, String value) {
		JSONObject newobj = new JSONObject();
		newobj.put(key, value);
		this.many_in_one_erros.append(arrayKey, newobj);
	}
	
	/**
	 * Usage : addToErrorArray(arrayKey,newobj) 
	 *   For : arrayKey(STRING) - ID of the array you want to add this into 
	 *         newobj(JSONObject)- custom JSONObject 
	 *  After: adds the a new json obj to the arrayKey list
	 * 
	 * @param arrayKey
	 * @param newobj
	 */
	public void addToErrorArray(String arrayKey, JSONObject newobj) {
		this.many_in_one_erros.append(arrayKey, newobj);
	}
	
	/**
	 * Usage : addSingleError(key,value) 
	 *   For : key,value are strings 
	 *  After: adds a single error to the error List
	 * 
	 * @param key
	 * @param value
	 */
	public void addSingleError(String key, String value) {
		JSONObject error = new JSONObject();
		this.error.add(error.put(key, value));
	}
	
	/**
	 * Usage : addSingleError(newobj) 
	 *   For : newobj is a custom JSONObject 
	 *  After: adds a single jsonobj to the error List
	 * 
	 * @param newobj
	 */
	public void addSingleError(JSONObject newobj) {
		this.error.add(newobj);
	}

	/**
	 * Usage : addToSuccArray(arrayKey,key,value) For : arrayKey(STRING) - ID of the
	 * array you want to add this into key(STRING) - key of the json obj that will
	 * be created value - value of key After: adds the a new json obj {key:value} to
	 * the arrayKey list
	 * 
	 * @param arrayKey
	 * @param key
	 * @param value
	 */
	public void addToSuccArray(String arrayKey, String key, String value) {
		JSONObject newobj = new JSONObject();
		newobj.put(key, value);
		this.many_in_one_succ.append(arrayKey, newobj);
	}
	
	/**
	 * Usage : addToSuccArray(arrayKey,newobj) 
	 *   For : arrayKey(STRING) - ID of the array you want to add this into newobj(JSONObject) 
	 *  After: adds the a new json  obj to the arrayKey list
	 * 
	 * @param arrayKey
	 * @param newobj
	 */
	public void addToSuccArray(String arrayKey, JSONObject newobj) {
		this.many_in_one_succ.append(arrayKey, newobj);
	}
	
	/**
	 * Adds a single key-value object (string-string) to success list.
	 * 
	 * @param key
	 * @param value
	 */
	public void addSingleSucc(String key, String value) {
		JSONObject error = new JSONObject();
		this.response.add(error.put(key, value));
	}
	
	/**
	 * Adds JSON object <code>newobj</code> to successful list.
	 * 
	 * @param newobj
	 */
	public void addSingleSucc(JSONObject newobj) {
		this.response.add(newobj);
	}

	/**
	 * Checks if errors exist.
	 * 
	 * @return <code>true</code> if errors exist, otherwise <code>false</code>.
	 */
	public boolean errorsExist() {
		if (this.error.size() > 0 || this.many_in_one_erros.length() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * When we are about to return the response we add the wrappers.
	 * 
	 * @return
	 */
	public String getErrorResponse() {
		if (this.many_in_one_erros.length() > 0) {
			this.error.add(this.many_in_one_erros);
		}
		return this.errors_response.put("BadResp", this.error).toString();
	}

	/**
	 * returns all the responses that were added to the response array
	 * @return
	 */
	public String getSuccessResponse() {
		if (this.many_in_one_succ.length() > 0) {
			this.response.add(this.many_in_one_succ);
		}
		return this.errors_response.put("GoodResp", this.response).toString();
	}

}
