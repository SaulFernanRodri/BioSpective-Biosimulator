package es.uvigo.ei.sing.singulator.constants;

import java.util.HashMap;
import java.util.Map;

public final class Constants {

	public static final Map<Integer, String> mapLayerIdLayerName;
	public static final Map<String, Integer> mapLayerNameLayerId;

	// GUI Constants
	public static boolean CAN_GUI;

	// Layer constants
	public static final int EXTERIOR = 0;
	public static final int OUTER_MEMBRANE = 1;
	public static final int OUTER_PERIPLASM = 2;
	public static final int PEPTIDOGLYCAN = 3;
	public static final int INNER_PERIPLASM = 4;
	public static final int INNER_MEMBRANE = 5;
	public static final int CYTOPLASM = 6;
	public static final String EXTERIOR_NAME = "exterior";
	public static final String OUTER_MEMBRANE_NAME = "outer membrane";
	public static final String OUTER_PERIPLASM_NAME = "outer periplasm";
	public static final String PEPTIDOGLYCAN_NAME = "peptidoglycan";
	public static final String INNER_PERIPLASM_NAME = "inner periplasm";
	public static final String INNER_MEMBRANE_NAME = "inner membrane";
	public static final String CYTOPLASM_NAME = "cytoplasm";

	// Layer types
	public static final String CAPSULE = "capsule";
	public static final String SPHERE = "sphere";
	public static final String HEMISPHERE = "hemisphere";

	// Door types
	public static final String UNIPORTER = "uniporter";
	public static final String SYMPORTER = "symporter";
	public static final String ANTIPORTER = "antiporter";

	// GUI Constants
	public static final String GUI_NAME = "SINGulator";
	public static final String GUI_VISOR = "Simulation visor";

	// JSON constants
	// Headers constants
	public static final String GENERAL_CONF = "generalConfiguration";
	public static final String ENVIRONMENT = "environment";
	public static final String UNITY = "unity";
	public static final String CELLS = "cells";
	public static final String MOLECULE = "molecule";
	public static final String TRANSPORTERS = "transporters";
	public static final String RIBOSOME = "ribosome";
	public static final String FEEDER = "feeder";
	public static final String EVENTS = "events";
	public static final String EVENTS_KILL = "kill";
	public static final String EVENTS_REACTION = "reaction";
	public static final String EVENTS_TRANSFORM = "transform";
	// Configuration constants
	public static final String PROCESSORS_CREATION = "numberOfProccesorsCreation";
	public static final String PROCESSORS = "numberOfProccesors";
	public static final String TOTAL_TRIES = "totalTries";
	public static final String SIMULATION_TYPE = "simulationType";
	public static final String ACTIVATE_GUI = "activateGUI";
	public static final String READ_CHECKPOINT = "readFromCheckpoint";
	public static final String NUMBER_JOBS = "numberOfJobs";
	public static final String NUMBER_STEPS = "numberOfSteps";
	public static final String SAVE_SIM_EVERY = "saveSimulationEvery";
	public static final String WRITE_RES_EVERY = "writeResultsEvery";
	public static final String DIR_OUTPUT = "dirOutput";
	public static final String FILE_OUTPUT = "fileOutput";
	public static final String SIMULATION_NAME = "simulationName";
	public static final String EMAIL_TO = "emailTo";
	// Simulation constants
	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";
	public static final String LENGTH = "length";
	// Unity constants
	public static final String UNITY_NAME = "name";
	public static final String UNITY_MW = "molecularWeight";
	public static final String UNITY_RADIUS = "radius";
	public static final String UNITY_DR = "diffusionRate";
	public static final String UNITY_DR0 = "exterior";
	public static final String UNITY_DR1 = "outerMembrane";
	public static final String UNITY_DR2 = "outerPeriplasm";
	public static final String UNITY_DR3 = "peptidoglycan";
	public static final String UNITY_DR4 = "innerPeriplasm";
	public static final String UNITY_DR5 = "innerMembrane";
	public static final String UNITY_DR6 = "cytoplasm";
	// Cell constants
	public static final String CELL_NAME = "cellName";
	public static final String CELL_LAYER_NAME = "layerName";
	public static final String CELL_RADIUS = "radius";
	public static final String CELL_HEIGHT = "height";
	public static final String CELL_COLOR = "color";
	public static final String CELL_NUMBER = "number";
	public static final String CELL_FORM = "form";
	public static final String CELL_LAYERS = "layers";
	// Layers constants
	public static final String LAYER_NAME = "name";
	public static final String LAYER_RADIUS = "radius";
	public static final String LAYER_HEIGHT = "height";
	public static final String LAYER_COLOR = "color";
	// Molecules constants
	public static final String AGENT_NAME = "name";
	public static final String AGENT_MW = "molecularWeight";
	public static final String AGENT_RADIUS = "radius";
	public static final String AGENT_DR = "diffusionRate";
	public static final String AGENT_DR0 = "exterior";
	public static final String AGENT_DR1 = "outerMembrane";
	public static final String AGENT_DR2 = "outerPeriplasm";
	public static final String AGENT_DR3 = "peptidoglycan";
	public static final String AGENT_DR4 = "innerPeriplasm";
	public static final String AGENT_DR5 = "innerMembrane";
	public static final String AGENT_DR6 = "cytoplasm";
	public static final String AGENT_COLOR = "color";
	public static final String AGENT_NUMBER = "number";
	public static final String AGENT_MAX_LAYER = "maxLayer";
	public static final String AGENT_MIN_LAYER = "minLayer";
	public static final String AGENT_CELL_LOC = "cellLocalization";
	public static final String AGENT_LAYER_LOC = "layerLocalization";
	public static final String AGENT_RAD_INFL = "radInfl";
	public static final String AGENT_RAD_INFL_WITH = "radInflWith";
	// Transporters constants
	public static final String TRANSPORTER_NAME = "name";
	public static final String TRANSPORTER_CELL_NAME = "cellName";
	public static final String TRANSPORTER_RADIUS = "radius";
	public static final String TRANSPORTER_DIFFUSION_RATE = "diffusionRate";
	public static final String TRANSPORTER_COLOR = "color";
	public static final String TRANSPORTER_NUMBER = "number";
	public static final String TRANSPORTER_OUTER_LAYER = "outerLayer";
	public static final String TRANSPORTER_INNER_LAYER = "innerLayer";
	public static final String TRANSPORTER_GET_FROM = "getFrom";
	public static final String TRANSPORTER_PUT_TO = "putTo";
	public static final String TRANSPORTER_TYPE = "type";
	public static final String TRANSPORTER_INPUTS = "inputs";
	public static final String TRANSPORTER_OUTPUTS = "outputs";
	// Ribosome constants
	public static final String RIBOSOME_NAME = "name";
	public static final String RIBOSOME_MW = "molecularWeight";
	public static final String RIBOSOME_RADIUS = "radius";
	public static final String RIBOSOME_DR = "diffusionRate";
	public static final String RIBOSOME_COLOR = "color";
	public static final String RIBOSOME_NUMBER = "number";
	public static final String RIBOSOME_MRNA = "mRna";
	public static final String RIBOSOME_CELL_LOC = "cellLocalization";
	public static final String RIBOSOME_LAYER_LOC = "layerLocalization";
	// Feeder constants
	public static final String FEEDER_TRIES = "tries";
	public static final String FEEDER_CREATE = "create";
	public static final String FEEDER_MAX_CONCENTRATION = "maxConcentration";
	public static final String FEEDER_ON = "on";
	public static final String FEEDER_INNER = "inner";
	public static final String FEEDER_STEP = "everyStep";
	public static final String FEEDER_REACTION = "reaction";
	// Events constants
	public static final String EVENTS_TRIGGER_REBOUND = "onRebound";
	public static final String EVENTS_TRIGGER_INNER = "onInner";

