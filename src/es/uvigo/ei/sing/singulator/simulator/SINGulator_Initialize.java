package es.uvigo.ei.sing.singulator.simulator;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import es.uvigo.ei.sing.singulator.agents.Cell;
import es.uvigo.ei.sing.singulator.agents.CellLayer;
import es.uvigo.ei.sing.singulator.agents.Door;
import es.uvigo.ei.sing.singulator.agents.Feeder;
import es.uvigo.ei.sing.singulator.agents.Molecule;
import es.uvigo.ei.sing.singulator.agents.SphereMolecule;
import es.uvigo.ei.sing.singulator.constants.Constants;
import es.uvigo.ei.sing.singulator.interfaces.iMolecule;
import es.uvigo.ei.sing.singulator.json.JsonAgent;
import es.uvigo.ei.sing.singulator.json.JsonCell;
import es.uvigo.ei.sing.singulator.json.JsonAlignment;
import es.uvigo.ei.sing.singulator.json.JsonDiffusionRate;
import es.uvigo.ei.sing.singulator.json.JsonFeeder;
import es.uvigo.ei.sing.singulator.json.JsonKill;
import es.uvigo.ei.sing.singulator.json.JsonLayer;
import es.uvigo.ei.sing.singulator.json.JsonMolecule;
import es.uvigo.ei.sing.singulator.json.JsonReaction;
import es.uvigo.ei.sing.singulator.json.JsonRibosome;
import es.uvigo.ei.sing.singulator.json.JsonTransform;
import es.uvigo.ei.sing.singulator.json.JsonTransporter;
import es.uvigo.ei.sing.singulator.json.JsonUnity;
import es.uvigo.ei.sing.singulator.modules.distribution.CreateCellHelper;
import es.uvigo.ei.sing.singulator.modules.distribution.CreateDoorHelper;
import es.uvigo.ei.sing.singulator.modules.distribution.CreateMoleculeHelper;
import es.uvigo.ei.sing.singulator.modules.distribution.SpaceExtents3D;
import es.uvigo.ei.sing.singulator.utils.Functions;
import sim.util.Double3D;

public class SINGulator_Initialize implements Serializable {

	private static final long serialVersionUID = 1L;

	private SpaceExtents3D sExtents;
	private int availableProcessorsCreation;
	public SINGulator_Logic parent;

	// [0]: Radius, [1]: DR0...[8]:DR7
	public double[] unityInfo;

	public SINGulator_Initialize(SINGulator_Logic parent) {
		this.parent = parent;
		this.availableProcessorsCreation = parent.parent.availableProcessorsCreation;
		this.sExtents = parent.parent.sExtents;
		this.unityInfo = new double[8];
	}

	public void distributeCells(Stack<Cell> toInsert) {
		// TODO: INTEGER PARA VER SI ENTRARON TODAS
		new CreateCellHelper(0.0, parent.parent.width, 0.0, parent.parent.height, 0.0, parent.parent.length, toInsert,
				parent.parent.totalTries, parent.parent).calculateValidLocation();
	}

