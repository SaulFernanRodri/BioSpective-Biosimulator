package es.uvigo.ei.sing.singulator.agents;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import es.uvigo.ei.sing.singulator.constants.Constants;
import es.uvigo.ei.sing.singulator.interfaces.iLayer;
import es.uvigo.ei.sing.singulator.interfaces.iMolecule;
import es.uvigo.ei.sing.singulator.simulator.SINGulator_Model;
import sim.engine.SimState;
import sim.util.Double3D;

public class Cell implements iLayer, Serializable {
	private static final long serialVersionUID = 1L;

	// Integer variables
	private int id;
	private int zoneID;
	protected int extent;
	// PCQUORUM DEPENDANCE
	protected int consumed = 0;

	// Double variables
	private double radius;
	private double height;
	private double scale;
	private double h1Min;
	private double h2Max;
	private double h1Center;
	private double h2Center;
	private double verticalAngle;
	private double horizontalAngle;

	// String variables
	private String layerName;
	protected String cellName;
	protected String form;

	// Double3D variables
	private Double3D location;

	// Complex variables
	private Color color;

	// Collection variables
	private TreeMap<Integer, iLayer> mapZoneLayer;
	private Map<String, List<Door>> mapNameDoors;
	private Map<String, Integer> mapNameDoorCount;
	private Set<iMolecule> setMoleculesInside;

	public Cell(String cellName, String layerName, double scale, double radius, double height, String color, int id,
			String form) {
		// Common variables for both types
		this.cellName = cellName;
		this.layerName = layerName;
		this.scale = scale;
		this.radius = radius;
		this.height = height;
		this.color = Color.decode(color);
		this.id = id;
		this.form = form;

		this.mapZoneLayer = new TreeMap<Integer, iLayer>();
		// Add the Layer of the Cell to the map
		// Cell is always zone 1
		// PCQUORUM DEPENDANCE
		switch (layerName) {
		case Constants.OUTER_MEMBRANE_NAME:
			this.zoneID = 1;
			this.mapZoneLayer.put(1, this);
			break;
		case Constants.OUTER_PERIPLASM_NAME:
			this.zoneID = 2;
			this.mapZoneLayer.put(2, this);
			break;
		case Constants.PEPTIDOGLYCAN_NAME:
			this.zoneID = 3;
			this.mapZoneLayer.put(3, this);
			break;
		case Constants.INNER_PERIPLASM_NAME:
			this.zoneID = 4;
			this.mapZoneLayer.put(4, this);
			break;
		case Constants.INNER_MEMBRANE_NAME:
			this.zoneID = 5;
			this.mapZoneLayer.put(5, this);
			break;
		case Constants.CYTOPLASM_NAME:
			this.zoneID = 6;
			this.mapZoneLayer.put(6, this);
			break;
		default:
			this.zoneID = 1;
			this.mapZoneLayer.put(1, this);
			break;
		}
		this.mapNameDoors = new HashMap<String, List<Door>>();
		this.mapNameDoorCount = new HashMap<String, Integer>();
		this.setMoleculesInside = new HashSet<iMolecule>();

		// El exterior es el 0
		this.horizontalAngle = 0;
		this.verticalAngle = 0;
	}

	public void step(SimState state) {
		if (form.equals("sphere")) {
			SINGulator_Model cs = (SINGulator_Model) state;

			iMolecule mol;

		}
	}

	@Override
	public void setLocation(Double3D location) {
		this.location = location;

		// Type capsule
		if (form.equals(Constants.CAPSULE)) {
			// Calculate hemispheres
			this.h1Center = location.x - height / 2;
			this.h2Center = location.x + height / 2;
			this.h1Min = h1Center - radius;
			this.h2Max = h2Center + radius;
		}
		// Type sphere
		else if (form.equals(Constants.SPHERE)) {
			// Calculate hemispheres
			this.h1Center = location.x;
			this.h2Center = location.x;
			this.h1Min = h1Center - radius;
			this.h2Max = h2Center + radius;
		}
		// Type hemisphere
		else {
			this.h1Center = location.x;
			this.h2Center = location.x;
			this.h1Min = h1Center;
			this.h2Max = h2Center + radius;
		}
	}

