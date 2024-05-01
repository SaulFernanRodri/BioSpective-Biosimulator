package es.uvigo.ei.sing.singulator.modules.distribution;

import java.util.Collection;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import es.uvigo.ei.sing.singulator.agents.Cell;
import es.uvigo.ei.sing.singulator.constants.Constants;
import es.uvigo.ei.sing.singulator.interfaces.iMolecule;
import es.uvigo.ei.sing.singulator.modules.physics.PhysicsEngine;
import es.uvigo.ei.sing.singulator.simulator.SINGulator_Model;
import sim.util.Bag;
import sim.util.Double3D;

public class CreateMoleculeHelper implements Runnable {

	// Integer variables
	private int tries;
	private int totalTries;
	private AtomicInteger totalAgents;

	// Double variables
	private double minWidth;
	private double maxWidth;
	private double minLength;
	private double maxLength;
	private double minHeigth;
	private double maxHeigth;

	// Boolean variables
	private boolean agentInserted;

	// Collection variables
	private Stack<iMolecule> toInsert;
	private Collection<Cell> totalCells;
	private Set<Cell> currentCells; // Cells in this extent

	// Complex variables
	private SINGulator_Model model;

	public CreateMoleculeHelper(double minWidth, double maxWidth, double minHeigth, double maxHeigth, double minLength,
			double maxLength, int extent, Stack<iMolecule> toInsert, Collection<Cell> totalCells, int tries,
			AtomicInteger totalAgents, SINGulator_Model model) {
		this.minWidth = minWidth;
		this.maxWidth = maxWidth;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.minHeigth = minHeigth;
		this.maxHeigth = maxHeigth;
		this.toInsert = toInsert;
		this.totalCells = totalCells;
		this.model = model;
		this.totalTries = tries;
		this.tries = tries;
		this.totalAgents = totalAgents;
		this.agentInserted = false;

		this.currentCells = new HashSet<Cell>();
		for (Cell cell : totalCells) {
			if (cell.getExtent() == extent) {
				currentCells.add(cell);
			}
		}
	}

	@Override
	public void run() {
		calculateValidLocation();
	}

	private void calculateValidLocation() {
		Double3D location = null;
		// exterior, cellA, cellB...
		String agentZoneLocalization;
		// 0, 1...6
		int agentLayerLocalization;
		double radius;
		iMolecule molecule = null;
		boolean canSpawn = false;
		Cell currentCell = null;
		Object[] ret;
		String position;

		while (totalAgents.get() > 0 && tries > 0) {
			try {
				molecule = toInsert.pop();

				radius = molecule.getRadius();
				agentZoneLocalization = molecule.retrieveInitLocalization();
				// TODO: Si no estas en el exterior no puede ser 0
				agentLayerLocalization = molecule.getCurrentZone();

				// Comprobar donde crear la molecula
				if (!agentZoneLocalization.equals(Constants.EXTERIOR_NAME)) {
					for (Cell cell : currentCells) {
						// TODO: CREAR SOLO LAS MOLECULAS NECESARIAS EN CADA
						// CELULA
						if (agentZoneLocalization.equals(cell.getCellName())) {
							currentCell = cell;
							break;
						}
					}
				}

				if (currentCell != null || agentZoneLocalization.equals(Constants.EXTERIOR_NAME)) {
					do {
						// Dentro de celula o en el exterior
						if (currentCell != null) {
							if (currentCell.getForm().equals(Constants.CAPSULE)) {
								// Solo hay una capa o es la mas interna
								if (agentLayerLocalization == currentCell.getLastLayerNumber()) {
									ret = PhysicsEngine.calculateRandomLocationInsideLayerCapsule(model.random,
											currentCell.getLocation(),
											currentCell.getLayer(agentLayerLocalization).getHeight(),
											currentCell.getHorizontalAngle(), currentCell.getVerticalAngle(),
											currentCell.getLayer(agentLayerLocalization).getRadius(), radius);
									
									location = (Double3D) ret[0];
									position = (String) ret[1];
								} else {
									ret = PhysicsEngine.calculateRandomLocationBetweenLayersCapsule(model.random,
											currentCell.getLocation(),
											currentCell.getNextLayer(agentLayerLocalization + 1).getHeight(),
											currentCell.getHorizontalAngle(), currentCell.getVerticalAngle(),
											currentCell.getNextLayer(agentLayerLocalization + 1).getRadius(),
											currentCell.getLayer(agentLayerLocalization).getRadius(), radius);
									
									location = (Double3D) ret[0];
									position = (String) ret[1];
								}
							} else if (currentCell.getForm().equals(Constants.SPHERE)
									|| currentCell.getForm().equals(Constants.HEMISPHERE)) {
								if (agentLayerLocalization == currentCell.getLastLayerNumber()) {
									location = PhysicsEngine.calculateRandomLocationInsideLayerSphere(model.random,
											currentCell.getLocation(),
											currentCell.getLayer(agentLayerLocalization).getRadius(), radius);
								} else {
									location = PhysicsEngine.calculateRandomLocationBetweenLayersSphere(model.random,
											currentCell.getLocation(),
											currentCell.getNextLayer(agentLayerLocalization + 1).getRadius(),
											currentCell.getLayer(agentLayerLocalization).getRadius(), radius);
								}
							}
						} else {
							location = PhysicsEngine.calculateRandomPositionInEnvironmentForSphere(model.random,
									minWidth, maxWidth, minHeigth, maxHeigth, minLength, maxLength, radius);
						}

						// Comprobar posición
						canSpawn = canSpawn(molecule, currentCell, location, model);

						if (!canSpawn)
							tries--;
					} while (!canSpawn && tries > 0);

					if (canSpawn) {
						setAgentInTheEnviroment(molecule, location, model);
						totalAgents.decrementAndGet();

						if (currentCell != null) {
							molecule.setCellId(currentCell.getId());
							currentCell.addMoleculeToCell(molecule);
						}

						// Reiniciar intentos
						tries = totalTries;
					} else {
						// Return object to stack and stop the thread
						toInsert.push(molecule);
						break;
					}
				} else {
					// Return object to stack
					toInsert.push(molecule);
					// Decrement tries
					tries--;
				}

				// Clear variables
				currentCell = null;
				canSpawn = false;
			} catch (EmptyStackException e) {
			}
		}
	}

