package es.uvigo.ei.sing.singulator.modules.distribution;

import java.io.Serializable;

public class SpaceExtents3D implements Serializable {

	private static final long serialVersionUID = 1L;

	public final int nP; // number of partitions

	public final double width; // width of total grid or space

	public final double height; // height of total grid or space

	public final double length; // length of total grid or space

	private int xpar; // # of x tiles

	private int ypar; // # of y tiles

	private int zpar; // # of z tiles

	private double[] xlim; // [0..xpar-1]

	private double[] ylim; // [0..ypar-1]

	private double[] zlim; // [0..zpar-1]

	private double[] x0; // [0..xpar-1]

	private double[] y0; // [0..ypar-1]

	private double[] z0; // [0..zpar-1]

	public SpaceExtents3D(final int nP, final double width, final double height, final double length) {
		this.nP = nP;
		this.width = width;
		this.height = height;
		this.length = length;

		computePartitions(nP, width, height, length);
	}

	public SpaceExtents3D(final int nP, final double width, final double height, final double length,
			final double discretization) {
		this(nP, width / discretization, height / discretization, length / discretization);
	}

	public int liesIn(final double x, final double y, final double z) {
		// for some value of xpar, ypar, zpar, binary search would be faster..
		int i = 0;
		try {
			while (x >= xlim[i])
				i++;
		} catch (ArrayIndexOutOfBoundsException e) {
			i--;
		}

		int j = 0;
		try {
			while (y >= ylim[j])
				j++;
		} catch (ArrayIndexOutOfBoundsException e) {
			j--;
		}

		int k = 0;
		try {
			while (z >= zlim[k])
				k++;
		} catch (ArrayIndexOutOfBoundsException e) {
			k--;
		}

		return (i * this.ypar + j) * this.zpar + k;
	}

	public double x0(final int p) {
		return this.x0[(p / this.zpar) / this.ypar];
	}

	public double y0(final int p) {
		return this.y0[(p / this.zpar) % this.ypar];
	}

	public double z0(final int p) {
		return this.z0[p % this.zpar];
	}

	public double xlim(final int p) {
		return this.xlim[(p / this.zpar) / this.ypar];
	}

	public double ylim(final int p) {
		return this.ylim[(p / this.zpar) % this.ypar];
	}

	public double zlim(final int p) {
		return this.zlim[p % this.zpar];
	}

	private void computePartitions(final int nP, final double width, final double height, final double length) {
		// produce three "nearly equal" factors of nP, xpar, ypar and zpar
		factor3(nP);

		this.x0 = new double[this.xpar];
		this.y0 = new double[this.ypar];
		this.z0 = new double[this.zpar];
		this.xlim = new double[this.xpar];
		this.ylim = new double[this.ypar];
		this.zlim = new double[this.zpar];

		this.xlim = partition(width, this.xpar);
		this.ylim = partition(height, this.ypar);
		this.zlim = partition(length, this.zpar);
		for (int i = 1; i < this.xlim.length; i++) {
			this.x0[i] = this.xlim[i - 1];
			this.xlim[i] += this.x0[i];
		}
		for (int i = 1; i < this.ylim.length; i++) {
			this.y0[i] = this.ylim[i - 1];
			this.ylim[i] += this.y0[i];
		}
		for (int i = 1; i < this.zlim.length; i++) {
			this.z0[i] = this.zlim[i - 1];
			this.zlim[i] += this.z0[i];
		}
	}

	private void factor3(final int n) {
		int u = (int) Math.pow(n, (1. / 3.)) + 1;
		while (n % u != 0)
			u--;
		final int m = n / u;
		int v = (int) Math.pow(m, 0.5) + 1;
		while (m % v != 0)
			v--;
		this.xpar = u;
		this.ypar = v;
		this.zpar = m / v;
	}

	private double[] partition(double x, int n) {
		// !System.out.format("x=%f, n=%d\n", x, n);
		double[] part = new double[n];
		int i = 0;
		while (n > 0) {
			double q = x / n;
			part[i++] = q;
			x -= q;
			n--;
			// !System.out.format("q=%f, x=%f, n=%d\n", q, x, n);
		}
		return part;
	}

}
