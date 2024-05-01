package es.uvigo.ei.sing.singulator.modules.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import es.uvigo.ei.sing.singulator.agents.Molecule;
import es.uvigo.ei.sing.singulator.agents.SphereMolecule;
import es.uvigo.ei.sing.singulator.constants.Constants;
import es.uvigo.ei.sing.singulator.interfaces.iEvent;
import es.uvigo.ei.sing.singulator.interfaces.iMolecule;
import es.uvigo.ei.sing.singulator.simulator.SINGulator_Logic;
import es.uvigo.ei.sing.singulator.simulator.SINGulator_Model;
import sim.util.Double3D;

public class Reaction implements iEvent, Serializable {

	private static final long serialVersionUID = 1L;

	protected double km;
	protected int kcat;

	protected List<String> inputAgents;
	protected List<String> outputAgents;

	private SINGulator_Logic parent;

	public Reaction(String[] reactionEventInfo, SINGulator_Logic parent) {
		inputAgents = new ArrayList<String>();
		outputAgents = new ArrayList<String>();

		// 0: inputs, 1: outputs, 2: km, 3: kcat
		for (String column : reactionEventInfo[0].split("\t")) {
			this.inputAgents.add(column);
		}

		for (String column : reactionEventInfo[1].split("\t")) {
			this.outputAgents.add(column);
		}

		this.km = Double.parseDouble(reactionEventInfo[2]);
		this.kcat = Integer.parseInt(reactionEventInfo[3]);

		this.parent = parent;
	}

