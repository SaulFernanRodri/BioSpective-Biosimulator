package es.uvigo.ei.sing.singulator.json;

import java.io.Serializable;

public class JsonDiffusionRate implements Serializable {

	private static final long serialVersionUID = 1L;

	private double exterior;
	private double outerMembrane;
	private double outerPeriplasm;
	private double peptidoglycan;
	private double innerPeriplasm;
	private double innerMembrane;
	private double cytoplasm;

	public JsonDiffusionRate() {

	}

	public double getExterior() {
		return exterior;
	}

	public void setExterior(double exterior) {
		this.exterior = exterior;
	}

	public double getOuterMembrane() {
		return outerMembrane;
	}

	public void setOuterMembrane(double outerMembrane) {
		this.outerMembrane = outerMembrane;
	}

	public double getOuterPeriplasm() {
		return outerPeriplasm;
	}

	public void setOuterPeriplasm(double outerPeriplasm) {
		this.outerPeriplasm = outerPeriplasm;
	}

	public double getPeptidoglycan() {
		return peptidoglycan;
	}

	public void setPeptidoglycan(double peptidoglycan) {
		this.peptidoglycan = peptidoglycan;
	}

	public double getInnerPeriplasm() {
		return innerPeriplasm;
	}

	public void setInnerPeriplasm(double innerPeriplasm) {
		this.innerPeriplasm = innerPeriplasm;
	}

	public double getInnerMembrane() {
		return innerMembrane;
	}

	public void setInnerMembrane(double innerMembrane) {
		this.innerMembrane = innerMembrane;
	}

	public double getCytoplasm() {
		return cytoplasm;
	}

	public void setCytoplasm(double cytoplasm) {
		this.cytoplasm = cytoplasm;
	}
}
