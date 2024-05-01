package es.uvigo.ei.sing.singulator.json;

import java.io.Serializable;

public class JsonEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	private JsonKill[] kill;
	private JsonReaction[] reaction;
	private JsonTransform[] transform;

	public JsonEvent() {

	}

	public JsonKill[] getKill() {
		return kill;
	}

	public void setKill(JsonKill[] kill) {
		this.kill = kill;
	}

	public JsonReaction[] getReaction() {
		return reaction;
	}

	public void setReaction(JsonReaction[] reaction) {
		this.reaction = reaction;
	}

	public JsonTransform[] getTransform() {
		return transform;
	}

	public void setTransform(JsonTransform[] transform) {
		this.transform = transform;
	}
}
