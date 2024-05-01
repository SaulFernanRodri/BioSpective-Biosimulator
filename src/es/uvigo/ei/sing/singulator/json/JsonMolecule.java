package es.uvigo.ei.sing.singulator.json;

import java.io.Serializable;

public class JsonMolecule implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private double molecularWeight;
	private double radius;
	private JsonDiffusionRate diffusionRate;
	private String color;
	private Integer number;
	private String maxLayer;
	private String minLayer;
	private String cellLocalization;
	private String layerLocalization;
	private Integer radInfl;
	private String radInflWith;

	public JsonMolecule() {

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

	public Integer getRadInfl() {
		return radInfl;
	}

	public void setRadInfl(Integer radInfl) {
		this.radInfl = radInfl;
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

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getMaxLayer() {
		return maxLayer;
	}

	public void setMaxLayer(String maxLayer) {
		this.maxLayer = maxLayer;
	}

	public String getMinLayer() {
		return minLayer;
	}

	public void setMinLayer(String minLayer) {
		this.minLayer = minLayer;
	}

	public String getCellLocalization() {
		return cellLocalization;
	}

	public void setCellLocalization(String cellLocalization) {
		this.cellLocalization = cellLocalization;
	}

	public String getLayerLocalization() {
		return layerLocalization;
	}

	public void setLayerLocalization(String layerLocalization) {
		this.layerLocalization = layerLocalization;
	}

	public String getRadInflWith() {
		return radInflWith;
	}

	public void setRadInflWith(String radInflWith) {
		this.radInflWith = radInflWith;
	}

	public JsonDiffusionRate getDiffusionRate() {
		return diffusionRate;
	}

	public void setDiffusionRate(JsonDiffusionRate diffusionRate) {
		this.diffusionRate = diffusionRate;
	}
}
