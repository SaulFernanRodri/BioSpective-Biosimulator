package es.uvigo.ei.sing.singulator.json;

import java.io.Serializable;

public class JsonKill implements Serializable {

	private static final long serialVersionUID = 1L;

	private String input;
	private String trigger;
	private String with;

	public JsonKill() {

	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getTrigger() {
		return trigger;
	}

	public void setTrigger(String trigger) {
		this.trigger = trigger;
	}

	public String getWith() {
		return with;
	}

	public void setWith(String with) {
		this.with = with;
	}
}
