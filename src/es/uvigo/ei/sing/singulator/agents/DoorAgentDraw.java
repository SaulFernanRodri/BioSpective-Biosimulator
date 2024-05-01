package es.uvigo.ei.sing.singulator.agents;

import java.awt.Color;
import java.io.Serializable;

import sim.util.Double3D;

public class DoorAgentDraw implements Serializable {
	private static final long serialVersionUID = 1L;

	// Integer variables
	private int zone;

	// Double variables
	private double radius;

	// String variables
	private String name;

	// Double3D variables
	private Double3D location;

	// Complex variables
	private Color color;

	public DoorAgentDraw(String name, Color color, double radius, int zone, Double3D location) {
		this.name = name;
		this.color = color;
		this.radius = radius;
		this.zone = zone;
		this.location = location;
	}

	public String getName() {
		return name;
	}

	public double getRadius() {
		return radius;
	}

	public Color getColor() {
		return color;
	}

	public Double3D getInteriorLocation() {
		return location;
	}

	public int getZone() {
		return zone;
	}

	public Double3D getLocation() {
		return location;
	}

	public void setLocation(Double3D location) {
		this.location = location;
	}
}
