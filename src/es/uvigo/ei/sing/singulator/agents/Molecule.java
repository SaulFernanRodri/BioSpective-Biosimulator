package es.uvigo.ei.sing.singulator.agents;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import es.uvigo.ei.sing.singulator.constants.Constants;
import es.uvigo.ei.sing.singulator.interfaces.iMolecule;
import es.uvigo.ei.sing.singulator.modules.physics.Vector3D;
import es.uvigo.ei.sing.singulator.simulator.SINGulator_Model;
import es.uvigo.ei.sing.singulator.utils.Functions;
import es.uvigo.ei.sing.singulator.utils.LockProxy;
import sim.engine.SimState;
import sim.util.Double3D;

public class Molecule implements iMolecule, Serializable {
	private static final long serialVersionUID = 1L;

	// Boolean variables
	private boolean acceptablePosition = false;
	private boolean hasCrashWihtEnvironment = false;
	private boolean hasCrash;
	private boolean inDoor = false;
	private boolean isReaction;
	private boolean isUnstoppable = false;
	private boolean toStop = false;

	// Integer variables
	private int id;
	private int cellID = -1;
	private int doorID = -1;
	@SuppressWarnings("unused")
	private int reactionID = -1;
	private int currentZone;
	private int previousZone;
	private int maxLayer;
	private int minLayer;
	private int timeToWait = 0;
	private int radInfl;
	private int randomRebound;

	// Double variables
	private double radius;
	private double weigth;
	private double currentDiffusionRate;
	private double totalDistance = 0;

	// String variables
	private String name;
	private String initialLocalization;
	private String doorName = "";
	private String radInflWith;
	private String lastReboundWith;
	private String type;

	// Double3D variables
	private Double3D currentLocation;
	private Double3D desiredLocation;
	private Double3D initialPosition;
	private Double3D finalPosition;

	// Vector3D variables
	private Vector3D originalSpeed;
	private Vector3D speed;

	// Complex variables
	private LockProxy<Lock> lock;
	private Stack<Character> mRna;

	// Collection variables
	private Map<Integer, Double> mapZoneDiffRate;
	private Map<Integer, Long> mapZoneTimeStep;

	public Molecule(int id, String name, double weigth, double radius, String color, double drExterior,
			double drOuterMembrane, double drOuterPeriplasm, double drPeptidoglycan, double drInnerPeriplasm,
			double drInnerMembrane, double drCytoplasm, int maxLayer, int minLayer, double random1, double random2,
			int initialZone, String initialLocalization, int radInfl, String radInflWith, String type, String mRNA) {
		this.id = id;
		this.name = name;
		this.weigth = weigth;
		this.radius = radius;

		this.mapZoneDiffRate = new HashMap<Integer, Double>();
		this.mapZoneDiffRate.put(Constants.EXTERIOR, drExterior);
		this.mapZoneDiffRate.put(Constants.OUTER_MEMBRANE, drOuterMembrane);
		this.mapZoneDiffRate.put(Constants.OUTER_PERIPLASM, drOuterPeriplasm);
		this.mapZoneDiffRate.put(Constants.PEPTIDOGLYCAN, drPeptidoglycan);
		this.mapZoneDiffRate.put(Constants.INNER_PERIPLASM, drInnerPeriplasm);
		this.mapZoneDiffRate.put(Constants.INNER_MEMBRANE, drInnerMembrane);
		this.mapZoneDiffRate.put(Constants.CYTOPLASM, drCytoplasm);

		this.mapZoneTimeStep = new HashMap<Integer, Long>();
		this.mapZoneTimeStep.put(Constants.OUTER_MEMBRANE, -1L);
		this.mapZoneTimeStep.put(Constants.OUTER_PERIPLASM, -1L);
		this.mapZoneTimeStep.put(Constants.PEPTIDOGLYCAN, -1L);
		this.mapZoneTimeStep.put(Constants.INNER_PERIPLASM, -1L);
		this.mapZoneTimeStep.put(Constants.INNER_MEMBRANE, -1L);
		this.mapZoneTimeStep.put(Constants.CYTOPLASM, -1L);

		this.maxLayer = maxLayer;
		this.minLayer = minLayer;
		this.currentZone = initialZone;

		Random r = new Random();
		this.randomRebound = r.nextInt(101);

		// Calculate random angles
		double upDownangleInDegree = Math.toRadians(random1);
		double leftRightangleInDegree = Math.toRadians(random2);

		// Calculate vector speeds
		double drLayer = mapZoneDiffRate.get(initialZone);
		this.speed = new Vector3D(drLayer * Math.sin(leftRightangleInDegree) * Math.cos(upDownangleInDegree),
				-drLayer * Math.sin(upDownangleInDegree),
				drLayer * Math.cos(leftRightangleInDegree) * Math.cos(upDownangleInDegree));
		this.currentDiffusionRate = drLayer;
		this.originalSpeed = speed;

		this.initialLocalization = initialLocalization;

		this.radInfl = radInfl;
		this.radInflWith = radInflWith;

		this.type = type;

		if (!mRNA.isEmpty()) {
			this.mRna = new Stack<Character>();

			char[] charArray = mRNA.toCharArray();
			for (int i = charArray.length - 1; i >= 0; i--) {
				this.mRna.add(charArray[i]);
			}
		}

		this.lock = new LockProxy<Lock>(new ReentrantLock());
	}