	public static final String KILL_INPUT = "input";
	public static final String KILL_TRIGGER = "trigger";
	public static final String KILL_WITH = "with";

	public static final String REACTION_INPUT = "onCollision";
	public static final String REACTION_OUTPUT = "output";
	public static final String REACTION_KM = "km";
	public static final String REACTION_KCAT = "kcat";

	public static final String TRANSFORM_INPUT = "from";
	public static final String TRANSFORM_OUTPUT = "to";
	public static final String TRANSFORM_TRIGGER = "trigger";
	public static final String TRANSFORM_WITH = "with";

	// String constants
	public static final String JSON_ERROR = "Your JSON file contains errors. Please, revise it.\n";
	public static final String JSON_NO_SELECTED = "Please, select an input JSON file with the model configuration to start the simulation.\n";
	public static final String JSON_ERROR_TITLE = "An error has occurred!";

	// Email constants
	public static final String SMTP_SERVER = "smtp.gmail.com";
	public static final String SMTP_PORT = "587";
	public static final String EMAIL_ACCOUNT = "singulator.esei.uvigo@gmail.com";
	public static final String EMAIL_PASSWORD = "Singulatorabc123.";

//	public static final String EMAIL_ACCOUNT = "biosimulator.sing.uvigo@gmail.com";
//	public static final String EMAIL_PASSWORD = "BioSimulatorabc123.";

	public static final String EMAIL_TITLE_1 = "[SINGulator] Your simulation '";
	public static final String EMAIL_TITLE_2 = "' has finished.";

	public static final String NO_SPACE_MOL = "Cannot insert all molecules.\nPlease, increment the dimensions of the simulation environment.";
	public static final String NO_SPACE_DOOR = "Cannot insert all doors.\nPlease, increment the dimensions of the layers.";
	public static final String NO_SPACE_CELL = "Cannot insert all layers.\nPlease, increment the dimensions of the simulation environment.";

	static {
		mapLayerIdLayerName = new HashMap<Integer, String>();

		mapLayerIdLayerName.put(EXTERIOR, EXTERIOR_NAME);
		mapLayerIdLayerName.put(OUTER_MEMBRANE, OUTER_MEMBRANE_NAME);
		mapLayerIdLayerName.put(OUTER_PERIPLASM, OUTER_PERIPLASM_NAME);
		mapLayerIdLayerName.put(PEPTIDOGLYCAN, PEPTIDOGLYCAN_NAME);
		mapLayerIdLayerName.put(INNER_PERIPLASM, INNER_PERIPLASM_NAME);
		mapLayerIdLayerName.put(INNER_MEMBRANE, INNER_MEMBRANE_NAME);
		mapLayerIdLayerName.put(CYTOPLASM, CYTOPLASM_NAME);

		mapLayerNameLayerId = new HashMap<String, Integer>();

		mapLayerNameLayerId.put(EXTERIOR_NAME, EXTERIOR);
		mapLayerNameLayerId.put(OUTER_MEMBRANE_NAME, OUTER_MEMBRANE);
		mapLayerNameLayerId.put(OUTER_PERIPLASM_NAME, OUTER_PERIPLASM);
		mapLayerNameLayerId.put(PEPTIDOGLYCAN_NAME, PEPTIDOGLYCAN);
		mapLayerNameLayerId.put(INNER_PERIPLASM_NAME, INNER_PERIPLASM);
		mapLayerNameLayerId.put(INNER_MEMBRANE_NAME, INNER_MEMBRANE);
		mapLayerNameLayerId.put(CYTOPLASM_NAME, CYTOPLASM);
	}
}