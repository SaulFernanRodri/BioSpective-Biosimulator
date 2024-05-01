package es.uvigo.ei.sing.singulator.modules.distribution;

import java.util.EmptyStackException;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import es.uvigo.ei.sing.singulator.agents.Cell;
import es.uvigo.ei.sing.singulator.agents.Door;
import es.uvigo.ei.sing.singulator.interfaces.iLayer;
import es.uvigo.ei.sing.singulator.modules.physics.PhysicsEngine;
import es.uvigo.ei.sing.singulator.simulator.SINGulator_Model;
import sim.util.Bag;
import sim.util.Double3D;

public class CreateDoorHelper implements Runnable {

	// Integer variables
	private int tries;
	private int totalTries;
	private AtomicInteger totalAgents;

	// Double variables
	// private double minWidth;
	// private double maxWidth;
	// private double minLength;
	// private double maxLength;
	// private double minHeigth;
	// private double maxHeigth;

	// Boolean variables
	private boolean agentInserted;

	// Collection variables
	private Stack<Door> toInsert;

	// Complex variables
	private Cell currentCell;
	private SINGulator_Model model;

	public CreateDoorHelper(double minWidth, double maxWidth, double minHeigth, double maxHeigth, double minLength,
			double maxLength, Stack<Door> toInsert, Cell currentCell, int tries, AtomicInteger totalAgents,
			SINGulator_Model model) {
		// this.minWidth = minWidth;
		// this.maxWidth = maxWidth;
		// this.minLength = minLength;
		// this.maxLength = maxLength;
		// this.minHeigth = minHeigth;
		// this.maxHeigth = maxHeigth;

		this.toInsert = toInsert;
		this.currentCell = currentCell;
		this.model = model;
		this.totalTries = tries;
		this.tries = tries;
		this.totalAgents = totalAgents;
		this.agentInserted = false;
	}

	@Override
	public void run() {
		calcualteValidLocation();
	}

	private void calcualteValidLocation() {
		Double3D locationInterior = null;
		Double3D locationExterior = null;
		Door door = null;
		boolean canSpawn = false;
		iLayer interiorLayer, exteriorLayer;

		while (totalAgents.get() > 0 && tries > 0) {
			try {
				door = toInsert.pop();

				do {
					double random;
					double verticalAngle;
					double horizontalAngle;
					// Random between [0, 1]
					synchronized (model.random) {
						random = model.random.nextDouble(true, true);
						verticalAngle = (model.random.nextInt(360));
						horizontalAngle = (model.random.nextInt(360));
					}

					// TODO: LA CAPA INTERNA NO PUEDE SER MENOR QUE LA
					// EXTERNA
					interiorLayer = currentCell.getLayer(door.getInteriorZone());
					Double3D cellLocation = interiorLayer.getLocation();
					locationInterior = PhysicsEngine.calculateDoorLocation(interiorLayer.getScale(),
							interiorLayer.getRadius(), interiorLayer.getH1Min(), interiorLayer.getH1Center(),
							interiorLayer.getH2Center(), cellLocation.y, cellLocation.z, random, verticalAngle,
							horizontalAngle);

					exteriorLayer = currentCell.getLayer(door.getExteriorZone());
					cellLocation = exteriorLayer.getLocation();
					locationExterior = PhysicsEngine.calculateDoorLocation(exteriorLayer.getScale(),
							exteriorLayer.getRadius(), exteriorLayer.getH1Min(), exteriorLayer.getH1Center(),
							exteriorLayer.getH2Center(), cellLocation.y, cellLocation.z, random, verticalAngle,
							horizontalAngle);

					canSpawn = canSpawn(door, locationInterior, locationExterior, interiorLayer, exteriorLayer, model);

					if (!canSpawn)
						tries--;

					door.setHorizontalAngle(horizontalAngle);
					door.setVerticalAngle(verticalAngle);
				} while (!canSpawn && tries > 0);

				if (canSpawn) {
					setAgentInTheEnviroment(door, locationInterior, locationExterior, model);
					totalAgents.decrementAndGet();

					// Reiniciar intentos
					tries = totalTries;
				} else {
					// Return object to stack and stop the thread
					toInsert.push(door);
					break;
				}

				// Clear variables
				canSpawn = false;
			} catch (EmptyStackException e) {
			}
		}
	}

	private boolean canSpawn(final Door agent, final Double3D locInt, Double3D locExt, final iLayer interiorLayer,
			final iLayer exteriorLayer, SINGulator_Model state) {
		boolean toRet = false;
		Double3D anotherLocation;
		Double3D exteriorLocation;
		boolean crash = false;
		boolean canSpawn = true;

		if (PhysicsEngine.checkDoorLocationsInsideCapsule(locInt, locExt, interiorLayer.getLocation(),
				exteriorLayer.getLocation(), interiorLayer.getH1Center(), interiorLayer.getH2Center(),
				exteriorLayer.getH1Center(), exteriorLayer.getH2Center())) {
			if (!agentInserted && canSpawn) {
				toRet = true;
			} else if (agentInserted && canSpawn) {
				double radiusExt = agent.getRadius();
				double radiusInt = agent.getRadius();

				Bag possibleDoors = state.environment.getNeighborsWithinDistance(locInt, radiusInt * 2, false, true);
				Bag possibleDoorsExterior = state.environment.getNeighborsWithinDistance(locExt, radiusExt * 2, false,
						true);
				possibleDoors.addAll(possibleDoorsExterior);

				// Go over neighbors
				for (Object obj : possibleDoors) {
					if (obj != null && obj != agent && obj instanceof Door) {
						Door anotherAgent = (Door) obj;

						try {
							if (agent != null && anotherAgent != agent) {
								// Door interior location
								anotherLocation = anotherAgent.getInteriorLocation();
								// Door exterior location
								exteriorLocation = anotherAgent.getExteriorLocation();

								// Compare only doors in the same zone
								if (agent.getExteriorZone() == anotherAgent.getExteriorZone()) {
									crash = PhysicsEngine.checkCollisionBetweenSpheres(agent.getRadius(), locExt,
											anotherAgent.getRadius(), exteriorLocation);
								}
								if (!crash && agent.getInteriorZone() == anotherAgent.getInteriorZone()) {
									crash = PhysicsEngine.checkCollisionBetweenSpheres(agent.getRadius(), locInt,
											anotherAgent.getRadius(), anotherLocation);
								}
								if (crash) {
									toRet = false;
									break;
								}
							}
						} catch (Exception e) {
						}
					}
				}
				// Si no choca contra otra puerta
				if (!crash)
					toRet = true;

			}
		}

		return toRet;
	}

	private void setAgentInTheEnviroment(Door door, Double3D locationInterior, Double3D locationExterior,
			SINGulator_Model state) {
		String name = door.getName();
		Double3D cellCenter = currentCell.getLocation();

		synchronized (state) {
			state.environment.setObjectLocation(door, locationExterior);
			state.environment.setObjectLocation(door.getInterior(), locationInterior);
		}

		door.setInteriorLocation(locationInterior, cellCenter);
		door.setExteriorLocation(locationExterior, cellCenter);
		door.setCellCenterLocation(cellCenter);
		door.setCellId(currentCell.getId());

		currentCell.putMapNameDoors(name, door);
		currentCell.increaseMapNameDoorCount(name);

		agentInserted = true;
	}
}
