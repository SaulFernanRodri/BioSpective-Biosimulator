package es.uvigo.ei.sing.singulator.simulator;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import es.uvigo.ei.sing.singulator.agents.Cell;
import es.uvigo.ei.sing.singulator.agents.Door;
import es.uvigo.ei.sing.singulator.agents.Feeder;
import es.uvigo.ei.sing.singulator.constants.Constants;
import es.uvigo.ei.sing.singulator.interfaces.iLayer;
import es.uvigo.ei.sing.singulator.interfaces.iMolecule;
import es.uvigo.ei.sing.singulator.modules.events.EventManager;
import es.uvigo.ei.sing.singulator.modules.physics.PhysicsEngine;
import es.uvigo.ei.sing.singulator.modules.physics.Vector3D;
import es.uvigo.ei.sing.singulator.utils.Functions;
import sim.util.Bag;
import sim.util.Double3D;

public class SINGulator_Logic implements Serializable {

	private static final long serialVersionUID = 1L;

	public SINGulator_Model parent;

	protected SINGulator_Initialize initialize;
	public AtomicInteger agentID = new AtomicInteger(0);

	public EventManager eventManager;

	public SINGulator_Logic(SINGulator_Model parent) {
		this.parent = parent;
		this.initialize = new SINGulator_Initialize(this);

		this.eventManager = new EventManager();
	}

	public Stack<iMolecule> extractMoleculeInformation(Map<String, String[]> mapNameInformation) {
		return initialize.extractMoleculeInformation(mapNameInformation, agentID);
	}

	public Stack<iMolecule> extractRibosomeInformation(Map<String, String[]> mapNameInformation) {
		return initialize.extractRibosomeInformation(mapNameInformation, agentID);
	}

	public void loadConfigurationFile(Stack<iMolecule> molecules) throws IOException {
		Stack<Cell> cells;
		Map<Cell, List<Door>> mapCellDoors;

		// Get layer information
		if (parent.hasCells) {
			cells = initialize.extractCellInformation();

			initialize.distributeCells(cells);
		}
		// Get door information
		if (parent.hasTransporters) {
			mapCellDoors = initialize.extractDoorInformation(agentID);
			// toDistribute.addAll(initialize.extractDoorInformation(layersMap,
			// agentID));
			initialize.distributeDoors(mapCellDoors);
		}

		// Distribute agent creation in threads
		initialize.distributeMolecules(molecules, parent.mapIdCell.values());
	}

	public void loadEventsFile() throws IOException {
		List<String[]> events;

		if (parent.hasEvents) {
			if (parent.singulator.getEvents().getKill() != null) {
				// Extract kill events
				events = initialize.extractKillEvents();
				for (String[] killEvenInfo : events) {
					// Insert kill events
					eventManager.addEvent(0, killEvenInfo, this);
				}
			}

			if (parent.singulator.getEvents().getReaction() != null) {
				// Extract reaction events
				events = initialize.extractReactionEvents();
				for (String[] reactionEventInfo : events) {
					// Insert reaction events
					eventManager.addEvent(1, reactionEventInfo, this);
				}
			}

			if (parent.singulator.getEvents().getTransform() != null) {
				// Extract transform events
				events = initialize.extractTranformEvents();
				for (String[] transformEventInfo : events) {
					// Insert transform events
					eventManager.addEvent(2, transformEventInfo, this);
				}
			}
		}
	}

	public List<Feeder> loadFeederFromFile() {
		return initialize.extractFeederFromInformation();
	}

