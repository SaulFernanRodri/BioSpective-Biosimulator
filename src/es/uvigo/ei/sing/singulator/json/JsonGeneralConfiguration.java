package es.uvigo.ei.sing.singulator.json;

import java.io.Serializable;

public class JsonGeneralConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer numberOfProccesorsCreation;
	private Integer numberOfProccesors;
	private Integer totalTries;
	private String dirOutput;
	private String fileOutput;
	private String simulationName;
	private String simulationType;
	private String[] emailTo;

	private boolean activateGUI;
	private String readFromCheckpoint;
	private Integer numberOfJobs;
	private Integer numberOfSteps;
	private Integer saveSimulationEvery;
	private Integer writeResultsEvery;
	private Integer randomRebound;
	private Integer stepsRandomRebound;

	public JsonGeneralConfiguration() {

	}

	public Integer getProcCreation() {
		return numberOfProccesorsCreation;
	}

	public void setProcCreation(Integer procCreation) {
		this.numberOfProccesorsCreation = procCreation;
	}

	public Integer getProcSim() {
		return numberOfProccesors;
	}

	public void setProcSim(Integer procSim) {
		this.numberOfProccesors = procSim;
	}

	public Integer getTotalTries() {
		return totalTries;
	}

	public void setTotalTries(Integer totalTries) {
		this.totalTries = totalTries;
	}

	public boolean isActivateGUI() {
		return activateGUI;
	}

	public void setActivateGUI(boolean activateGUI) {
		this.activateGUI = activateGUI;
	}

	public String getReadFromCheckpoint() {
		return readFromCheckpoint;
	}

	public void setReadFromCheckpoint(String readFromCheckpoint) {
		this.readFromCheckpoint = readFromCheckpoint;
	}

	public Integer getNumberOfJobs() {
		return numberOfJobs;
	}

	public void setNumberOfJobs(Integer numberOfJobs) {
		this.numberOfJobs = numberOfJobs;
	}

	public Integer getNumberOfSteps() {
		return numberOfSteps;
	}

	public void setNumberOfSteps(Integer numberOfSteps) {
		this.numberOfSteps = numberOfSteps;
	}

	public Integer getSaveSimEvery() {
		return saveSimulationEvery;
	}

	public void setSaveSimEvery(Integer saveSimEvery) {
		this.saveSimulationEvery = saveSimEvery;
	}

	public Integer getWriteResultsEvery() {
		return writeResultsEvery;
	}

	public void setWriteResultsEvery(Integer writeResultsEvery) {
		this.writeResultsEvery = writeResultsEvery;
	}

	public String getDirOutput() {
		return dirOutput;
	}

	public void setDirOutput(String dirOutput) {
		this.dirOutput = dirOutput;
	}

	public String getFileOutput() {
		return fileOutput;
	}

	public void setFileOutput(String fileOutput) {
		this.fileOutput = fileOutput;
	}

	public String getSimName() {
		return simulationName;
	}

	public void setSimName(String simName) {
		this.simulationName = simName;
	}

	public String getSimulationType() {
		return simulationType;
	}

	public void setSimulationType(String simulationType) {
		this.simulationType = simulationType;
	}

	public String[] getEmailTo() {
		return emailTo;
	}

	public void setEmailTo(String[] emailTo) {
		this.emailTo = emailTo;
	}

	public Integer getRandomRebound() {
		return randomRebound;
	}

	public void setRandomRebound(Integer randomRebound) {
		this.randomRebound = randomRebound;
	}

	public Integer getStepsRandomRebound() {
		return stepsRandomRebound;
	}

	public void setStepsRandomRebound(Integer stepsRandomRebound) {
		this.stepsRandomRebound = stepsRandomRebound;
	}
}
