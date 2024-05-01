package es.uvigo.ei.sing.singulator.agents;

import java.awt.Color;
import java.io.Serializable;

import es.uvigo.ei.sing.singulator.constants.Constants;
import es.uvigo.ei.sing.singulator.interfaces.iLayer;
import sim.util.Double3D;

public class CellLayer implements iLayer, Serializable {
	private static final long serialVersionUID = 1L;

	// Integer variables
	private int zoneID;

	// Double variables
	private double h1Center;
	private double h1Min;
	private double h2Center;
	private double h2Max;
	private double height;
	private double radius;
	private double scale;

	// Double3D variables
	private Double3D location;

	// String variables
	private String name;

	// Complex variables
	private Color color;
	private Cell parent;

	public CellLayer(String name, double scale, double radius, double height, String color, int zoneID,
			Double3D location, Cell parent) {
		this.name = name;
		this.scale = scale;
		this.radius = radius;
		this.height = height;
		this.color = Color.decode(color);
		this.zoneID = zoneID;
		this.location = location;
		this.parent = parent;
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public double getH1Center() {
		return h1Center;
	}

	@Override
	public double getH1Min() {
		return h1Min;
	}

	@Override
	public double getH2Center() {
		return h2Center;
	}

	public double getH2Max() {
		return h2Max;
	}

	@Override
	public double getHeight() {
		return height;
	}

	@Override
	public Double3D getLocation() {
		return location;
	}

	public String getName() {
		return name;
	}

	@Override
	public double getRadius() {
		return radius;
	}

	@Override
	public double getScale() {
		return scale;
	}

	@Override
	public String getForm() {
		return parent.form;
	}

	public int getZoneID() {
		return zoneID;
	}

	@Override
	public int getExtent() {
		return parent.extent;
	}

	@Override
	public void setExtent(int extent) {
		parent.extent = extent;
	}

	@Override
	public double getHorizontalAngle() {
		// TODO Auto-generated method stub
		return parent.getHorizontalAngle();
	}

	@Override
	public void setHorizontalAngle(double horizontalAngle) {
		// TODO Auto-generated method stub
		parent.setHorizontalAngle(horizontalAngle);
	}

	@Override
	public double getVerticalAngle() {
		// TODO Auto-generated method stub
		return parent.getVerticalAngle();
	}

	@Override
	public void setVerticalAngle(double verticalAngle) {
		// TODO Auto-generated method stub
		parent.setVerticalAngle(verticalAngle);
	}

	@Override
	public void setLocation(Double3D location) {
		// TODO Auto-generated method stub
		parent.setLocation(location);
		this.location = location;

		// Type capsule
		if (parent.form.equals(Constants.CAPSULE)) {
			// Calculate hemispheres
			this.h1Center = location.x - height / 2;
			this.h2Center = location.x + height / 2;
			this.h1Min = h1Center - radius;
			this.h2Max = h2Center + radius;
		}
		// Type sphere
		else if (parent.form.equals(Constants.SPHERE)) {
			// Calculate hemispheres
			this.h1Center = location.x;
			this.h2Center = location.x;
			this.h1Min = h1Center - radius;
			this.h2Max = h2Center + radius;
		}
		// Type hemisphere
		else {
			this.h1Center = location.x;
			this.h2Center = location.x;
			this.h1Min = h1Center;
			this.h2Max = h2Center + radius;
		}
	}

	@Override
	public String getCellName() {
		return parent.cellName;
	}

	@Override
	public String getType() {
		return parent.getType();
	}
}