	public void executeStepDoor(Door door) {
		iMolecule moleculeInterior = null;
		iMolecule moleculeExterior = null;
		Vector3D moleculeInteriorV = null;
		Vector3D moleculeExteriorV = null;
		Map<iMolecule, Vector3D> interior = new HashMap<iMolecule, Vector3D>();
		Map<iMolecule, Vector3D> exterior = new HashMap<iMolecule, Vector3D>();
		List<String> toGetList = new ArrayList<String>();
		toGetList.addAll(door.getListMoleculeToGet());
		List<String> toExpulseList = new ArrayList<String>();
		toExpulseList.addAll(door.getListMoleculeToExpulse());
		int molID;
		boolean deactivate = true;
		String doorType = door.getType();
		Double3D cellCenter = door.getCellCenterLocation();
		Double3D doorExteriorLoc = door.getExteriorLocation();
		Double3D doorInteriorLoc = door.getInteriorLocation();

		Bag bag = parent.environment.getNeighborsWithinDistance(door.getInteriorLocation(), door.getRadius() * 2, false,
				true);
		Bag bagExterior = parent.environment.getNeighborsWithinDistance(door.getExteriorLocation(),
				door.getRadius() * 2, false, true);
		bag.addAll(bagExterior);

		// Only for molecules
		for (Object obj : bag) {
			if (obj instanceof iMolecule) {
				iMolecule mol = (iMolecule) obj;

				molID = mol.getId();
				// Las puertas solo pueden tratar a las moléculas que estén
				// libres y a las que estén en la propia puerta. Con esto
				// evitamos que una puerta pueda quitar el isInDoor de otra
				// puerta diferente.
				if (mol.getDesiredLocation() != null && (door.getId() == mol.getDoorID() || mol.getDoorID() == -1)) {
					// 0: capa externa a capa interna, 3: capa interna a capa
					// externa
					int collision = PhysicsEngine.checkCollisionWithDoor(mol.getRadius(), mol.isInDoor(),
							mol.getDesiredLocation(), door.getRadius(), door.getMinDistanceToCenter(),
							door.getMaxDistanceToCenter(), door.getVerticalAngle(), door.getHorizontalAngle(),
							door.getInteriorLocation(), door.getExteriorLocation(), cellCenter);

					if (collision != -1) {
						if (doorType.equalsIgnoreCase(Constants.UNIPORTER)
								|| doorType.equalsIgnoreCase(Constants.SYMPORTER)) {
							if ((collision == 0 || collision == 3) && mol.getCurrentZone() == door.getToGetZone()
									&& (!door.isBusyInterior() && !door.isBusyExterior() || door.isMoleculeID(molID))) {
								if (toGetList.contains(mol.getName())) {
									moleculeInteriorV = new Vector3D(doorInteriorLoc.x - doorExteriorLoc.x,
											doorInteriorLoc.y - doorExteriorLoc.y,
											doorInteriorLoc.z - doorExteriorLoc.z);
									moleculeInteriorV.normalize();
									moleculeInteriorV.mult(door.getDiffusionRate());

									// Save interior molecule
									moleculeInterior = mol;

									// Stop the molecule until other edge is
									// busy
									moleculeInterior.setSpeed(new Vector3D(0, 0, 0));

									// Set position in the middle of the door
									synchronized (parent.environment) {
										if (collision == 0) {
											parent.environment.setObjectLocation(moleculeInterior,
													door.getExteriorLocation());
										} else {
											parent.environment.setObjectLocation(moleculeInterior,
													door.getInteriorLocation());
										}
									}

									moleculeInterior.setUnstoppable(true);
									moleculeInterior.setInDoor(true);
									moleculeInterior.setDoorID(door.getId());
									moleculeInterior.setDoorName(door.getName());

									interior.put(moleculeInterior, moleculeInteriorV);

									toGetList.remove(moleculeInterior.getName());
									// Ya obtuvo todas las moleculas necesarias
									if (toGetList.isEmpty()) {
										if (collision == 0) {
											door.setBusyExterior(true);
										} else {
											door.setBusyInterior(true);
										}
									}

									door.addMolID(molID);

									deactivate = false;
								}
							}
						}
						// ANTIPORTER: En get poner la capa más interna siempre!
						else if (doorType.equalsIgnoreCase(Constants.ANTIPORTER)) {
							if (collision == 0 && mol.getCurrentZone() == door.getToOutputZone()
									&& (!door.isBusyExterior() || door.isMoleculeID(molID))) {
								if (toExpulseList.contains(mol.getName())) {
									// Save interior speed
									moleculeExteriorV = new Vector3D(doorInteriorLoc.x - doorExteriorLoc.x,
											doorInteriorLoc.y - doorExteriorLoc.y,
											doorInteriorLoc.z - doorExteriorLoc.z);
									moleculeExteriorV.normalize();
									moleculeExteriorV.mult(door.getDiffusionRate());

									// Save interior molecule
									moleculeExterior = mol;

									// Stop the molecule until other edge is
									// busy
									moleculeExterior.setSpeed(new Vector3D(0, 0, 0));

									// Set position in the middle of the door
									synchronized (parent.environment) {
										parent.environment.setObjectLocation(moleculeExterior,
												door.getExteriorLocation());
									}

									moleculeExterior.setUnstoppable(true);
									moleculeExterior.setInDoor(true);
									moleculeExterior.setDoorID(door.getId());
									moleculeExterior.setDoorName(door.getName());

									exterior.put(moleculeExterior, moleculeExteriorV);

									toExpulseList.remove(moleculeExterior.getName());
									// Ya obtuvo todas las moleculas necesarias
									if (toExpulseList.isEmpty()) {
										door.setBusyExterior(true);
									}

									door.addMolID(molID);

									deactivate = false;
								}
							} else if (collision == 3 && mol.getCurrentZone() == door.getToGetZone()
									&& (!door.isBusyInterior() || door.isMoleculeID(molID))) {
								// PARTE EXTERNA (manda a capas internas)
								if (toGetList.contains(mol.getName())) {
									// Save interior speed
									moleculeInteriorV = new Vector3D(doorInteriorLoc.x - doorExteriorLoc.x,
											doorInteriorLoc.y - doorExteriorLoc.y,
											doorInteriorLoc.z - doorExteriorLoc.z);
									moleculeInteriorV.normalize();
									moleculeInteriorV.mult(door.getDiffusionRate());

									// Save interior molecule
									moleculeInterior = mol;

									// Stop the molecule until other edge is
									// busy
									moleculeInterior.setSpeed(new Vector3D(0, 0, 0));

									// Set position in the middle of the door
									synchronized (parent.environment) {
										parent.environment.setObjectLocation(moleculeInterior,
												door.getInteriorLocation());
									}

									moleculeInterior.setUnstoppable(true);
									moleculeInterior.setInDoor(true);
									moleculeInterior.setDoorID(door.getId());
									moleculeInterior.setDoorName(door.getName());

									interior.put(moleculeInterior, moleculeInteriorV);

									toGetList.remove(moleculeInterior.getName());
									// Ya obtuvo todas las moleculas necesarias
									if (toGetList.isEmpty()) {
										door.setBusyInterior(true);
									}

									door.addMolID(molID);

									deactivate = false;
								}
							}
						}

						if (collision == 2) {
							// Collision = 2 during molecule transition
							// throuhg the door
							if (mol.isInDoor()) {
								deactivate = false;
							} else {
								// Collision out door to in door on the middle
								mol.setNegativeSpeed();
							}
						}
					} else {
						if (collision == -1 || collision == 1) {
							mol.setInDoor(false);
							mol.setDoorID(-1);
							mol.setDoorName("");

							// Las puertas tienen una lista con IDs de
							// moleculas para saber con cuales estan
							// tratando. Una vez las moleculas acaban de
							// transitar, el id se borra de la lista.
							if (door.isMoleculeID(molID)) {
								door.removeMolID(molID);
							}
						}
					}
				}
			}
		}

		if (deactivate && door.canTransportMolecules()) {
			door.setBusyExterior(false);
			door.setBusyInterior(false);
		} else {
			if (doorType.equalsIgnoreCase(Constants.UNIPORTER) || doorType.equalsIgnoreCase(Constants.SYMPORTER)) {
				// Reusar variable para indicar que esta ocupada
				if (door.isBusyInterior() && !interior.isEmpty()) {
					for (iMolecule mol : interior.keySet()) {
						mol.setSpeed(Vector3D.mult(interior.get(mol), -1));
						mol.setMaxLayer(door.getToOutputZone());
						mol.setMinLayer(door.getToOutputZone());
						mol.setCurrentZone(door.getToOutputZone());
						mol.setCurrentDiffusionRate(door.getDiffusionRate());
					}
				} else if (door.isBusyExterior() && !interior.isEmpty()) {
					for (iMolecule mol : interior.keySet()) {
						mol.setSpeed(interior.get(mol));
						mol.setMaxLayer(door.getToOutputZone());
						mol.setMinLayer(door.getToOutputZone());
						mol.setCurrentZone(door.getToOutputZone());
						mol.setCurrentDiffusionRate(door.getDiffusionRate());
					}
				}
			} else if (doorType.equalsIgnoreCase(Constants.ANTIPORTER)) {
				if (door.isBusyExterior() && door.isBusyInterior() && !interior.isEmpty() && !exterior.isEmpty()) {
					for (iMolecule mol : exterior.keySet()) {
						mol.setSpeed(exterior.get(mol));
						mol.setMaxLayer(door.getToGetZone());
						mol.setMinLayer(door.getToGetZone());
						mol.setCurrentZone(door.getToGetZone());
						mol.setCurrentDiffusionRate(door.getDiffusionRate());
					}
					for (iMolecule mol : interior.keySet()) {
						mol.setSpeed(Vector3D.mult(interior.get(mol), -1));
						mol.setMaxLayer(door.getToOutputZone());
						mol.setMinLayer(door.getToOutputZone());
						mol.setCurrentZone(door.getToOutputZone());
						mol.setCurrentDiffusionRate(door.getDiffusionRate());
					}
				}
			}
		}
	}