	private boolean canSpawn(final iMolecule agent, final Cell currentCell, final Double3D loc,
			SINGulator_Model state) {
		Double3D anotherLocation;
		boolean crash = false;
		boolean canSpawn = true;
		double radius = agent.getRadius();

		if (PhysicsEngine.moleculeCanSpawnInEnvironment(loc, radius, minWidth, maxWidth, minHeigth, maxHeigth,
				minLength, maxLength)) {
			// Comprobar contra las mas externas solamente si spawnea en el
			// exterior
			if (agent.getCurrentZone() == 0) {
				for (Cell cell : totalCells) {
					// Validate if the molecules has crashed.
					canSpawn = !PhysicsEngine.spawnMoleculeWithLayer(agent.getRadius(), loc, cell.getRadius(),
							cell.getH1Center(), cell.getH2Center(), cell.getForm(), cell.getLocation());

					if (!canSpawn) {
						break;
					}
				}
			} else {
				// Comprobar que puede entrar en la celula
				// Esta en ultima capa o solo existe una capa
				if (agent.getCurrentZone() == currentCell.getLastLayerNumber()) {
					canSpawn = PhysicsEngine.canSpawnMoleculeInsideLayer(radius,
							currentCell.getLayer(agent.getCurrentZone()).getRadius());
				} else {
					// Esta entre varias capas
					canSpawn = PhysicsEngine.canSpawnMoleculeBetweenLayers(radius,
							currentCell.getNextLayer(agent.getCurrentZone() + 1).getRadius(),
							currentCell.getLayer(agent.getCurrentZone()).getRadius());
				}
			}

			if (!agentInserted && canSpawn) {
				canSpawn = true;
			} else if (agentInserted && canSpawn) {
				Bag possibleMolecules = state.environment.getNeighborsWithinDistance(loc, agent.getRadius() * 2, false,
						true);

				// Go over neighbors
				iMolecule anotherAgent;
				for (Object obj : possibleMolecules) {
					if (obj != null && obj != agent && obj instanceof iMolecule) {
						anotherAgent = (iMolecule) obj;
						// Get molecule location in the partition
						anotherLocation = anotherAgent.getCurrentLocation();

						// Validate if the molecules has crashed
						crash = PhysicsEngine.checkCollisionBetweenSpheres(agent.getRadius(), loc,
								anotherAgent.getRadius(), anotherLocation);

						// If the molecule crash with another molecule in the
						// board,
						// return false and indicate that molecule can't spawn
						if (crash) {
							this.tries--;
							canSpawn = false;
						}
					}
				}
			}
		} else {
			canSpawn = false;
		}

		return canSpawn;
	}

	private void setAgentInTheEnviroment(iMolecule molecule, Double3D location, SINGulator_Model state) {
		// Need to be synchronized for Mason. Although, each agent put
		// molecules in its own space.
		synchronized (state) {
			// Put in the layer
			state.environment.setObjectLocation(molecule, location);
			molecule.setInitialPostion(location);
			molecule.setFinalPostion(location);
			molecule.setCurrentLocation(location);
			molecule.setDesiredLocation(location);
		}

		molecule.setCurrentLocation(location);

		String name = molecule.getName();
		// Need to be synchronized. Avoid dirty reads.
		synchronized (state) {
			// Put molecule number in the map
			Integer number = state.mapNameNumber.get(name);
			state.mapNameNumber.put(name, number + 1);

			int zone = molecule.getCurrentZone();
			// Put molecules in layer number zone
			number = state.mapLayerMolecules.get(zone);
			if (number == null) {
				state.mapLayerMolecules.put(zone, 1);
			} else {
				state.mapLayerMolecules.put(zone, number + 1);
			}
		}

		agentInserted = true;
	}
}