	public void distributeMolecules(Stack<iMolecule> toDistribute, Collection<Cell> cells) {
		AtomicInteger totalAgents = new AtomicInteger(toDistribute.size());
		ExecutorService es = Executors.newFixedThreadPool(availableProcessorsCreation);
		Collections.shuffle(toDistribute, new Random(System.nanoTime()));

		for (int i = 0; i < availableProcessorsCreation; i++) {
			es.execute(new CreateMoleculeHelper(sExtents.x0(i), sExtents.xlim(i), sExtents.y0(i), sExtents.ylim(i),
					sExtents.z0(i), sExtents.zlim(i), i, toDistribute, cells, parent.parent.totalTries, totalAgents,
					parent.parent));
		}

		es.shutdown();
		try {
			es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

			if (totalAgents.get() != 0) {
				// Functions.showErrorDialog(parent.parent.canGUI,
				// Constants.MOLECULE);
				System.err.println("NO SE HAN INSERTADO TODAS LAS MOLECULAS");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void distributeDoors(Map<Cell, List<Door>> mapCellDoors) {
		// Count number of total doors
		int occurrences = 0;
		for (List<Door> listOfCells : mapCellDoors.values()) {
			occurrences += listOfCells.size();
		}

		AtomicInteger totalAgents = new AtomicInteger(occurrences);
		ExecutorService es = Executors.newFixedThreadPool(availableProcessorsCreation);
		Stack<Door> toInsert = new Stack<Door>();
		Set<Cell> cells = mapCellDoors.keySet();

		// One cell per thread
		for (Cell currentCell : cells) {
			toInsert = new Stack<Door>();
			toInsert.addAll(mapCellDoors.get(currentCell));

			es.execute(new CreateDoorHelper(0.0, parent.parent.width, 0.0, parent.parent.height, 0.0,
					parent.parent.length, toInsert, currentCell, parent.parent.totalTries, totalAgents, parent.parent));
		}

		es.shutdown();
		try {
			es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

			if (totalAgents.get() != 0) {
				// Functions.showErrorDialog(parent.parent.canGUI,
				// Constants.NO_SPACE_DOOR);
				System.err.println("NO SE HAN INSERTADO TODAS LAS PUERTAS");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void extractUnityInformation() {
		SINGulator_Model rs = parent.parent;
		double radius, molecularWeight;

		JsonUnity unity = rs.singulator.getUnity();

		// Dar preferencia al peso molecular sobre el radio
		radius = unity.getRadius();
		if (radius != -1) {
			// toRet[0] =
			// Functions.calculateRealRadiusFromTheoricalRadius(radius);
			unityInfo[0] = radius;
		}
		molecularWeight = unity.getMolecularWeight();
		if (molecularWeight != -1) {
			// toRet[0] = Functions.calculateRealRadiusFromMW(molecularWeight);
		}

		JsonDiffusionRate dr = unity.getDiffusionRate();

		unityInfo[1] = dr.getExterior();
		unityInfo[2] = dr.getOuterMembrane();
		unityInfo[3] = dr.getOuterPeriplasm();
		unityInfo[4] = dr.getPeptidoglycan();
		unityInfo[5] = dr.getInnerPeriplasm();
		unityInfo[6] = dr.getInnerMembrane();
		unityInfo[7] = dr.getCytoplasm();
	}

	public void extractCellPosition() {
		SINGulator_Model rs = parent.parent;
		String axis;
		double distance;
		double half_sphero;

		JsonAlignment cellPosition = rs.singulator.getAlignment();

		// Retrieve cell positioning information
		axis=cellPosition.getAxis();
		distance=cellPosition.getDistance();
		half_sphero=cellPosition.getHalf_sphero();
	}


	public List<Feeder> extractFeederFromInformation() {
		List<Feeder> toRet = new ArrayList<Feeder>();
		SINGulator_Model rs = parent.parent;

		JsonFeeder[] feeders = rs.singulator.getFeeder();
		for (JsonFeeder feeder : feeders) {
			toRet.add(new Feeder(feeder.getCreate(), feeder.getType(), feeder.getLocation(), feeder.getEveryStep(),
					feeder.getProductionNumber(), feeder.getMaxConcentration()));
		}

		return toRet;
	}

	public Stack<Cell> extractCellInformation() throws IllegalArgumentException, FileNotFoundException {
		Stack<Cell> toInsert = new Stack<Cell>();
		SINGulator_Model rs = parent.parent;

		String cellName, layerName, color, form;
		double scale, radius, height;
		int id = 0, cellNumber;
		Double3D location = null;
		List<Cell> createdCells;
		Cell cell = null;
		CellLayer layer = null;

		// Set exterior layer in maps
		rs.mapLayerMolecules.put(0, 0);

		JsonCell[] jsonCells = rs.singulator.getCells();
		for (JsonCell jsonCell : jsonCells) {
			createdCells = new ArrayList<Cell>();

			// Retrieve cell information
			cellName = jsonCell.getCellName();
			layerName = jsonCell.getLayerName();

			// Get radius and calculate simRadius for cell
			radius = jsonCell.getRadius();
			// radius =
			// Functions.calculateRealRadiusFromTheoricalRadius(radius);
			radius = radius / unityInfo[0];
			// Get height and calculate simHeight for cell
			height = jsonCell.getHeight();
			// height =
			// Functions.calculateRealRadiusFromTheoricalRadius(height);
			height = height / unityInfo[0];

			color = jsonCell.getColor();
			cellNumber = jsonCell.getNumber();

			form = jsonCell.getForm();
			if (form.equals(Constants.CAPSULE)) {
				// Scale of the capsule (height + 2 * radius)
				// To calculate radius: scale o height/2. EJ: h=1, r=0.25
				// s=30
				// h=30*1,
				// r=r*2*30
				scale = height + radius * 2;
			} else if (form.equals(Constants.SPHERE)) {
				// Same as diameter
				scale = radius * 2;
			} else {
				// Same as radius
				scale = radius;
			}

			// Create cells
			for (int count = 0; count < cellNumber; count++) {
				// Create cell
				cell = new Cell(cellName, layerName, scale, radius, height, color, id, form);

				createdCells.add(cell);
				id++;
				rs.mapLayerMolecules.put(Constants.mapLayerNameLayerId.get(layerName), 0);
			}

			JsonLayer[] jsonLayers = jsonCell.getLayers();
			// Go over layers for the cell
			for (JsonLayer jsonLayer : jsonLayers) {
				layerName = jsonLayer.getName();
				// Get radius and calculate simRadius for cell
				radius = jsonLayer.getRadius();
				// radius =
				// Functions.calculateRealRadiusFromTheoricalRadius(radius);
				radius = radius / unityInfo[0];
				// Get height and calculate simHeight for cell
				height = jsonLayer.getHeight();
				// height =
				// Functions.calculateRealRadiusFromTheoricalRadius(height);
				height = height / unityInfo[0];

				color = jsonLayer.getColor();

				if (form.equals(Constants.CAPSULE)) {
					scale = height + radius * 2;
				} else if (form.equals(Constants.SPHERE)) {
					// Same as diameter
					scale = radius * 2;
				} else {
					// Same as radius
					scale = radius;
				}

				for (Cell createdCell : createdCells) {
					location = createdCell.getLocation();
					layer = new CellLayer(layerName, scale, radius, height, color,
							Constants.mapLayerNameLayerId.get(layerName), location, createdCell);

					createdCell.putLayer(Constants.mapLayerNameLayerId.get(layerName), layer);
				}
				rs.mapLayerMolecules.put(Constants.mapLayerNameLayerId.get(layerName), 0);
			}
			toInsert.addAll(createdCells);
		}

		return toInsert;
	}

	public Stack<iMolecule> extractMoleculeInformation(Map<String, String[]> molecules, AtomicInteger agentID) {
		Stack<iMolecule> toRet = new Stack<iMolecule>();

		SINGulator_Model rs = parent.parent;

		String moleculeName, moleculeColor, initialLocalization, radInflWith;
		int moleculeNumber, moleculeMaxLayer, moleculeMinLayer, layerLocalization, radInfl;
		double moleculeMW = -1, moleculeRadius = -1, aux;
		double drExterior, drOuterMembrane, drOuterPeriplasm, drPeptidoglycan, drInnerPeriplasm, drInnerMembrane,
				drCytoplasm;

		JsonAgent jsonAgents = rs.singulator.getAgents();
		JsonMolecule[] jsonMolecules = jsonAgents.getMolecules();
		JsonDiffusionRate jsonDiffRate;
		// Go over each agent molecules
		for (JsonMolecule jsonMolecule : jsonMolecules) {
			moleculeName = jsonMolecule.getName();

			aux = jsonMolecule.getRadius();
			if (aux != -1) {
				// Calculate simRadius based on theorical radius
				// moleculeRadius =
				// Functions.calculateRealRadiusFromTheoricalRadius(aux);

				// moleculeMW = Functions.calculateMWFromRealRadius(aux);

				// Calculate theoricalRadius based on MW
				// moleculeRadius =
				// Functions.calculateRealRadiusFromMW(moleculeMW);
				moleculeRadius = aux;
			}
			aux = jsonMolecule.getMolecularWeight();
			if (aux != -1) {
				moleculeMW = aux;

				// Calculate theoricalRadius based on MW
				// moleculeRadius =
				// Functions.calculateRealRadiusFromMW(moleculeMW);
			}

			jsonDiffRate = jsonMolecule.getDiffusionRate();

			drExterior = jsonDiffRate.getExterior();
			if (drExterior == -1) {
				drExterior = Functions.calculateDRUsingStokesExtracellular(moleculeRadius);
				drExterior = drExterior / unityInfo[1];
			}
			drOuterMembrane = jsonDiffRate.getOuterMembrane();
			if (drOuterMembrane == -1) {
				drOuterMembrane = Functions.calculateDRUsingStokesIntracellular(moleculeRadius);
				drOuterMembrane = drOuterMembrane / unityInfo[2];
			}
			drOuterPeriplasm = jsonDiffRate.getOuterPeriplasm();
			if (drOuterPeriplasm == -1) {
				drOuterPeriplasm = Functions.calculateDRUsingStokesIntracellular(moleculeRadius);
				drOuterPeriplasm = drOuterPeriplasm / unityInfo[3];
			}
			drPeptidoglycan = jsonDiffRate.getPeptidoglycan();
			if (drPeptidoglycan == -1) {
				drPeptidoglycan = Functions.calculateDRUsingStokesIntracellular(moleculeRadius);
				drPeptidoglycan = drPeptidoglycan / unityInfo[4];
			}
			drInnerPeriplasm = jsonDiffRate.getInnerPeriplasm();
			if (drInnerPeriplasm == -1) {
				drInnerPeriplasm = Functions.calculateDRUsingStokesIntracellular(moleculeRadius);
				drInnerPeriplasm = drInnerPeriplasm / unityInfo[5];
			}
			drInnerMembrane = jsonDiffRate.getInnerMembrane();
			if (drInnerMembrane == -1) {
				drInnerMembrane = Functions.calculateDRUsingStokesIntracellular(moleculeRadius);
				drInnerMembrane = drInnerMembrane / unityInfo[6];
			}
			drCytoplasm = jsonDiffRate.getCytoplasm();
			if (drCytoplasm == -1) {
				drCytoplasm = Functions.calculateDRUsingStokesIntracellular(moleculeRadius);
				drCytoplasm = drCytoplasm / unityInfo[7];
			}

			moleculeColor = jsonMolecule.getColor();
			moleculeNumber = jsonMolecule.getNumber();
			moleculeMaxLayer = Constants.mapLayerNameLayerId.get(jsonMolecule.getMaxLayer());
			moleculeMinLayer = Constants.mapLayerNameLayerId.get(jsonMolecule.getMinLayer());

			// Calculate simRadius
			moleculeRadius = moleculeRadius / unityInfo[0];

			initialLocalization = jsonMolecule.getCellLocalization();
			layerLocalization = Constants.mapLayerNameLayerId.get(jsonMolecule.getLayerLocalization());

			// Obtener radio de influencia
			radInfl = jsonMolecule.getRadInfl();
			radInflWith = jsonMolecule.getRadInflWith();

			// Save molecule in moleculesInformation map
			molecules.put(moleculeName,
					new String[] { moleculeName, String.valueOf(moleculeMW), String.valueOf(moleculeRadius),
							String.valueOf(drExterior), String.valueOf(drOuterMembrane),
							String.valueOf(drOuterPeriplasm), String.valueOf(drPeptidoglycan),
							String.valueOf(drInnerPeriplasm), String.valueOf(drInnerMembrane),
							String.valueOf(drCytoplasm), moleculeColor, String.valueOf(moleculeNumber),
							String.valueOf(moleculeMaxLayer), String.valueOf(moleculeMinLayer), String.valueOf(radInfl),
							radInflWith, Constants.MOLECULE, "" });

			// Create entry with zero value in moleculesNumber and
			// moleculesReaction map
			rs.mapNameNumber.put(moleculeName, 0);
			// Create entry with zero value in moleculesNumber and
			// moleculesReaction map
			rs.mapNameNumber.put(moleculeName, 0);
			rs.mapNameReactionNumber.put(moleculeName, 0);

			// TODO: CREAR TANTAS COMO CELULAS HAYA Y COMO LA DISTRIBUCION
			// INDIQUE
			for (int y = 0; y < moleculeNumber; y++) {
				if (Constants.CAN_GUI) {
					toRet.add(new SphereMolecule(agentID.getAndIncrement(), moleculeName, moleculeMW, moleculeRadius,
							moleculeColor, drExterior, drOuterMembrane, drOuterPeriplasm, drPeptidoglycan,
							drInnerPeriplasm, drInnerMembrane, drCytoplasm, moleculeMaxLayer, moleculeMinLayer,
							rs.random.nextInt(360), rs.random.nextInt(360), layerLocalization, initialLocalization,
							radInfl, radInflWith, Constants.MOLECULE, ""));
				} else {
					toRet.add(new Molecule(agentID.getAndIncrement(), moleculeName, moleculeMW, moleculeRadius,
							moleculeColor, drExterior, drOuterMembrane, drOuterPeriplasm, drPeptidoglycan,
							drInnerPeriplasm, drInnerMembrane, drCytoplasm, moleculeMaxLayer, moleculeMinLayer,
							rs.random.nextInt(360), rs.random.nextInt(360), layerLocalization, initialLocalization,
							radInfl, radInflWith, Constants.MOLECULE, ""));
				}
			}
		}

		return toRet;
	}

	public Stack<iMolecule> extractRibosomeInformation(Map<String, String[]> molecules, AtomicInteger agentID) {
		Stack<iMolecule> toRet = new Stack<iMolecule>();

		SINGulator_Model rs = parent.parent;

		String name, color, initialLocalization, radInflWith, mRNA;
		int number, maxLayer, minLayer, layerLocalization, radInfl;
		double mW = -1, radius = -1, aux;
		double drExterior, drOuterMembrane, drOuterPeriplasm, drPeptidoglycan, drInnerPeriplasm, drInnerMembrane,
				drCytoplasm;

		JsonAgent jsonAgents = rs.singulator.getAgents();
		JsonRibosome[] jsonRibosomes = jsonAgents.getRibosomes();
		JsonDiffusionRate jsonDiffRate;
		// Go over each agent molecules
		for (JsonRibosome jsonRibosome : jsonRibosomes) {
			name = jsonRibosome.getName();

			aux = jsonRibosome.getRadius();
			if (aux != -1) {
				// Calculate simRadius based on theorical radius
				// moleculeRadius =
				// Functions.calculateRealRadiusFromTheoricalRadius(aux);

				// moleculeMW = Functions.calculateMWFromRealRadius(aux);

				// Calculate theoricalRadius based on MW
				// moleculeRadius =
				// Functions.calculateRealRadiusFromMW(moleculeMW);
				radius = aux;
			}
			aux = jsonRibosome.getMolecularWeight();
			if (aux != -1) {
				mW = aux;

				// Calculate theoricalRadius based on MW
				// moleculeRadius =
				// Functions.calculateRealRadiusFromMW(moleculeMW);
			}

			drExterior = jsonRibosome.getDiffusionRate();
			if (drExterior == -1) {
				drExterior = Functions.calculateDRUsingStokesExtracellular(radius);
				drExterior = drExterior / unityInfo[1];
			}
			drOuterMembrane = jsonRibosome.getDiffusionRate();
			if (drOuterMembrane == -1) {
				drOuterMembrane = Functions.calculateDRUsingStokesIntracellular(radius);
				drOuterMembrane = drOuterMembrane / unityInfo[2];
			}
			drOuterPeriplasm = jsonRibosome.getDiffusionRate();
			if (drOuterPeriplasm == -1) {
				drOuterPeriplasm = Functions.calculateDRUsingStokesIntracellular(radius);
				drOuterPeriplasm = drOuterPeriplasm / unityInfo[3];
			}
			drPeptidoglycan = jsonRibosome.getDiffusionRate();
			if (drPeptidoglycan == -1) {
				drPeptidoglycan = Functions.calculateDRUsingStokesIntracellular(radius);
				drPeptidoglycan = drPeptidoglycan / unityInfo[4];
			}
			drInnerPeriplasm = jsonRibosome.getDiffusionRate();
			if (drInnerPeriplasm == -1) {
				drInnerPeriplasm = Functions.calculateDRUsingStokesIntracellular(radius);
				drInnerPeriplasm = drInnerPeriplasm / unityInfo[5];
			}
			drInnerMembrane = jsonRibosome.getDiffusionRate();
			if (drInnerMembrane == -1) {
				drInnerMembrane = Functions.calculateDRUsingStokesIntracellular(radius);
				drInnerMembrane = drInnerMembrane / unityInfo[6];
			}
			drCytoplasm = jsonRibosome.getDiffusionRate();
			if (drCytoplasm == -1) {
				drCytoplasm = Functions.calculateDRUsingStokesIntracellular(radius);
				drCytoplasm = drCytoplasm / unityInfo[7];
			}

			color = jsonRibosome.getColor();
			number = jsonRibosome.getNumber();
			maxLayer = Constants.mapLayerNameLayerId.get(jsonRibosome.getMaxLayer());
			minLayer = Constants.mapLayerNameLayerId.get(jsonRibosome.getMinLayer());

			// Calculate simRadius
			radius = radius / unityInfo[0];

			initialLocalization = jsonRibosome.getCellLocalization();
			layerLocalization = Constants.mapLayerNameLayerId.get(jsonRibosome.getLayerLocalization());

			// Obtener radio de influencia
			radInfl = jsonRibosome.getRadInfl();
			radInflWith = jsonRibosome.getRadInflWith();

			// Obtener mRNA
			mRNA = jsonRibosome.getmRNA();

			// Save molecule in moleculesInformation map
			molecules.put(name,
					new String[] { name, String.valueOf(mW), String.valueOf(radius), String.valueOf(drExterior),
							String.valueOf(drOuterMembrane), String.valueOf(drOuterPeriplasm),
							String.valueOf(drPeptidoglycan), String.valueOf(drInnerPeriplasm),
							String.valueOf(drInnerMembrane), String.valueOf(drCytoplasm), color, String.valueOf(number),
							String.valueOf(maxLayer), String.valueOf(minLayer), String.valueOf(radInfl), radInflWith,
							Constants.RIBOSOME, mRNA });

			// Create entry with zero value in moleculesNumber and
			// moleculesReaction map
			rs.mapNameNumber.put(name, 0);
			// Create entry with zero value in moleculesNumber and
			// moleculesReaction map
			rs.mapNameNumber.put(name, 0);
			rs.mapNameReactionNumber.put(name, 0);

			// TODO: CREAR TANTAS COMO CELULAS HAYA Y COMO LA DISTRIBUCION
			// INDIQUE
			for (int y = 0; y < number; y++) {
				if (Constants.CAN_GUI) {
					toRet.add(new SphereMolecule(agentID.getAndIncrement(), name, mW, radius, color, drExterior,
							drOuterMembrane, drOuterPeriplasm, drPeptidoglycan, drInnerPeriplasm, drInnerMembrane,
							drCytoplasm, maxLayer, minLayer, rs.random.nextInt(360), rs.random.nextInt(360),
							layerLocalization, initialLocalization, radInfl, radInflWith, Constants.RIBOSOME, mRNA));
				} else {
					toRet.add(new Molecule(agentID.getAndIncrement(), name, mW, radius, color, drExterior,
							drOuterMembrane, drOuterPeriplasm, drPeptidoglycan, drInnerPeriplasm, drInnerMembrane,
							drCytoplasm, maxLayer, minLayer, rs.random.nextInt(360), rs.random.nextInt(360),
							layerLocalization, initialLocalization, radInfl, radInflWith, Constants.RIBOSOME, mRNA));
				}
			}
		}

		return toRet;
	}

	public Map<Cell, List<Door>> extractDoorInformation(AtomicInteger agentID) {
		Map<Cell, List<Door>> toRet = new HashMap<Cell, List<Door>>();
		List<Door> cellDoors;
		SINGulator_Model rs = parent.parent;

		String doorName, doorCellName, doorColor, doorType;
		String[] doorInputs, doorOutputs;
		int doorNumber, doorOuterLayer, doorInnerLayer, doorGetFrom, doorPutTo;
		double doorRadius, doorDiffRate;

		Door door = null;

		Collection<Cell> createdCells = rs.mapIdCell.values();
		// Insert cells in return map
		for (Cell cell : createdCells) {
			toRet.put(cell, new ArrayList<Door>());
		}

		JsonTransporter[] jsonTransporters = rs.singulator.getTransporters();
		// Go over each agent type
		for (JsonTransporter jsonTransporter : jsonTransporters) {
			doorName = jsonTransporter.getName();
			doorCellName = jsonTransporter.getCellName();
			// Calculate simRadius based on theorical radius
			doorRadius = jsonTransporter.getRadius();
			// doorRadius =
			// Functions.calculateRealRadiusFromTheoricalRadius(doorRadius);

			doorDiffRate = jsonTransporter.getDiffusionRate();
			if (doorDiffRate == -1) {
				doorDiffRate = Functions.calculateDRUsingStokesIntracellular(doorRadius);
				// TODO: SE DIVIDE CONTRA EL CITOPLASMA
				doorDiffRate = doorDiffRate / unityInfo[7];
			}
			doorRadius = doorRadius / unityInfo[0];

			doorColor = jsonTransporter.getColor();
			doorNumber = jsonTransporter.getNumber();
			doorOuterLayer = Constants.mapLayerNameLayerId.get(jsonTransporter.getOuterLayer());
			doorInnerLayer = Constants.mapLayerNameLayerId.get(jsonTransporter.getInnerLayer());
			doorGetFrom = Constants.mapLayerNameLayerId.get(jsonTransporter.getGetFrom());
			doorPutTo = Constants.mapLayerNameLayerId.get(jsonTransporter.getPutTo());
			doorType = jsonTransporter.getType();
			doorInputs = jsonTransporter.getInputs();
			doorOutputs = jsonTransporter.getOutputs();

			// Create entry with zero value in mapDoorNumber
			rs.mapDoorNumber.put(doorName, 0);

			for (Cell cell : toRet.keySet()) {
				if (doorCellName.contains(cell.getCellName())) {
					cellDoors = new ArrayList<Door>();
					// Create all doors
					for (int y = 0; y < doorNumber; y++) {
						// Create door object
						door = new Door(doorName, doorRadius, doorDiffRate, doorColor, doorInnerLayer, doorOuterLayer,
								doorGetFrom, doorPutTo, doorType, doorInputs, doorOutputs, agentID.getAndIncrement());

						cellDoors.add(door);
					}

					cellDoors.addAll(toRet.get(cell));
					toRet.put(cell, cellDoors);
				}
			}
		}

		return toRet;
	}

	public List<String[]> extractKillEvents() {
		List<String[]> toRet = new ArrayList<String[]>();
		SINGulator_Model rs = parent.parent;
		String[] toList;

		JsonKill[] jsonKills = rs.singulator.getEvents().getKill();
		for (JsonKill jsonKill : jsonKills) {
			toList = new String[3];
			toList[0] = jsonKill.getInput();
			toList[1] = jsonKill.getTrigger();
			toList[2] = jsonKill.getWith();

			toRet.add(toList);
		}

		return toRet;
	}

	public List<String[]> extractReactionEvents() {
		List<String[]> toRet = new ArrayList<String[]>();
		SINGulator_Model rs = parent.parent;
		String[] toList;
		String[] aux;

		JsonReaction[] jsonReactions = rs.singulator.getEvents().getReaction();
		for (JsonReaction jsonReaction : jsonReactions) {
			toList = new String[4];
			aux = jsonReaction.getOnCollision();
			for (String str : aux) {
				if (toList[0] == null) {
					toList[0] = str;
				} else {
					toList[0] += "\t" + str;
				}
			}
			aux = jsonReaction.getOutput();
			for (String str : aux) {
				if (toList[1] == null) {
					toList[1] = str;
				} else {
					toList[1] += "\t" + str;
				}
			}
			toList[2] = String.valueOf(jsonReaction.getKm());
			toList[3] = String.valueOf(jsonReaction.getKcat());

			toRet.add(toList);
		}

		return toRet;
	}

	public List<String[]> extractTranformEvents() {
		List<String[]> toRet = new ArrayList<String[]>();
		SINGulator_Model rs = parent.parent;
		String[] toList;

		JsonTransform[] jsonTransforms = rs.singulator.getEvents().getTransform();
		for (JsonTransform jsonTransform : jsonTransforms) {
			toList = new String[4];
			toList[0] = jsonTransform.getFrom();
			toList[1] = jsonTransform.getTo();
			toList[2] = jsonTransform.getTrigger();
			toList[3] = jsonTransform.getWith();

			toRet.add(toList);
		}

		return toRet;
	}
}