	public boolean acceptablePosition1(iMolecule mol) {
		boolean acceptablePosition = false;
		Double3D desiredLocation = mol.getDesiredLocation();
		Cell currentCell;
		// 0: Exito, 2: No se pudo llevar a cabo
		int resolved = 2;

		// Check inside board
		acceptablePosition = Functions.insideBoard(mol, parent);

		if (acceptablePosition) {
			if (!mol.isInDoor() && parent.hasCells) {
				// TODO: REVISAR. Si no esta en la celula no va a estar en
				// ninguna capa ya.
				currentCell = parent.mapIdCell.get(mol.getCellId());
				if (currentCell != null) {
					int toGo = 0;
					// From inner cell to outer
					Set<Integer> keys = currentCell.getMapZoneLayers().descendingKeySet();
					iLayer layer;
					for (int zone : keys) {
						layer = currentCell.getLayer(zone);
						toGo = PhysicsEngine.checkCollisionWithLayer(mol.getRadius(),
								parent.environment.getObjectLocation(mol), desiredLocation, layer.getForm(),
								layer.getRadius(), layer.getH1Center(), layer.getH2Center(), layer.getLocation());

						if (toGo != 0) {
							acceptablePosition = PhysicsEngine.hasToReboundWithLayer(mol.getMaxLayer(),
									mol.getMinLayer(), zone, toGo);

							if (!acceptablePosition) {
								mol.setNegativeSpeed();
								mol.setLastReboundWith(Constants.mapLayerIdLayerName.get(zone));

								if (parent.hasEvents) {
									// Comprobar eventos (kill y/o transform)
									if (eventManager.checkEvent(0, mol)) {
										resolved = eventManager.resolveEvent(0, mol);
									}
									if (eventManager.checkEvent(2, mol) && resolved == 2) {
										resolved = eventManager.resolveEvent(2, mol);
									}
								}
							}

							break;
						}
					}
				} else {
					// Si estas en capa 0 verificas contra capa 1
					int toGo = 0;
					for (Cell cell : parent.mapIdCell.values()) {
						toGo = PhysicsEngine.checkCollisionWithLayer(mol.getRadius(),
								parent.environment.getObjectLocation(mol), desiredLocation, cell.getForm(),
								cell.getRadius(), cell.getH1Center(), cell.getH2Center(), cell.getLocation());

						if (toGo != 0) {
							acceptablePosition = PhysicsEngine.hasToReboundWithLayer(mol.getMaxLayer(),
									mol.getMinLayer(), 1, toGo);

							if (!acceptablePosition) {
								mol.setNegativeSpeed();
								mol.setLastReboundWith(Constants.mapLayerIdLayerName.get(cell.getZoneID()));

								if (parent.hasEvents) {
									// Comprobar eventos (kill y/o transform)
									if (eventManager.checkEvent(0, mol)) {
										resolved = eventManager.resolveEvent(0, mol);
									}
									if (eventManager.checkEvent(2, mol) && resolved == 2) {
										resolved = eventManager.resolveEvent(2, mol);
									}
								}
							}

							break;
						}
					}
				}
			}
		} else {
			mol.setHasCrashWihtEnvironment(true);
			mol.setLastReboundWith(Constants.EXTERIOR_NAME);

			if (parent.hasEvents) {
				// Comprobar eventos (kill y/o transform)
				if (eventManager.checkEvent(0, mol)) {
					resolved = eventManager.resolveEvent(0, mol);
				}
				if (eventManager.checkEvent(2, mol) && resolved == 2) {
					resolved = eventManager.resolveEvent(2, mol);
				}
			}
		}

		return acceptablePosition;
	}

