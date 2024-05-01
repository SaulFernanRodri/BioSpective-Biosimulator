package es.uvigo.ei.sing.singulator.modules.events;

import java.io.Serializable;

import es.uvigo.ei.sing.singulator.agents.Molecule;
import es.uvigo.ei.sing.singulator.agents.SphereMolecule;
import es.uvigo.ei.sing.singulator.constants.Constants;
import es.uvigo.ei.sing.singulator.interfaces.iEvent;
import es.uvigo.ei.sing.singulator.interfaces.iMolecule;
import es.uvigo.ei.sing.singulator.simulator.SINGulator_Logic;
import es.uvigo.ei.sing.singulator.simulator.SINGulator_Model;
import sim.util.Double3D;

public class Transform implements iEvent, Serializable {

	private static final long serialVersionUID = 1L;

	private String inputAgent;
	private String outputAgent;
	private String trigger;
	private String with;
	private int layer;

	private SINGulator_Logic parent;

	public Transform(String[] transformEventInfo, SINGulator_Logic parent) {
		// 0: from, 1: to, 2: trigger, 3: with
		this.inputAgent = transformEventInfo[0];
		this.outputAgent = transformEventInfo[1];
		this.trigger = transformEventInfo[2];
		this.with = transformEventInfo[3];

		if (Constants.mapLayerNameLayerId.containsKey(with) && trigger.equals(Constants.EVENTS_TRIGGER_INNER)) {
			layer = Constants.mapLayerNameLayerId.get(with);
		} else {
			layer = -1;
		}

		this.parent = parent;
	}

