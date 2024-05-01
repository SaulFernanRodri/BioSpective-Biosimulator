package es.uvigo.ei.sing.singulator.interfaces;

import java.util.Map;

import es.uvigo.ei.sing.singulator.modules.physics.Vector3D;
import sim.engine.SimState;
import sim.util.Double3D;

public interface iMolecule {
	public int getCellId();

	public double getCurrentDiffusionRate();

	public double getRadius();

	public Double3D getDesiredLocation();

	public void setCellId(int cellId);

	public int getMaxLayer();

	public boolean isInDoor();

	public void setCurrentZone(int currentZone);

	public Map<Integer, Double> getMapZoneDiffRate();

	public void setCurrentDiffusionRate(double currentDiffusionRate);

	public Vector3D getSpeed();

	public void setSpeed(Vector3D speed);

	public int getMinLayer();

	public void setNegativeSpeed();

	public void setDesiredLocation(Double3D desiredLocation);

	public Vector3D getOriginalSpeed();

	public boolean isHasCrash();

	public void setHasCrash(boolean hasCrash);

	public Double3D getCurrentLocation();

	public int getCurrentZone();

	public void setInitialPostion(Double3D init);

	public void setFinalPostion(Double3D finalPos);

	public void setCurrentLocation(Double3D currentLocation);

	public String getName();

	public String retrieveInitLocalization();

	public boolean isToStop();

	public String getDoorName();

	public void calculateDesired(SimState state);

	public boolean firstPrepare(SimState state);

	public int secondPrepare(SimState state, int rootID, int extent);

	public void move(SimState state);

	public boolean tryLock();

	public void lock();

	public void unlock();

	public boolean isAcceptablePosition();

	public Double3D getFinalPosition();

	public void putReactionId(int reactionId);

	public void putReaction(boolean isReaction);

	public void putTimeToWait(int timeToWait);

	public void setInitialPosition(Double3D initialPosition);

	public void setFinalPosition(Double3D finalPosition);

	public void putToStop(boolean toStop);

	public boolean isReaction();

	public int getId();

	public int getDoorID();

	public void setUnstoppable(boolean isUnstoppable);

	public void setInDoor(boolean inDoor);

	public void setDoorID(int doorID);

	public void setDoorName(String doorName);

	public void setMaxLayer(int maxLayer);

	public void setMinLayer(int minLayer);

	public void setHasCrashWihtEnvironment(boolean hasCrashWihtEnvironment);

	public boolean isUnstoppable();

	public double getTotalDistance();

	public Double3D getInitialPosition();

	public boolean isHasCrashWihtEnvironment();

	public boolean getTimeToWait();

	public int getRadInfl();

	public String getRadInflWith();

	public void setTimeStepToZone(int zone, long timestep);

	public Long getTimeStepForZone(int zone);

	public void setLastReboundWith(String lastReboundWith);

	public String getLastReboundWith();

	public Character showMRna();

	public Character removeMRna();

	public String getType();
}
