package es.uvigo.ei.sing.singulator.json;

import java.io.Serializable;

public class JsonUnity implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private double molecularWeight;
	private double radius;
	private JsonDiffusionRate diffusionRate;

	public JsonUnity() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getMolecularWeight() {
		return molecularWeight;
	}

	public void setMolecularWeight(double molecularWeight) {
		this.molecularWeight = molecularWeight;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public JsonDiffusionRate getDiffusionRate() {
		return diffusionRate;
	}

	public void setDiffusionRate(JsonDiffusionRate diffusionRate) {
		this.diffusionRate = diffusionRate;
	}
}
