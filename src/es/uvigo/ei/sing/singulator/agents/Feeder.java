package es.uvigo.ei.sing.singulator.agents;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import es.uvigo.ei.sing.singulator.constants.Constants;
import es.uvigo.ei.sing.singulator.interfaces.iMolecule;
import es.uvigo.ei.sing.singulator.modules.physics.PhysicsEngine;
import es.uvigo.ei.sing.singulator.modules.physics.Vector3D;
import es.uvigo.ei.sing.singulator.simulator.SINGulator_Model;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Double3D;

public class Feeder implements Serializable {
	private static final long serialVersionUID = 1L;

	private String toCreate;
	private String type;
	private String location;

	private int everyStep;
	private int productionNumber;

	private int maxConcentration;
	private int tries;

	// PCQUORUM DEPENDANCE: FORCE TO SPAWN IN ONE LOCATION
	private AtomicInteger leftHemisphere;
	private AtomicInteger cylinder;
	private AtomicInteger rightHemisphere;

	public Feeder(String toCreate, String type, String location, int everyStep, int productionNumber,
			int maxConcentration) {
		this.toCreate = toCreate;
		this.type = type;
		this.location = location;
		this.everyStep = everyStep;
		this.productionNumber = productionNumber;
		this.maxConcentration = maxConcentration;

		this.tries = 1000000;
	}

	public void step(SimState state) {
		SINGulator_Model model = (SINGulator_Model) state;

		long steps = model.schedule.getSteps();
		if (steps % everyStep == 0) {
			int count = 0, id, currentTries;
			double random1, random2, radius;
			iMolecule result;
			Double3D location3D;
			Object[] ret;
			String position = null;

			// [0]:Name,[1]:MW,[2]:Radius,[3...9]:DR,[10]:Color,[11]:Number,[12]:
			// MaxLayer,[13]:minLayer,[14]:radInfl,[15]:radInflWith,[16]:type,[17]:mRNA
			String[] info = model.mapNameInformation.get(toCreate);

			count = model.mapNameNumber.get(toCreate);

			// PCQUORUM DEPEDENCE (CREATE IN PSEUDOMONAS)
			for (Cell cell : model.getcreatedCells().values()) {
				/* DUAL FEEDING DEPENDENCE!!!
				* Ideally, it would be nicer to eliminate the hardcoding of the organism's name and use something like the location specified in the .json
				* That is, if the cell currently in process is the one that should secrete the molecules. While this works with the condition commented below,
				* it does not work when both the cell and the environment are feeding. Somehow, location=exterior is not tested as it should... granted exterior is not specified as a cell!!!
				* */

				if (cell.getCellName().equals(location) ) {
					System.out.println("??? "+cell.getCellName()+"  "+location);

					for (int i = 0; i < productionNumber; i++) {
						currentTries = tries;

						if (!type.equals("Continuous") && count >= maxConcentration) {
						//if (count >= maxConcentration) {
							System.out.println("Stopping since "+cell.getCellName()+" has "+count+" and limit is "+maxConcentration);
							break;
						}

						while (currentTries > 0) {
							synchronized (model.random) {
								if (location.equals("exterior")) {
									location3D = PhysicsEngine.calculateRandomLocationInEnvironmentPerimeter(
											model.random, Double.parseDouble(info[2]), 0.0, model.width, 0.0,
											model.height, 0.0, model.length);
								}
								else {
									if (cell.getForm().equals("capsule")) {
										// PCQUORUM DEPEDENCE (CREATE IN
										// PSEUDOMONAS)
										ret = PhysicsEngine.calculateRandomLocationInPerimenterLayerCapsule(
												model.random, cell.getLocation(), cell.getHeight() * 1.01,
												cell.getHorizontalAngle() * 1.01,
												cell.getVerticalAngle() * 1.01, cell.getRadius() * 1.01,
												Double.parseDouble(info[2]));
										location3D = (Double3D) ret[0];
										position = (String) ret[1];
									} else {
										// PCQUORUM DEPEDENCE (CREATE IN
										// CANDIDA)
										location3D = PhysicsEngine.calculateRandomLocationInPerimeterLayerSphere(
												model.random, cell.getLocation(), cell.getRadius() * 1.01,
												Double.parseDouble(info[2]));
									}
								}

								random1 = model.random.nextInt(360);
								random2 = model.random.nextInt(360);
							}

							// Coges su id estimado
							id = model.simulatorLogic.agentID.get();
							// Coges su radio estimado
							radius = Double.parseDouble(info[2]);

							if (canSpawn(model, radius, id, location3D)){
								// Coges su id e incrementas
								id = model.simulatorLogic.agentID.getAndIncrement();
								if (Constants.CAN_GUI) {
									// Create molecule with data
									result = new SphereMolecule(id, toCreate, Double.parseDouble(info[1]), radius,
											info[10], Double.parseDouble(info[3]), Double.parseDouble(info[4]),
											Double.parseDouble(info[5]), Double.parseDouble(info[6]),
											Double.parseDouble(info[7]), Double.parseDouble(info[8]),
											Double.parseDouble(info[9]), Integer.parseInt(info[12]),
											Integer.parseInt(info[13]), random1, random2, 0, "exterior",
											Integer.parseInt(info[14]), info[15], info[16], info[17]);
								} else {
									// Create molecule with data
									result = new Molecule(id, toCreate, Double.parseDouble(info[1]), radius, info[10],
											Double.parseDouble(info[3]), Double.parseDouble(info[4]),
											Double.parseDouble(info[5]), Double.parseDouble(info[6]),
											Double.parseDouble(info[7]), Double.parseDouble(info[8]),
											Double.parseDouble(info[9]), Integer.parseInt(info[12]),
											Integer.parseInt(info[13]), random1, random2, 0, "exterior",
											Integer.parseInt(info[14]), info[15], info[16], info[17]);
								}

								// PCQUORUM DEPENDENCE: Para que se muevan
								// perpendicularmente a la
								// superficie del ambiente
								if (location3D.x == result.getRadius()) {
									result.setSpeed(new Vector3D(result.getCurrentDiffusionRate(), 0, 0));
								} else if (location3D.x == (model.width - result.getRadius())) {
									result.setSpeed(new Vector3D(-result.getCurrentDiffusionRate(), 0, 0));
								} else if (location3D.y == result.getRadius()) {
									result.setSpeed(new Vector3D(0, result.getCurrentDiffusionRate(), 0));
								} else if (location3D.y == (model.height - result.getRadius())) {
									result.setSpeed(new Vector3D(0, -result.getCurrentDiffusionRate(), 0));
								} else if (location3D.z == result.getRadius()) {
									result.setSpeed(new Vector3D(0, 0, result.getCurrentDiffusionRate()));
								} else if (location3D.z == (model.length - result.getRadius())) {
									result.setSpeed(new Vector3D(0, 0, -result.getCurrentDiffusionRate()));
								}

								synchronized (model.environment) {
									model.environment.setObjectLocation(result, location3D);
								}

								result.setInitialPosition(location3D);
								result.setFinalPosition(location3D);
								result.setCurrentLocation(location3D);
								result.setDesiredLocation(location3D);

								// PCQUORUM DEPEDENCE (CREATE IN PSEUDOMONAS)
								if (!location.equals("exterior")) {
									Vector3D speed;
									// COMPROBAR CUADRANTE (1º HEMISFERIO,
									// CENTRO, 2º
									// HEMISFERIO)
									if (result.getCurrentLocation().x <= cell.getH1Center()) {
										speed = new Vector3D(result.getCurrentLocation().x - cell.getH1Center(),
												result.getCurrentLocation().y - cell.getLocation().y,
												result.getCurrentLocation().z - cell.getLocation().z);
									} else if (result.getCurrentLocation().x >= cell.getH2Center()) {
										speed = new Vector3D(result.getCurrentLocation().x - cell.getH2Center(),
												result.getCurrentLocation().y - cell.getLocation().y,
												result.getCurrentLocation().z - cell.getLocation().z);
									} else {
										speed = new Vector3D(
												result.getCurrentLocation().x - result.getCurrentLocation().x,
												result.getCurrentLocation().y - cell.getLocation().y,
												result.getCurrentLocation().z - cell.getLocation().z);
									}
									speed.normalize();
									speed.mult(result.getCurrentDiffusionRate());

									result.setSpeed(speed);
								}

								// Indicas que se ha insertado
								count++;

								model.mapNameNumber.put(result.getName(), count);
								break;
							} else {
								currentTries--;
							}
						}
					}

				}
			}
		}
	}

