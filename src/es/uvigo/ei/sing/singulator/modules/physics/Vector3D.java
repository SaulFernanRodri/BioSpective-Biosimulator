package es.uvigo.ei.sing.singulator.modules.physics;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Vector3D implements Serializable, Comparable<Vector3D> {
	private static final long serialVersionUID = 1L;

	public double x;
	public double y;
	public double z;

	public Vector3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3D(double x, double y) {
		this.x = x;
		this.y = y;
		this.z = 0f;
	}

	public Vector3D() {
		this.x = 0f;
		this.y = 0f;
		this.z = 0f;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public void setXYZ(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setXYZ(Vector3D v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}

	public double magnitude() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	public Vector3D copy() {
		return new Vector3D(x, y, z);
	}

	public static Vector3D copy(Vector3D v) {
		return new Vector3D(v.x, v.y, v.z);
	}

	public void add(Vector3D v) {
		x += v.x;
		y += v.y;
		z += v.z;
	}

	public void sub(Vector3D v) {
		x -= v.x;
		y -= v.y;
		z -= v.z;
	}

	public void mult(double n) {
		x *= n;
		y *= n;
		z *= n;
	}

	public void div(double n) {
		x /= n;
		y /= n;
		z /= n;
	}

	public double dot(Vector3D v) {
		double dot = x * v.x + y * v.y + z * v.z;
		return dot;
	}

	public Vector3D cross(Vector3D v) {
		double crossX = y * v.z - v.y * z;
		double crossY = z * v.x - v.z * x;
		double crossZ = x * v.y - v.x * y;
		return (new Vector3D(crossX, crossY, crossZ));
	}

	public void normalize() {
		double m = magnitude();
		if (m > 0) {
			div(m);
		}
	}

	public void maxLimit(double max) {
		if (magnitude() > max) {
			normalize();
			mult(max);
		}
	}

	public void minLimit(double min) {
		if (magnitude() < min) {
			normalize();
			mult(min);
		}
	}

	public double heading2D() {
		double angle = Math.atan2(-y, x);
		return -1 * angle;
	}

	public void rotate2D(double theta) {
		double currentTheta = heading2D();
		double mag = magnitude();
		currentTheta += theta;
		x = mag * Math.cos(currentTheta);
		y = mag * Math.sin(currentTheta);
	}

	public static Vector3D add(Vector3D v1, Vector3D v2) {
		Vector3D v = new Vector3D(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
		return v;
	}

	public static Vector3D sub(Vector3D v1, Vector3D v2) {
		Vector3D v = new Vector3D(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
		return v;
	}

	public static Vector3D div(Vector3D v1, double n) {
		Vector3D v = new Vector3D(v1.x / n, v1.y / n, v1.z / n);
		return v;
	}

	public static Vector3D mult(Vector3D v1, double n) {
		Vector3D v = new Vector3D(v1.x * n, v1.y * n, v1.z * n);
		return v;
	}

	public static Vector3D rotate3D(Vector3D v, double theta) {
		// What is my current heading
		double currentTheta = v.heading2D();
		// What is my current speed
		double mag = v.magnitude();
		// Turn me
		currentTheta += theta;
		// Look, polar coordinates to cartesian!!
		Vector3D newV = new Vector3D(mag * Math.cos(currentTheta), mag * Math.cos(currentTheta));
		return newV;
	}

	/**
	 * Calculate the Euclidean distance between two points (considering a point
	 * as a vector object).
	 * 
	 * @param v1
	 *            a vector
	 * @param v2
	 *            another vector
	 * @return the Euclidean distance between v1 and v2
	 */
	public static double distance(Vector3D v1, Vector3D v2) {
		double dx = v1.x - v2.x;
		double dy = v1.y - v2.y;
		double dz = v1.z - v2.z;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/**
	 * Calculate the angle between two vectors, using the dot product.
	 * 
	 * @param v1
	 *            a vector
	 * @param v2
	 *            another vector
	 * @return the angle between the vectors
	 */
	public static double angleBetween(Vector3D v1, Vector3D v2) {
		double dot = v1.dot(v2);
		double theta = Math.acos(dot / (v1.magnitude() * v2.magnitude()));
		return theta;
	}

	/**
	 * Calculate the angle between two vectors, using the dot product.
	 * 
	 * @param p1
	 *            the p1
	 * @param p2
	 *            the p2
	 * @return the angle between the vectors
	 */
	Vector3D getTangent(Vector3D p1, Vector3D p2) {
		Vector3D r = new Vector3D(p1.x - p2.x, p1.y - p2.y, p1.z - p2.z);
		r.normalize();
		return r;
	}

	@Override
	public int compareTo(Vector3D toCompare) {
		BigDecimal bd1 = new BigDecimal(x);
		bd1 = bd1.setScale(2, RoundingMode.HALF_UP);
		BigDecimal bd2 = new BigDecimal(toCompare.x);
		bd2 = bd2.setScale(2, RoundingMode.HALF_UP);

		if (bd1.doubleValue() == bd2.doubleValue()) {
			bd1 = new BigDecimal(y);
			bd1 = bd1.setScale(2, RoundingMode.HALF_UP);
			bd2 = new BigDecimal(toCompare.y);
			bd2 = bd2.setScale(2, RoundingMode.HALF_UP);
			if (bd1.doubleValue() == bd2.doubleValue()) {
				bd1 = new BigDecimal(z);
				bd1 = bd1.setScale(2, RoundingMode.HALF_UP);
				bd2 = new BigDecimal(toCompare.z);
				bd2 = bd2.setScale(2, RoundingMode.HALF_UP);
				if (bd1.doubleValue() < bd2.doubleValue()) {
					return -1;
				} else {
					return 1;
				}
			} else if (bd1.doubleValue() < bd2.doubleValue()) {
				return -1;
			} else {
				return 1;
			}
		} else if (bd1.doubleValue() < bd2.doubleValue()) {
			return -1;
		} else {
			return 1;
		}
	}

	@Override
	public String toString() {
		return "{x=" + x + ", y=" + y + ", z=" + z + "}";
	}
}