	@Override
	public void calculateDesired(SimState state) {
		if (timeToWait == 0 && !this.toStop) {
			SINGulator_Model cs = (SINGulator_Model) state;
			hasCrash = false;
			this.currentLocation = cs.environment.getObjectLocation(this);

			// Generate movement
			if(cs.schedule.getSteps() % cs.stepRandomRebound == 0 && this.randomRebound <= cs.randomRebound){
				double upDownangleInDegree = Math.toRadians(cs.random.nextInt(365));
				double leftRightangleInDegree = Math.toRadians(cs.random.nextInt(365));
				this.speed = new Vector3D(this.currentDiffusionRate * Math.sin(leftRightangleInDegree) * Math.cos(upDownangleInDegree),
						-this.currentDiffusionRate * Math.sin(upDownangleInDegree),
						this.currentDiffusionRate * Math.cos(leftRightangleInDegree) * Math.cos(upDownangleInDegree));
			}

			this.desiredLocation = new Double3D(currentLocation.x + speed.x, currentLocation.y + speed.y,
					currentLocation.z + speed.z);
			this.previousZone = currentZone;
			this.originalSpeed = speed;
		}
	}

	@Override
	public boolean firstPrepare(SimState state) {
		SINGulator_Model cs = (SINGulator_Model) state;

		// Encontrar la zona actual y settear velocidad, celula, etc
		if (cs.hasCells) {
			Functions.findCurrentZone(this, cs.mapIdCell);
		} else {
			// Set speed with diffusion rate
			speed.maxLimit(currentDiffusionRate);
			speed.minLimit(currentDiffusionRate);

			setSpeed(speed);
		}

		// Si cambias de zona y vas mas adentro en la celula
		if (previousZone != currentZone && previousZone < currentZone) {
			this.mapZoneTimeStep.put(currentZone, cs.schedule.getSteps());
		}

		// Comprobar eventos (kill y/o transform)
		if (cs.hasEvents) {
			int resolved = 2;
			if (cs.simulatorLogic.eventManager.checkEvent(0, this)) {
				resolved = cs.simulatorLogic.eventManager.resolveEvent(0, this);
			}
			if (cs.simulatorLogic.eventManager.checkEvent(2, this) && resolved == 2) {
				cs.simulatorLogic.eventManager.resolveEvent(2, this);
			}
		}

		// Avisar al Feeder por si tiene que crear algo
		// if (cs.hasFeeder) {
		// if (!cs.mapIdCell.isEmpty() && cellID != -1 && previousZone !=
		// currentZone) {
		// cs.simulatorLogic.parent.feeder.checkAndIncrementMoleculeCreation(this,
		// name, currentZone,
		// cs.mapIdCell.get(cellID).getCellName());
		// }
		// }

		if (!this.inDoor && timeToWait == 0 && !this.toStop) {
			// Validate collision with the board and other particles
			this.acceptablePosition = cs.simulatorLogic.acceptablePosition1(this);
		} else {
			this.acceptablePosition = true;
		}

		return this.acceptablePosition;
	}

