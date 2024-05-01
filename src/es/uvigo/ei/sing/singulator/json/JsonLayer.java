package es.uvigo.ei.sing.singulator.json;

import java.io.Serializable;

public class JsonLayer implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private double radius;
	private double height;
	private String color;

	public JsonLayer() {

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
}