	@Override
	public double getHorizontalAngle() {
		return horizontalAngle;
	}

	@Override
	public void setHorizontalAngle(double horizontalAngle) {
		this.horizontalAngle = horizontalAngle;
	}

	@Override
	public double getVerticalAngle() {
		return verticalAngle;
	}

	@Override
	public void setVerticalAngle(double verticalAngle) {
		this.verticalAngle = verticalAngle;
	}

	@Override
	public int getExtent() {
		return extent;
	}

	@Override
	public void setExtent(int extent) {
		this.extent = extent;
	}

	public void addMoleculeToCell(iMolecule molecule) {
		this.setMoleculesInside.add(molecule);
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public double getH1Center() {
		return h1Center;
	}

	@Override
	public double getH1Min() {
		return h1Min;
	}

	@Override
	public double getH2Center() {
		return h2Center;
	}

	public double getH2Max() {
		return h2Max;
	}

	@Override
	public double getHeight() {
		return height;
	}

	public int getId() {
		return id;
	}

	public iLayer getLayer(int zone) {
		// Zone 1 == Outer membrane == Cell
		if (zone == 1) {
			return this;
		} else {
			return mapZoneLayer.get(zone);
		}
	}

	public iLayer getNextLayer(int zone) {
		return mapZoneLayer.get(mapZoneLayer.ceilingKey(zone));
	}

	@Override
	public Double3D getLocation() {
		return location;
	}

	public Map<String, Integer> getMapNameDoorCount() {
		return mapNameDoorCount;
	}

	public Map<String, List<Door>> getMapNameDoors() {
		return mapNameDoors;
	}

	public TreeMap<Integer, iLayer> getMapZoneLayers() {
		return mapZoneLayer;
	}

	public int getMolsSize() {
		return this.setMoleculesInside.size();
	}

	public String getLayerName() {
		return layerName;
	}

	@Override
	public double getRadius() {
		return radius;
	}

	@Override
	public double getScale() {
		return scale;
	}

	public Set<iMolecule> getSetMoleculesInside() {
		return setMoleculesInside;
	}

	@Override
	public String getForm() {
		return form;
	}

	public int getZoneID() {
		return zoneID;
	}

	public void increaseMapNameDoorCount(String doorName) {
		if (mapNameDoorCount.containsKey(doorName)) {
			mapNameDoorCount.put(doorName, mapNameDoorCount.get(doorName) + 1);
		} else {
			mapNameDoorCount.put(doorName, 1);
		}
	}

	public void putLayer(int zone, CellLayer layer) {
		// TODO: DEBEN IR ORDENADAS EN EL FICHERO, DE MAS EXTERNA A MAS INTERNA
		this.mapZoneLayer.put(zone, layer);
	}

	public void putMapNameDoors(String doorName, Door door) {
		List<Door> listWithDoors;
		if (mapNameDoors.containsKey(doorName)) {
			listWithDoors = mapNameDoors.get(doorName);
			listWithDoors.add(door);

			mapNameDoors.put(doorName, listWithDoors);
		} else {
			listWithDoors = new ArrayList<Door>();
			listWithDoors.add(door);
			this.mapNameDoors.put(doorName, listWithDoors);
		}
	}

	public void removeMoleculeInCell(iMolecule molecule) {
		this.setMoleculesInside.remove(molecule);
	}

	public int getLastLayerNumber() {
		return mapZoneLayer.lastKey();
	}

	@Override
	public String getCellName() {
		return cellName;
	}

	@Override
	public String getType() {
		return Constants.CELLS;
	}

	// PCQUORUM DEPENDANCE
	public int getConsumed() {
		return consumed;
	}

	public void setConsumed() {
		this.consumed++;
	}
}
