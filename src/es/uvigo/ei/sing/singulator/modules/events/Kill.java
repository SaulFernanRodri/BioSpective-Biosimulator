package es.uvigo.ei.sing.singulator.modules.events;

import java.io.Serializable;

import es.uvigo.ei.sing.singulator.constants.Constants;
import es.uvigo.ei.sing.singulator.interfaces.iEvent;
import es.uvigo.ei.sing.singulator.interfaces.iMolecule;
import es.uvigo.ei.sing.singulator.simulator.SINGulator_Logic;
import es.uvigo.ei.sing.singulator.simulator.SINGulator_Model;

public class Kill implements iEvent, Serializable {

	private static final long serialVersionUID = 1L;

	private String inputAgent;
	private String trigger;
	private String with;
	private int layer;

	private SINGulator_Logic parent;

	public Kill(String[] killEventInfo, SINGulator_Logic parent) {
		// 0: input, 1: trigger, 2: with
		this.inputAgent = killEventInfo[0];
		this.trigger = killEventInfo[1];
		this.with = killEventInfo[2];

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
		int cellID;

		// Si agents == 1, entras al metodo al entrar o rebotar contra una capa
		if (agents.length == 1) {
			if (agent.getCurrentZone() == layer || with.equals(agent.getLastReboundWith())) {
				// PCQUORUM DEPENDANCE
				if (agent.getLastReboundWith().equals("outer membrane")){
					agent.setTimeStepToZone(1, parent.parent.schedule.getSteps());
				}else if(agent.getLastReboundWith().equals("inner membrane")){
					agent.setTimeStepToZone(5, parent.parent.schedule.getSteps());
				}else if(agent.getLastReboundWith().equals("cytoplasm")){
					agent.setTimeStepToZone(6, parent.parent.schedule.getSteps());
				}

				cellID = agent.getCellId();
				if (cellID != -1)
					simulator.mapIdCell.get(cellID).removeMoleculeInCell(agent);
				// Stop molecules
				agent.putToStop(true);
				// Incluir agente muerto
				simulator.deadAgents.add(agent);

				// if (parent.parent.hasFeeder) {
				// // Comprobar si este agente necesita insertarse desde feeder
				// simulator.simulatorLogic.parent.feeder.checkAndIncrementMoleculeCreation(agent.getName());
				// }

				if (with.equals(Constants.EXTERIOR_NAME)
						&& agent.getLastReboundWith().equals(Constants.EXTERIOR_NAME)) {
					// Increase counter of escaped molecules
					simulator.escapedAgents++;
				}

				// PCQUORUM DEPENDANCE
				if (agent.getName().equals("Macromoleculeahl")) {
					simulator.mapIDLastRebound.put(agent.getId(), agent.getLastReboundWith());
					if (agent.getLastReboundWith() != "exterior") {
						parent.parent.getcreatedCells().get(agent.getCellId()).setConsumed();
					}
				} else if (agent.getName().equals("Macromoleculeahl2")) {
					simulator.mapIDLastReboundEnvironment.put(agent.getId(), agent.getLastReboundWith());

					//ONLY FOR DUAL FEEDING
					if (agent.getLastReboundWith() != "exterior") {
						parent.parent.getcreatedCells().get(agent.getCellId()).setConsumed();
					}
				}

				toRet = 0;
			} else {
				toRet = 2;
			}
		}
		// Si > 1, entras al rebotar contra otro agente
		else {
			if (with.equals(agents[1].getName())) {
				cellID = agent.getCellId();
				if (cellID != -1)
					simulator.mapIdCell.get(cellID).removeMoleculeInCell(agent);
				// Stop molecules
				agent.putToStop(true);
				// Incluir agente muerto
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

		return toRet;
	}

	@Override
	public boolean checkInputs(iMolecule... agents) {
		return inputAgent.equals(agents[0].getName());
	}
}
