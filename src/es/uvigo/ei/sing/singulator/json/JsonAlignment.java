package es.uvigo.ei.sing.singulator.json;

import java.io.Serializable;

public class JsonAlignment implements Serializable {

	private static final long serialVersionUID = 1L;

	private String axis;
	private double distance;

	private double half_sphero;

	public JsonAlignment() {

	}

	public String getAxis() {
		return axis;
	}

	public void setAxis(String axis) {
		this.axis = axis;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getHalf_sphero() {
		return half_sphero;
	}

	public void setHalf_sphero(double half_sphero) {
		this.half_sphero = half_sphero;
	}


}