	@Override
	public int secondPrepare(SimState state, int rootID, int extent) {
		int toRet = 1;
		SINGulator_Model cs = (SINGulator_Model) state;

		if (!this.inDoor && timeToWait == 0 && !this.toStop) {
			// Validate collision with the board and other particles
			toRet = cs.simulatorLogic.acceptablePosition2(this, rootID, extent);

			// Si esta imparable activo se puede mover, aunque necesitas
			// encontrar la primera posición libre para desactivar la variable
			if (isUnstoppable && toRet == 0) {
				this.acceptablePosition = true;
			} else if (isUnstoppable && toRet == 1) {
				this.acceptablePosition = true;
				isUnstoppable = false;
			} else if (toRet == 0) {
				this.acceptablePosition = false;
			} else if (toRet == 1) {
				this.acceptablePosition = true;
			}
		} else {
			this.acceptablePosition = true;
		}

		return toRet;
	}

	@Override
	public void move(SimState state) {
		SINGulator_Model cs = (SINGulator_Model) state;

		if (!this.toStop) {
			if (desiredLocation != null && this.acceptablePosition && timeToWait == 0) {
				synchronized (cs.environment) {
					cs.environment.setObjectLocation(this, desiredLocation);
				}

				// Get accumulative distance
				totalDistance += getTotalSpeed();
				finalPosition = desiredLocation;
			} else if (timeToWait > 0) {
				timeToWait--;

				if (timeToWait == 0) {
					putReaction(false);
					// setUnstoppable(true);
				}
			}
		}
	}

	@Override
	public boolean isUnstoppable() {
		return isUnstoppable;
	}

	@Override
	public boolean isHasCrash() {
		return hasCrash;
	}

	@Override
	public void setHasCrash(boolean hasCrash) {
		this.hasCrash = hasCrash;
	}

	@Override
	public void setUnstoppable(boolean isUnstoppable) {
		this.isUnstoppable = isUnstoppable;
	}

	@Override
	public boolean tryLock() {
		return lock.tryLock();
	}

	@Override
	public void lock() {
		lock.lock();
	}

	@Override
	public void unlock() {
		lock.unlock();
	}

	public void forceUnlock() {
		lock.forceUnlock();
	}

	@Override
	public String retrieveInitLocalization() {
		return this.initialLocalization;
	}

	@Override
	public Vector3D getOriginalSpeed() {
		return originalSpeed;
	}

	@Override
	public int getCellId() {
		return cellID;
	}

	@Override
	public double getCurrentDiffusionRate() {
		return currentDiffusionRate;
	}

	@Override
	public Double3D getCurrentLocation() {
		return currentLocation;
	}

	@Override
	public int getCurrentZone() {
		return currentZone;
	}

	@Override
	public Double3D getDesiredLocation() {
		return desiredLocation;
	}

	@Override
	public int getDoorID() {
		return doorID;
	}

	@Override
	public String getDoorName() {
		return doorName;
	}

