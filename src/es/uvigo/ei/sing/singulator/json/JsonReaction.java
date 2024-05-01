package es.uvigo.ei.sing.singulator.json;

import java.io.Serializable;

public class JsonReaction implements Serializable {

	private static final long serialVersionUID = 1L;

	private String[] onCollision;
	private String[] output;
	private double km;
	private Integer kcat;

	public JsonReaction() {

	}

	public String[] getOnCollision() {
		return onCollision;
	}

	public void setOnCollision(String[] onCollision) {
		this.onCollision = onCollision;
	}

	public String[] getOutput() {
		return output;
	}

	public void setOutput(String[] output) {
		this.output = output;
	}

	public double getKm() {
		return km;
	}

	public void setKm(double km) {
		this.km = km;
	}

	public Integer getKcat() {
		return kcat;
	}

	public void setKcat(Integer kcat) {
		this.kcat = kcat;
	}
}
