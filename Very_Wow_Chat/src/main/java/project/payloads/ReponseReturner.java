package project.payloads;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class ReponseReturner {

	private Map<String, Object> results;
	private Map<String, List<Map<String, Object>>> errors;

	public ReponseReturner() {
		results = new HashMap<String, Object>();
		errors = new HashMap<String, List<Map<String, Object>>>();

		List<Map<String, Object>> e = new ArrayList<>();
		errors.put("errors", e);
	}

	public void addError(String key, String value) {
		Map<String, Object> error = new HashMap<String, Object>();
		error.put(key, value);
		errors.get("errors").add(error);
	}

	public Map<String, List<Map<String, Object>>> getErrors() {
		return this.errors;
	}

	public void addResult(String key, Object value) {
		results.put(key, value);
	}

	public Map<String, Object> getResults() {
		return this.results;
	}
}