	@Override
	public Double3D getFinalPosition() {
		return finalPosition;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public Double3D getInitialPosition() {
		return initialPosition;
	}

	@Override
	public Map<Integer, Double> getMapZoneDiffRate() {
		return mapZoneDiffRate;
	}

	@Override
	public int getMaxLayer() {
		return maxLayer;
	}

	@Override
	public int getMinLayer() {
		return minLayer;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public double getRadius() {
		return radius;
	}

	@Override
	public Vector3D getSpeed() {
		return speed;
	}

	@Override
	public boolean getTimeToWait() {
		if (timeToWait > 0) {
			return false;
		} else
			return true;
	}

	@Override
	public double getTotalDistance() {
		return totalDistance;
	}

	private double getTotalSpeed() {
		double toRet;

		toRet = Math.pow(speed.x, 2) + Math.pow(speed.y, 2) + Math.pow(speed.z, 2);

		return Math.sqrt(toRet);
	}

	public double getWeigth() {
		return weigth;
	}

	@Override
	public boolean isHasCrashWihtEnvironment() {
		return hasCrashWihtEnvironment;
	}

	@Override
	public boolean isInDoor() {
		return inDoor;
	}

	@Override
	public boolean isReaction() {
		return isReaction;
	}

	@Override
	public boolean isToStop() {
		return toStop;
	}

	@Override
	public boolean isAcceptablePosition() {
		return acceptablePosition;
	}

	@Override
	public void putReaction(boolean isReaction) {
		this.isReaction = isReaction;
	}

	@Override
	public void putReactionId(int reactionId) {
		this.reactionID = reactionId;
	}

	@Override
	public void putTimeToWait(int timeToWait) {
		this.timeToWait = timeToWait;
	}

	@Override
	public void putToStop(boolean toStop) {
		this.toStop = toStop;
	}

	@Override
	public void setCellId(int cellId) {
		// TODO Auto-generated method stub
		this.cellID = cellId;
	}

	@Override
	public void setCurrentDiffusionRate(double currentDiffusionRate) {
		this.currentDiffusionRate = currentDiffusionRate;
	}

	@Override
	public void setCurrentLocation(Double3D currentLocation) {
		this.currentLocation = currentLocation;
	}

	@Override
	public void setCurrentZone(int currentZone) {
		this.currentZone = currentZone;
	}

	@Override
	public void setDesiredLocation(Double3D desiredLocation) {
		this.desiredLocation = desiredLocation;
	}

	@Override
	public void setDoorID(int doorID) {
		this.doorID = doorID;
	}

	@Override
	public void setDoorName(String doorName) {
		this.doorName = doorName;
	}

	@Override
	public void setFinalPosition(Double3D finalPosition) {
		this.finalPosition = finalPosition;
	}

	@Override
	public void setFinalPostion(Double3D finalPos) {
		// TODO Auto-generated method stub
		finalPosition = finalPos;
	}

	@Override
	public void setHasCrashWihtEnvironment(boolean hasCrashWihtEnvironment) {
		this.hasCrashWihtEnvironment = hasCrashWihtEnvironment;
	}

	@Override
	public void setInDoor(boolean inDoor) {
		this.inDoor = inDoor;
	}

	@Override
	public void setInitialPosition(Double3D initialPosition) {
		this.initialPosition = initialPosition;
	}

	@Override
	public void setInitialPostion(Double3D init) {
		// TODO Auto-generated method stub
		initialPosition = init;
	}

	@Override
	public void setMaxLayer(int maxLayer) {
		this.maxLayer = maxLayer;
	}

	@Override
	public void setMinLayer(int minLayer) {
		this.minLayer = minLayer;
	}

	@Override
	public void setNegativeSpeed() {
		this.speed.x *= -1;
		this.speed.y *= -1;
		this.speed.z *= -1;
		this.speed.rotate2D(45);
	}

	@Override
	public void setSpeed(Vector3D speed) {
		this.speed = speed;
	}

	@Override
	public int getRadInfl() {
		return this.radInfl;
	}

	@Override
	public String getRadInflWith() {
		return this.radInflWith;
	}

	@Override
	public void setTimeStepToZone(int zone, long timestep) {
		this.mapZoneTimeStep.put(zone, timestep);
	}

	@Override
	public Long getTimeStepForZone(int zone) {
		return this.mapZoneTimeStep.get(zone);
	}

	public Map<Integer, Long> getMapZoneTimeStep() {
		return this.mapZoneTimeStep;
	}

	@Override
	public void setLastReboundWith(String lastReboundWith) {
		this.lastReboundWith = lastReboundWith;
	}

	@Override
	public String getLastReboundWith() {
		return this.lastReboundWith;
	}

	@Override
	public Character showMRna() {
		return mRna.peek();
	}

	@Override
	public Character removeMRna() {
		return mRna.pop();
	}

	@Override
	public String getType() {
		return type;
	}

	public Stack<Character> getmRna() {
		return mRna;
	}
}
