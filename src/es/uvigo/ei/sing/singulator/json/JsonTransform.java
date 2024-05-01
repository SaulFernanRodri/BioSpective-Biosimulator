package es.uvigo.ei.sing.singulator.json;

import java.io.Serializable;

public class JsonTransform implements Serializable {

	private static final long serialVersionUID = 1L;

	private String from;
	private String to;
	private String trigger;
	private String with;

	public JsonTransform() {

	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
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
