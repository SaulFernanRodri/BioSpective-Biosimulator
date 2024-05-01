package es.uvigo.ei.sing.singulator.modules.distribution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import es.uvigo.ei.sing.singulator.agents.Door;
import es.uvigo.ei.sing.singulator.simulator.SINGulator_Model;
import sim.engine.SimState;

public class DoorHelper implements Callable<Void>, Serializable {
	private static final long serialVersionUID = 1L;

	private int extent;

	private List<Door> insertedDoors;

	private SimState state;

	public DoorHelper(int extent, SimState state) {
		this.extent = extent;
		this.state = state;
		this.insertedDoors = new ArrayList<Door>();
	}

	@Override
	public Void call() {
		SINGulator_Model state = (SINGulator_Model) this.state;

		for (Door door : insertedDoors)
			door.step(state);

		return null;
	}

	public int getExtent() {
		return extent;
	}

	public void insertDoor(Door door) {
		insertedDoors.add(door);
	}
}