	// 0: false, 1: true, 2: insertar en stack de nuevo (ceder)
	public int acceptablePosition2(iMolecule mol, int rootID, int extent) {
		int toRet = 1;

		Double3D desiredLocation = mol.getDesiredLocation();
		int tries = parent.availableProcessors;

		iMolecule ta = null;
		iMolecule toRebound = null;
		Double3D currentLocation = mol.getCurrentLocation();
		Double3D anotherLocation;
		boolean crash;
		// 0: success, 1: failure (only reactions), 2: cannot start event
		int resolveCollision;
		int molRadInfl = mol.getRadInfl();
		Bag bag;

		// TODO: Verificar el extent que coincida con el tuyo
		if (molRadInfl == 1 || mol.isUnstoppable()) {
			bag = parent.environment.getNeighborsWithinDistance(desiredLocation, mol.getRadius() * 2, false, true);
		} else {
			bag = parent.environment.getNeighborsWithinDistance(desiredLocation, mol.getRadius() * molRadInfl, false,
					true);
		}

		Queue<Object> mysteriousObjects;
		if (!bag.isEmpty()) {
			mysteriousObjects = new ArrayBlockingQueue<Object>(bag.size());
		} else {
			mysteriousObjects = new ArrayBlockingQueue<Object>(1);
		}

		// If the molecule has neighbors
		if (bag != null) {
			mysteriousObjects.addAll(bag);

			Object obj;
			while (!mysteriousObjects.isEmpty() && tries > 0) {
				resolveCollision = 2;
				obj = mysteriousObjects.poll();

				if (obj != null && obj != mol && obj instanceof iMolecule) {
					ta = (iMolecule) obj;

					if (ta.tryLock()) {
						// anotherLocation = ta.calculateDesiredWithSpeed(
						// parent.environment.getObjectLocation(ta),
						// ta.getSpeed());
						anotherLocation = ta.getDesiredLocation();

						if (!ta.isToStop()) {
							if (mol.isToStop()) {
								ta.unlock();
								break;
							}

							// Validar con radio de influencia
							if (mol.getRadInflWith().equals(ta.getName()) && !mol.isUnstoppable()) {
								crash = PhysicsEngine.checkCollisionBetweenSpheres(mol.getRadius() * molRadInfl,
										desiredLocation, ta.getRadius(), anotherLocation);
							} else {
								crash = PhysicsEngine.checkCollisionBetweenSpheres(mol.getRadius(), desiredLocation,
										ta.getRadius(), anotherLocation);
							}

							// Si soy unstoppable y choco, no tengo posición
							// libre
							if (mol.isUnstoppable() && crash) {
								toRet = 0;
								ta.unlock();
								break;
							} else if (crash && !ta.isInDoor() && !ta.isUnstoppable()) {
								if (ta.isReaction()) {
									if (toRebound != null && toRebound != ta) {
										toRebound.unlock();
									}
									toRebound = ta;
								} else {
									if (!ta.isUnstoppable()) {
										if (parent.hasEvents) {
											// Comprobar eventos (Reaction
											// first)
											if (eventManager.checkEvent(1, mol, ta)) {
												resolveCollision = eventManager.resolveEvent(1, mol, ta);
											}
											if (eventManager.checkEvent(0, mol) && resolveCollision == 2) {
												resolveCollision = eventManager.resolveEvent(0, mol, ta);
											}
											if (eventManager.checkEvent(2, mol) && resolveCollision == 2) {
												resolveCollision = eventManager.resolveEvent(2, mol, ta);
											}
										}

										if (resolveCollision == 2) {
											// No se puede realizar evento,
											// seguir buscando (posible rebote)
											if (toRebound != null && toRebound != ta) {
												toRebound.unlock();
											}
											toRebound = ta;
										} else if (resolveCollision == 0) {
											// Evento exitoso, se ha realizado
											// una acción este turno
											ta.unlock();
											toRet = 0;
											break;
										} else {
											// Evento fallido, se necesita
											// rebotar
											if (toRebound != null && toRebound != ta) {
												toRebound.unlock();
											}
											toRebound = ta;
											break;
										}
									} else {
										if (toRebound != null && toRebound != ta) {
											toRebound.unlock();
										}
										toRebound = ta;
									}
								}
							}
						}
						if (ta != toRebound) {
							ta.unlock();
						}

					} else {
						// Ceden todos menos el root
						if (toRebound != null) {
							toRebound.unlock();
						}
						return 2;

					}
				}
			}


			if (toRebound != null) {
				if (!toRebound.isToStop() && !mol.isToStop()) {
					Functions.rebound(mol, currentLocation, toRebound, parent.environment.getObjectLocation(toRebound),
							parent);
				}
				toRebound.unlock();
			}

		}

		return toRet;
	}
}