	@Override
	public int act(iMolecule... agents) {
		// Get cellSimulator3D
		SINGulator_Model simulator = parent.parent;
		int toRet = 2;
		double reaction;

		synchronized (simulator.random) {
			reaction = simulator.random.nextDouble();
		}

		// Percentage to do the reaction (KM value)
		if (reaction >= km) {
			// Get molecules
			iMolecule agent1 = agents[0];
			iMolecule agent2 = agents[1];
			// Get environment positions
			Double3D currentLocation = simulator.environment.getObjectLocation(agent1);
			Double3D anotherLocation = simulator.environment.getObjectLocation(agent2);
			Double3D loc = null;
			Double3D finalPosition;

			// Get biggest molecule in the reaction. The resultant molecules
			// cannot
			// be bigger than the initial molecules.
			if (agent1.getRadius() >= agent2.getRadius()) {
				// Set location of molecule 1
				loc = new Double3D(currentLocation.x, currentLocation.y, currentLocation.z);

				finalPosition = agent1.getFinalPosition();
			} else {
				// Set location of molecule 2
				loc = new Double3D(anotherLocation.x, anotherLocation.y, anotherLocation.z);

				finalPosition = agent2.getFinalPosition();
			}

			// Create the output molecules
			iMolecule result = null;
			String[] info;
			int random1, random2;
			boolean stopAgent1 = true, stopAgent2 = true;
			int cellID;
			for (String output : outputAgents) {
				// Validate if one of the inputs are in the outputs and
				// reuse it
				if (output.equals(agent1.getName())) {
					result = agent1;
					// Stop molecules
					stopAgent1 = false;
				} else if (output.equals(agent2.getName())) {
					result = agent2;
					// Stop molecules
					stopAgent2 = false;
				}

				// Can reuse an existing molecule
				if (result != null) {
					// Put in the board
					synchronized (simulator.environment) {
						simulator.environment.setObjectLocation(result, loc);
					}
					result.putReactionId(simulator.reactions);
					result.putReaction(true);
					result.putTimeToWait(kcat);
				}
				// Create new molecule
				else {
					// [0]:Name,[1]:MW,[2]:Radius,[3...9]:DR,[10]:Color,[11]:Number,[12]:
					// MaxLayer,[13]:minLayer,[14]:radInfl,[15]:radInflWith,[16]:type,[17]:mRNA
					info = parent.parent.mapNameInformation.get(output);

					synchronized (simulator.random) {
						random1 = simulator.random.nextInt(360);
						random2 = simulator.random.nextInt(360);
					}

					if (Constants.CAN_GUI) {
						// Create molecule with data
						result = new SphereMolecule(parent.agentID.getAndIncrement(), output,
								Double.parseDouble(info[1]), Double.parseDouble(info[2]), info[10],
								Double.parseDouble(info[3]), Double.parseDouble(info[4]), Double.parseDouble(info[5]),
								Double.parseDouble(info[6]), Double.parseDouble(info[7]), Double.parseDouble(info[8]),
								Double.parseDouble(info[9]), Integer.parseInt(info[12]), Integer.parseInt(info[13]),
								random1, random2, agent1.getCurrentZone(), "", Integer.parseInt(info[14]), info[15],
								info[16], info[17]);
					} else {
						// Create molecule with data
						result = new Molecule(parent.agentID.getAndIncrement(), output, Double.parseDouble(info[1]),
								Double.parseDouble(info[2]), info[10], Double.parseDouble(info[3]),
								Double.parseDouble(info[4]), Double.parseDouble(info[5]), Double.parseDouble(info[6]),
								Double.parseDouble(info[7]), Double.parseDouble(info[8]), Double.parseDouble(info[9]),
								Integer.parseInt(info[12]), Integer.parseInt(info[13]), random1, random2,
								agent1.getCurrentZone(), "", Integer.parseInt(info[14]), info[15], info[16], info[17]);
					}

					// Put in the board
					// synchronized (simulator.environment) {
					// simulator.environment
					// .setObjectLocation(result, loc);
					// }
					// Con multiples hilos se posterga la creacion al hilo
					// de prioridad maxima
					simulator.toCreateAgents.add(result);
					result.putReactionId(simulator.reactions);
					result.putReaction(true);
					result.putTimeToWait(kcat);

					result.setInitialPosition(finalPosition);
					result.setFinalPosition(finalPosition);
					result.setCurrentLocation(loc);
					result.setDesiredLocation(loc);

					// Las nuevas de una reacción son imparables hasta que
					// encuentren hueco
					result.setUnstoppable(true);
					// Settear una velocidad máxima para que se hagan hueco
					// lo más rápido posible
					result.setCurrentDiffusionRate(0.99);

					// TODO: DEBERIAN SER IGUALES PARA AMBOS
					cellID = agent1.getCellId();
					result.setCellId(agent1.getCellId());
					if (cellID != -1)
						simulator.mapIdCell.get(cellID).addMoleculeToCell(result);
				}

				result = null;
			}

			if (stopAgent1) {
				agent1.putToStop(true);
				cellID = agent1.getCellId();
				if (cellID != -1)
					simulator.mapIdCell.get(cellID).removeMoleculeInCell(agent1);

				// Incluir agente muerto
				simulator.deadAgents.add(agent1);
				// Comprobar si este agente necesita insertarse desde feeder
				// if (parent.parent.hasFeeder)
				// simulator.simulatorLogic.parent.feeder.checkAndIncrementMoleculeCreation(agent1.getName());
			}
			if (stopAgent2) {
				agent2.putToStop(true);
				cellID = agent1.getCellId();
				if (cellID != -1)
					simulator.mapIdCell.get(cellID).removeMoleculeInCell(agent2);

				// Incluir agente muerto
				simulator.deadAgents.add(agent2);
				// Comprobar si este agente necesita insertarse desde feeder
				// if (parent.parent.hasFeeder)
				// simulator.simulatorLogic.parent.feeder.checkAndIncrementMoleculeCreation(agent2.getName());
			}

			simulator.reactions++;

			toRet = 0;
		} else {
			toRet = 1;
		}

		return toRet;
	}

	@Override
	public boolean checkInputs(iMolecule... agents) {
		boolean toRet = false;
		List<String> auxList = new ArrayList<String>();
		String listAgentName, agentName;

		// Número de agentes tiene que ser igual al de la lista de entrada
		if (agents.length == inputAgents.size()) {
			auxList.addAll(inputAgents);

			for (int index = 0; index < agents.length; index++) {
				agentName = agents[index].getName();
				listAgentName = auxList.get(index);

				if (agentName.equals(listAgentName)) {
					toRet = true;
				} else {
					toRet = false;
					break;
				}
			}
		}

		return toRet;
	}

}
