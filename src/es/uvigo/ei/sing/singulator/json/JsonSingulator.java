package es.uvigo.ei.sing.singulator.json;

import java.io.Serializable;

public class JsonSingulator implements Serializable {

	private static final long serialVersionUID = 1L;

	private JsonGeneralConfiguration generalConfiguration;
	private JsonEnvironment environment;
	private JsonUnity unity;
	private JsonCell[] cells;
	private JsonAlignment alignment;
	private JsonAgent agents;
	private JsonTransporter[] transporters;
	private JsonFeeder[] feeder;
	private JsonEvent events;

	public JsonSingulator() {

	}

	public JsonGeneralConfiguration getGeneralConfiguration() {
		return generalConfiguration;
	}

	public void setGeneralConfiguration(JsonGeneralConfiguration generalConfiguration) {
		this.generalConfiguration = generalConfiguration;
	}

	public JsonEnvironment getEnvironment() {
		return environment;
	}

	public void setEnvironment(JsonEnvironment environment) {
		this.environment = environment;
	}

	public JsonUnity getUnity() {
		return unity;
	}

	public void setUnity(JsonUnity unity) {
		this.unity = unity;
	}

	public JsonCell[] getCells() {
		return cells;
	}

	public void setCells(JsonCell[] cells) {
		this.cells = cells;
	}

	public JsonAlignment getAlignment() {
		return alignment;
	}

	public void setAlignment(JsonAlignment alignment) {
		this.alignment = alignment;
	}

	public JsonAgent getAgents() {
		return agents;
	}

	public void setAgents(JsonAgent agents) {
		this.agents = agents;
	}

	public JsonTransporter[] getTransporters() {
		return transporters;
	}

	public void setTransporters(JsonTransporter[] transporters) {
		this.transporters = transporters;
	}

	public JsonFeeder[] getFeeder() {
		return feeder;
	}

	public void setFeeder(JsonFeeder[] feeder) {
		this.feeder = feeder;
	}

	public JsonEvent getEvents() {
		return events;
	}

	public void setEvents(JsonEvent events) {
		this.events = events;
	}
}