	@Override
	public int act(iMolecule... agents) {
		int toRet = 2;
		// Get cellSimulator3D
		SINGulator_Model simulator = parent.parent;
		// Get molecule
		iMolecule agent = agents[0];
		String[] info;
		Double3D currentLocation, finalPosition;
		int cellID;
		iMolecule result = null;
		double random1, random2;

		// Si agents == 1, entras al metodo al entrar o rebotar contra una capa
		if (agents.length == 1) {
			if (agent.getCurrentZone() == layer || with.equals(agent.getLastReboundWith())) {
				// [0]:Name,[1]:MW,[2]:Radius,[3...9]:DR,[10]:Color,[11]:Number,[12]:
				// MaxLayer,[13]:minLayer,[14]:radInfl,[15]:radInflWith,[16]:type,[17]:mRNA
				info = parent.parent.mapNameInformation.get(outputAgent);
				finalPosition = agent.getFinalPosition();
				currentLocation = agent.getCurrentLocation();

				synchronized (simulator.random) {
					random1 = simulator.random.nextInt(360);
					random2 = simulator.random.nextInt(360);
				}

				if (Constants.CAN_GUI) {
					// Create molecule with data
					result = new SphereMolecule(parent.agentID.getAndIncrement(), outputAgent,
							Double.parseDouble(info[1]), Double.parseDouble(info[2]), info[10],
							Double.parseDouble(info[3]), Double.parseDouble(info[4]), Double.parseDouble(info[5]),
							Double.parseDouble(info[6]), Double.parseDouble(info[7]), Double.parseDouble(info[8]),
							Double.parseDouble(info[9]), Integer.parseInt(info[12]), Integer.parseInt(info[13]),
							random1, random2, agent.getCurrentZone(), "", Integer.parseInt(info[14]), info[15],
							info[16], info[17]);
				} else {
					// Create molecule with data
					result = new Molecule(parent.agentID.getAndIncrement(), outputAgent, Double.parseDouble(info[1]),
							Double.parseDouble(info[2]), info[10], Double.parseDouble(info[3]),
							Double.parseDouble(info[4]), Double.parseDouble(info[5]), Double.parseDouble(info[6]),
							Double.parseDouble(info[7]), Double.parseDouble(info[8]), Double.parseDouble(info[9]),
							Integer.parseInt(info[12]), Integer.parseInt(info[13]), random1, random2,
							agent.getCurrentZone(), "", Integer.parseInt(info[14]), info[15], info[16], info[17]);
				}

				// Con multiples hilos se posterga la creacion al hilo
				// de prioridad maxima
				simulator.toCreateAgents.add(result);

				result.setInitialPosition(finalPosition);
				result.setFinalPosition(finalPosition);
				result.setCurrentLocation(currentLocation);
				result.setDesiredLocation(currentLocation);

				cellID = agent.getCellId();
				result.setCellId(agent.getCellId());
				if (cellID != -1) {
					simulator.mapIdCell.get(cellID).addMoleculeToCell(result);
					simulator.mapIdCell.get(cellID).removeMoleculeInCell(agent);
				}

				agent.putToStop(true);
				simulator.deadAgents.add(agent);

				// if (parent.parent.hasFeeder) {
				// // Comprobar si este agente necesita insertarse desde feeder
				// simulator.simulatorLogic.parent.feeder.checkAndIncrementMoleculeCreation(agent.getName());
				// }

				toRet = 0;
			} else {
				toRet = 2;
			}
		}
		// Si > 1, entras al rebotar contra otro agente
		else {
			if ((with.equals(agents[1].getName()) && !agents[1].getType().equals(Constants.RIBOSOME))
					|| (agents[1].getType().equals(Constants.RIBOSOME) && with.equals(agents[1].getName())
							&& agent.getName().charAt(0) == agents[1].showMRna())) {
				// [0]:Name,[1]:MW,[2]:Radius,[3...9]:DR,[10]:Color,[11]:Number,[12]:
				// MaxLayer,[13]:minLayer,[14]:radInfl,[15]:radInflWith,[16]:type,[17]:mRNA
				info = parent.parent.mapNameInformation.get(outputAgent);

				finalPosition = agent.getFinalPosition();
				currentLocation = agent.getCurrentLocation();

				synchronized (simulator.random) {
					random1 = simulator.random.nextInt(360);
					random2 = simulator.random.nextInt(360);
				}

				if (Constants.CAN_GUI) {
					// Create molecule with data
					result = new SphereMolecule(parent.agentID.getAndIncrement(), outputAgent,
							Double.parseDouble(info[1]), Double.parseDouble(info[2]), info[10],
							Double.parseDouble(info[3]), Double.parseDouble(info[4]), Double.parseDouble(info[5]),
							Double.parseDouble(info[6]), Double.parseDouble(info[7]), Double.parseDouble(info[8]),
							Double.parseDouble(info[9]), Integer.parseInt(info[12]), Integer.parseInt(info[13]),
							random1, random2, agent.getCurrentZone(), "", Integer.parseInt(info[14]), info[15],
							info[16], info[17]);
				} else {
					// Create molecule with data
					result = new Molecule(parent.agentID.getAndIncrement(), outputAgent, Double.parseDouble(info[1]),
							Double.parseDouble(info[2]), info[10], Double.parseDouble(info[3]),
							Double.parseDouble(info[4]), Double.parseDouble(info[5]), Double.parseDouble(info[6]),
							Double.parseDouble(info[7]), Double.parseDouble(info[8]), Double.parseDouble(info[9]),
							Integer.parseInt(info[12]), Integer.parseInt(info[13]), random1, random2,
							agent.getCurrentZone(), "", Integer.parseInt(info[14]), info[15], info[16], info[17]);
				}

				// Con multiples hilos se posterga la creacion al hilo
				// de prioridad maxima
				simulator.toCreateAgents.add(result);

				result.setInitialPosition(finalPosition);
				result.setFinalPosition(finalPosition);
				result.setCurrentLocation(currentLocation);
				result.setDesiredLocation(currentLocation);

				cellID = agent.getCellId();
				result.setCellId(agent.getCellId());
				if (cellID != -1) {
					simulator.mapIdCell.get(cellID).addMoleculeToCell(result);
					simulator.mapIdCell.get(cellID).removeMoleculeInCell(agent);
				}

				agent.putToStop(true);
				simulator.deadAgents.add(agent);

				// if (parent.parent.hasFeeder) {
				// // Comprobar si este agente necesita insertarse desde feeder
				// simulator.simulatorLogic.parent.feeder.checkAndIncrementMoleculeCreation(agent.getName());
				// }

				// Comparas nombre contra letra del MRNA que haya en ese momento
				if (agents[1].getType().equals(Constants.RIBOSOME)
						&& agent.getName().charAt(0) == agents[1].showMRna()) {
					agents[1].removeMRna();
				}

				toRet = 0;
			} else {
				toRet = 2;
			}
		}

		return toRet;
	}

	@Override
	public boolean checkInputs(iMolecule... agents) {
		return inputAgent.equals(agents[0].getName());
	}
}
