package es.uvigo.ei.sing.singulator.json;

import java.io.Serializable;

public class JsonCell implements Serializable {

	private static final long serialVersionUID = 1L;

	private String cellName;
	private String layerName;
	private double radius;
	private double height;
	private String color;
	private Integer number;
	private String form;
	private JsonLayer[] layers;

	public JsonCell() {

	}

	public String getCellName() {
		return cellName;
	}

	public void setCellName(String cellName) {
		this.cellName = cellName;
	}

	public String getLayerName() {
		return layerName;
	}

	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
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

	public String getForm() {
		return form;
	}

	public void setForm(String form) {
		this.form = form;
	}

	public JsonLayer[] getLayers() {
		return layers;
	}

	public void setLayers(JsonLayer[] layers) {
		this.layers = layers;
	}
}
