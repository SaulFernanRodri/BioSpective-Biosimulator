package es.uvigo.ei.sing.singulator.json;

import java.io.Serializable;

public class JsonFeeder implements Serializable {

	private static final long serialVersionUID = 1L;

	private String create;
	private String type;
	private String location;

	private int everyStep;
	private int productionNumber;
	private int maxConcentration;

	public JsonFeeder() {

	}

	public String getCreate() {
		return create;
	}

	public void setCreate(String create) {
		this.create = create;
	}

	public Integer getMaxConcentration() {
		return maxConcentration;
	}

	public void setMaxConcentration(Integer maxConcentration) {
		this.maxConcentration = maxConcentration;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getEveryStep() {
		return everyStep;
	}

	public void setEveryStep(int everyStep) {
		this.everyStep = everyStep;
	}

	public int getProductionNumber() {
		return productionNumber;
	}

	public void setProductionNumber(int productionNumber) {
		this.productionNumber = productionNumber;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void setMaxConcentration(int maxConcentration) {
		this.maxConcentration = maxConcentration;
	}
}
