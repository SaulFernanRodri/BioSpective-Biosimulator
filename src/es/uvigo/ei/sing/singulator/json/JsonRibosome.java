package es.uvigo.ei.sing.singulator.json;

import java.io.Serializable;

public class JsonRibosome implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private double molecularWeight;
	private double radius;
	private double diffusionRate;
	private String color;
	private Integer number;
	private String maxLayer;
	private String minLayer;
	private String cellLocalization;
	private String layerLocalization;
	private Integer radInfl;
	private String radInflWith;
	private String mRNA;

	public JsonRibosome() {

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

	public double getDiffusionRate() {
		return diffusionRate;
	}

	public void setDiffusionRate(double diffusionRate) {
		this.diffusionRate = diffusionRate;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
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

	public Integer getRadInfl() {
		return radInfl;
	}

	public void setRadInfl(Integer radInfl) {
		this.radInfl = radInfl;
	}

	public String getRadInflWith() {
		return radInflWith;
	}

	public void setRadInflWith(String radInflWith) {
		this.radInflWith = radInflWith;
	}

	public String getmRNA() {
		return mRNA;
	}

	public void setmRNA(String mRNA) {
		this.mRNA = mRNA;
	}
}
