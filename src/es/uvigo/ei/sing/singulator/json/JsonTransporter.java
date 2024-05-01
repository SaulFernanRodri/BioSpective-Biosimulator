package es.uvigo.ei.sing.singulator.json;

import java.io.Serializable;

public class JsonTransporter implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private String cellName;
	private double radius;
	private double diffusionRate;
	private String color;
	private Integer number;
	private String outerLayer;
	private String innerLayer;
	private String getFrom;
	private String putTo;
	private String type;
	private String[] inputs;
	private String[] outputs;

	public JsonTransporter() {

	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getDiffusionRate() {
		return diffusionRate;
	}

	public void setDiffusionRate(double diffusionRate) {
		this.diffusionRate = diffusionRate;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCellName() {
		return cellName;
	}

	public void setCellName(String cellname) {
		this.cellName = cellname;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getOuterLayer() {
		return outerLayer;
	}

	public void setOuterLayer(String outerLayer) {
		this.outerLayer = outerLayer;
	}

	public String getInnerLayer() {
		return innerLayer;
	}

	public void setInnerLayer(String innerLayer) {
		this.innerLayer = innerLayer;
	}

	public String getGetFrom() {
		return getFrom;
	}

	public void setGetFrom(String getFrom) {
		this.getFrom = getFrom;
	}

	public String getPutTo() {
		return putTo;
	}

	public void setPutTo(String putTo) {
		this.putTo = putTo;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String[] getInputs() {
		return inputs;
	}

	public void setInputs(String[] inputs) {
		this.inputs = inputs;
	}

	public String[] getOutputs() {
		return outputs;
	}

	public void setOutputs(String[] outputs) {
		this.outputs = outputs;
	}
}
