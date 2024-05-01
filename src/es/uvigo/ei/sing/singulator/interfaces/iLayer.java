package es.uvigo.ei.sing.singulator.interfaces;

import java.awt.Color;

import sim.util.Double3D;

public interface iLayer {
	public String getForm();

	public String getType();

	public double getRadius();

	public double getH1Center();

	public double getH2Center();

	public double getScale();

	public double getH1Min();

	public Double3D getLocation();

	public int getExtent();

	public void setExtent(int extent);

	public double getHorizontalAngle();

	public void setHorizontalAngle(double horizontalAngle);

	public double getVerticalAngle();

	public void setVerticalAngle(double verticalAngle);

	public double getHeight();

	public void setLocation(Double3D location);

	public Color getColor();

	public String getCellName();
}
