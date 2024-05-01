package es.uvigo.ei.sing.singulator.modules.events;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import es.uvigo.ei.sing.singulator.interfaces.iEvent;
import es.uvigo.ei.sing.singulator.interfaces.iMolecule;
import es.uvigo.ei.sing.singulator.simulator.SINGulator_Logic;

public class EventManager implements Serializable {

	private static final long serialVersionUID = 1L;

	private Set<iEvent> setKillEvents;
	private Set<iEvent> setReactionEvents;
	private Set<iEvent> setTransformEvents;

	// Sets para comprobar rapidamente si existe el agente como input de alguno
	// de los eventos, así se comprueba si se puede llevaer a cabo o no
	private Set<String> setKillChecks;
	private Set<String> setTransformChecks;
	private Set<String> setReactionChecks;

	public EventManager() {
		setKillChecks = new HashSet<String>();
		setTransformChecks = new HashSet<String>();
		setReactionChecks = new HashSet<String>();

		setKillEvents = new HashSet<iEvent>();
		setReactionEvents = new HashSet<iEvent>();
		setTransformEvents = new HashSet<iEvent>();
	}

	public void addEvent(int typeOfEvent, String[] eventInfo, SINGulator_Logic parent) {
		if (typeOfEvent == 0) {
			// 0: input, 1: layer, 2: rebodundWiths
			setKillEvents.add(new Kill(eventInfo, parent));

			setKillChecks.add(eventInfo[0]);
		} else if (typeOfEvent == 1) {
			// 0: inputs, 1: outputs, 2: km, 3: kcat
			setReactionEvents.add(new Reaction(eventInfo, parent));

			for (String column : eventInfo[0].split("\t")) {
				setReactionChecks.add(column);
			}
		} else {
			// 0: input, 1: layer, 2: rebodundWiths
			setTransformEvents.add(new Transform(eventInfo, parent));

			setTransformChecks.add(eventInfo[0]);
		}
	}

	public boolean checkEvent(int typeOfEvent, iMolecule... agents) {
		boolean toRet = false;

		if (typeOfEvent == 0 && setKillChecks.contains(agents[0].getName())) {
			toRet = true;
		} else if (typeOfEvent == 2 && setTransformChecks.contains(agents[0].getName())) {
			toRet = true;
		} else if (typeOfEvent == 1) {
			if (setReactionChecks.contains(agents[0].getName()) && setReactionChecks.contains(agents[1].getName())) {
				toRet = true;
			} else {
				toRet = false;
			}
		}

		return toRet;
	}

	public int resolveEvent(int typeOfEvent, iMolecule... agents) {
		int toRet = 2;

		if (typeOfEvent == 0) {
			for (iEvent killEvent : setKillEvents) {
				if (killEvent.checkInputs(agents)) {
					toRet = killEvent.act(agents);
				} else {
					toRet = 2;
				}

				if (toRet == 0 || toRet == 1)
					break;
			}
		} else if (typeOfEvent == 2) {
			for (iEvent transformEvent : setTransformEvents) {
				if (transformEvent.checkInputs(agents)) {
					toRet = transformEvent.act(agents);
				} else {
					toRet = 2;
				}

				if (toRet == 0 || toRet == 1)
					break;
			}
		} else {
			for (iEvent reactionEvent : setReactionEvents) {
				if (reactionEvent.checkInputs(agents)) {
					toRet = reactionEvent.act(agents);
				} else {
					toRet = 2;
				}

				if (toRet == 0 || toRet == 1) {
					break;
				}
			}
		}

		return toRet;
	}
}