	private boolean canSpawn(SINGulator_Model model, double radius, int id, Double3D loc) {
		boolean crash, toRet = true;
		Double3D anotherLocation;

		for (Cell cell : model.mapIdCell.values()) {
			// Validate if the molecules has crashed.
			crash = PhysicsEngine.spawnMoleculeWithLayer(radius, loc, cell.getRadius(), cell.getH1Center(),
					cell.getH2Center(), cell.getForm(), cell.getLocation());

			// PCQUORUM DEPEDENCE (CREATE INSIDE HEMISPHERE)
			if (crash && !cell.getForm().equals("hemisphere")) {
				toRet = false;
			}
		}

		if (toRet) {
			Bag possibleMolecules = model.environment.getNeighborsWithinDistance(loc, radius * 2, false, true);

			// Set<Molecule> possibleMolecules = new HashSet<Molecule>(
			// submap.values());
			// Go over neighbors
			iMolecule anotherAgent;
			for (Object obj : possibleMolecules) {
				if (obj != null && obj instanceof iMolecule) {
					anotherAgent = (iMolecule) obj;
					if (anotherAgent.getId() != id) {
						// Get molecule location in the partition
						anotherLocation = anotherAgent.getCurrentLocation();

						// Validate if the molecules has crashed
						crash = PhysicsEngine.checkCollisionBetweenSpheres(radius, loc, anotherAgent.getRadius(),
								anotherLocation);

						// If the molecule crash with another molecule in the
						// board,
						// return false and indicate that molecule can't spawn
						if (crash) {
							toRet = false;
							break;
						}
					}
				}
			}
		}

		return toRet;
	}


	public int getMaxConcentration() {
		return maxConcentration;
	}

	public void setMaxConcentration(int maxConcentration) {
		this.maxConcentration = maxConcentration;
	}
}
