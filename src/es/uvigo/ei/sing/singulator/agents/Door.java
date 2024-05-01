package es.uvigo.ei.sing.singulator.agents;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.uvigo.ei.sing.singulator.modules.physics.Vector3D;
import es.uvigo.ei.sing.singulator.simulator.SINGulator_Model;
import sim.engine.SimState;
import sim.util.Double3D;

public class Door implements Serializable {
	private static final long serialVersionUID = 1L;

	// Boolean variables
	private boolean isBusyExterior;
	private boolean isBusyInterior;

	// Integer variables
	private int id;
	private int cellId;
	private int exteriorZoneLocation;
	private int interiorZoneLocation;
	private int toGetZone;
	private int toExpulseZone;

	// Double variables
	private double radius;
	private double verticalAngle;
	private double horizontalAngle;
	private double diffusionRate;
	private double minDistanceToCenter;
	private double maxDistanceToCenter;

	// String variables
	private String name;
	private String type;

	// Double3D variables
	private Double3D exteriorLocation; // This class location (external door)
	private Double3D interiorLocation;
	private Double3D cellCenterLocation;

	// Complex variables
	private Color color;
	private DoorAgentDraw interior;

	// Collection variables
	private List<String> listMoleculeToGet;
	private List<String> listMoleculeToExpulse;
	private Set<Integer> moleculesIDs;

	public Door(String name, double radius, double diffusionRate, String color, int interiorZone, int exteriorZone,
			int toInputZone, int toOutputZone, String type, String[] moleculeInputs, String[] moleculeOutputs, int id) {
		this.name = name;
		this.type = type;
		this.radius = radius;
		this.diffusionRate = diffusionRate;
		this.color = Color.decode(color);
		this.interiorZoneLocation = interiorZone;
		this.exteriorZoneLocation = exteriorZone;
		this.toGetZone = toInputZone;
		this.toExpulseZone = toOutputZone;
		this.listMoleculeToGet = new ArrayList<String>(Arrays.asList(moleculeInputs));
		this.listMoleculeToExpulse = new ArrayList<String>(Arrays.asList(moleculeOutputs));
		this.id = id;

		this.isBusyExterior = false;
		this.isBusyInterior = false;
		this.interior = new DoorAgentDraw(name, this.color, radius, interiorZone, interiorLocation);
		this.moleculesIDs = new HashSet<Integer>();
	}

	public void step(SimState state) {
		SINGulator_Model cs = (SINGulator_Model) state;

		if (canWork(cs)) {
			cs.simulatorLogic.executeStepDoor(this);
		}
	}

	// TODO: Por ahora funcionan siempre, pero en el futuro se podrán
	// deshabilitar
	private boolean canWork(SINGulator_Model cs) {
		boolean toRet = true;

		return toRet;
	}

	public double getDiffusionRate() {
		return diffusionRate;
	}

	public String getName() {
		return name;
	}

	public double getRadius() {
		return radius;
	}

	public int getExteriorZone() {
		return exteriorZoneLocation;
	}

	public int getInteriorZone() {
		return interiorZoneLocation;
	}

	public int getToGetZone() {
		return toGetZone;
	}

	public int getToOutputZone() {
		return toExpulseZone;
	}

	public List<String> getListMoleculeToGet() {
		return listMoleculeToGet;
	}

	public List<String> getListMoleculeToExpulse() {
		return listMoleculeToExpulse;
	}

	public double getVerticalAngle() {
		return verticalAngle;
	}

	public void setVerticalAngle(double verticalAngle) {
		this.verticalAngle = verticalAngle;
	}

	public double getHorizontalAngle() {
		return horizontalAngle;
	}

	public void setHorizontalAngle(double horizontalAngle) {
		this.horizontalAngle = horizontalAngle;
	}

	public Color getColor() {
		return color;
	}

	public DoorAgentDraw getInterior() {
		return interior;
	}

	public Double3D getExteriorLocation() {
		return exteriorLocation;
	}

	public void setExteriorLocation(Double3D extLocation, Double3D cellCenter) {
		this.exteriorLocation = extLocation;
		// exterior.setLocation(extLocation);

		Vector3D center = new Vector3D(cellCenter.x, cellCenter.y, cellCenter.z);

		Vector3D desiredLoc = new Vector3D(exteriorLocation.x, exteriorLocation.y, exteriorLocation.z);

		double distX = center.x - desiredLoc.x;
		double distY = center.y - desiredLoc.y;
		double distZ = center.z - desiredLoc.z;

		this.maxDistanceToCenter = Math.sqrt((distX * distX) + (distY * distY) + (distZ * distZ));

	}

	public Double3D getInteriorLocation() {
		return interiorLocation;
	}

	public void setInteriorLocation(Double3D intLocation, Double3D cellCenter) {
		interiorLocation = intLocation;
		interior.setLocation(intLocation);

		Vector3D center = new Vector3D(cellCenter.x, cellCenter.y, cellCenter.z);

		Vector3D desiredLoc = new Vector3D(interiorLocation.x, interiorLocation.y, interiorLocation.z);

		double distX = center.x - desiredLoc.x;
		double distY = center.y - desiredLoc.y;
		double distZ = center.z - desiredLoc.z;

		this.minDistanceToCenter = Math.sqrt((distX * distX) + (distY * distY) + (distZ * distZ));
		// this. minDistanceToCenter = Math.sqrt(squareDistDesired);

	}

	public double getMinDistanceToCenter() {
		return minDistanceToCenter;
	}

	public double getMaxDistanceToCenter() {
		return maxDistanceToCenter;
	}

	public int getId() {
		return id;
	}

	public boolean isBusyInterior() {
		return isBusyInterior;
	}

	public void setBusyInterior(boolean isBusyInterior) {
		this.isBusyInterior = isBusyInterior;
	}

	public boolean isBusyExterior() {
		return isBusyExterior;
	}

	public void setBusyExterior(boolean isBusyExterior) {
		this.isBusyExterior = isBusyExterior;
	}

	public void addMolID(int id) {
		this.moleculesIDs.add(id);
	}

	public void removeMolID(int id) {
		this.moleculesIDs.remove(id);
	}

	public boolean isMoleculeID(int id) {
		return this.moleculesIDs.contains(id);
	}

	public boolean canTransportMolecules() {
		return this.moleculesIDs.size() + 1 < this.listMoleculeToExpulse.size() + this.listMoleculeToGet.size();
	}

	public String getType() {
		return type;
	}

	public void setCellCenterLocation(Double3D cellCenterLocation) {
		this.cellCenterLocation = cellCenterLocation;
	}

	public Double3D getCellCenterLocation() {
		return this.cellCenterLocation;
	}

	public int getCellId() {
		return cellId;
	}

	public void setCellId(int cellId) {
		this.cellId = cellId;
	}
}
