package project.payloads;

import java.util.Map;
import java.util.HashMap;

public class RelationsResponder {

	private final Map<String, Object> relations;

	public RelationsResponder() {
		this.relations = new HashMap<String, Object>();
	}

	public void add(String key, Object value) {
		this.relations.put(key, value);
	}

	public Map<String, Object> getRelations() {
		return relations;
	}
}
