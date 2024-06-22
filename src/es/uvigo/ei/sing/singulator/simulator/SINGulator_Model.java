package es.uvigo.ei.sing.singulator.simulator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import es.uvigo.ei.sing.singulator.agents.Cell;
import es.uvigo.ei.sing.singulator.agents.Feeder;
import es.uvigo.ei.sing.singulator.constants.Constants;
import es.uvigo.ei.sing.singulator.interfaces.iMolecule;
import es.uvigo.ei.sing.singulator.json.JsonEnvironment;
import es.uvigo.ei.sing.singulator.json.JsonGeneralConfiguration;
import es.uvigo.ei.sing.singulator.json.JsonSingulator;
import es.uvigo.ei.sing.singulator.json.JsonUnity;
import es.uvigo.ei.sing.singulator.json.JsonML;
import es.uvigo.ei.sing.singulator.modules.distribution.ExecuteAll;
import es.uvigo.ei.sing.singulator.modules.distribution.SpaceExtents3D;
import es.uvigo.ei.sing.singulator.modules.distribution.Sector;
import es.uvigo.ei.sing.singulator.utils.CustomMap;
import es.uvigo.ei.sing.singulator.utils.Functions;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous3D;
import sim.util.Bag;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SINGulator_Model extends SimState {

    private static final long serialVersionUID = 1L;

    public double width;
    public double height;
    public double length;

    public int totalTries;
    public int availableProcessorsCreation;
    public int availableProcessors;
    public int saveSimulationEvery;
    public int writeResultsEvery;
    public int reactions;
    public int escapedAgents;
    public int randomRebound;
    public int stepRandomRebound;

    public boolean activateGUI;
    public boolean canGUI;
    public boolean hasCells;
    public boolean hasFeeder;
    public boolean hasTransporters;
    public boolean hasEvents;
    public boolean hasRibosome;

    public String fileSuffix;
    public String fileResultsName;
    public String outputPath;
    public String resultsFilePath;
    public String displacementFilePath;
    public String dataFilePath;
    public String zoneTimeStepPath;
    public String simulationName;
    public String simulationType;

    public Set<String> emailTo;
    public Set<iMolecule> deadAgents;
    public Set<iMolecule> toCreateAgents;

    public boolean flag_ml;
    public String mlOutput;
    public String python;
    public Integer jump;
    public Integer ts;
    public String option;
    public String json;
    public String model;
    public String data;



    public Map<Integer, Integer> mapLayerMolecules;
    public Map<Integer, String> layerZoneNameMap;
    public Map<String, Integer> mapDoorNumber;
    public Map<Integer, Cell> mapIdCell;
    // Da las moleculas que no están reaccionando
    public Map<String, Integer> mapNameNumber;
    // Da las moleculas que están reaccionando
    public Map<String, Integer> mapNameReactionNumber;
    // Map molName:molInformation
    public Map<String, String[]> mapNameInformation;
    public Map<Integer, String> mapIDLastRebound;
    public Map<Integer, String> mapIDLastReboundEnvironment;

    public Continuous3D environment = null;
    public SINGulator_Logic simulatorLogic;
    public SpaceExtents3D sExtents;
    public List<Feeder> feederList;
    public JsonSingulator singulator;

    // PCQUORUM DEPENDANCE
    boolean finish = false;

    public SINGulator_Model(long seed, JsonSingulator singulator, boolean canGUI) {
        super(seed);

        this.singulator = singulator;
        this.canGUI = canGUI;

        // Calculate environment sizes
        calculateEnvironmentSize(singulator);


        JsonML ml = singulator.getML();
        this.flag_ml = ml.isMl();
        this.mlOutput= ml.getMlOutput();
        this.python = ml.getPython();
        this.jump = ml.getJump();
        this.ts = ml.getTs();
        this.option = ml.getOption();
        this.json = ml.getJson();
        this.model = ml.getModel();
        this.data = ml.getData();


        JsonGeneralConfiguration configuration = singulator.getGeneralConfiguration();
        this.totalTries = configuration.getTotalTries();
        this.availableProcessors = configuration.getProcSim();
        this.availableProcessorsCreation = configuration.getProcCreation();
        this.writeResultsEvery = configuration.getWriteResultsEvery();

        try {
            this.randomRebound = configuration.getRandomRebound();
            System.out.println("Rebotes aleatorios en " + this.randomRebound + "% de moleculas");
        } catch (NullPointerException ex) {
            System.out.println("No se ha detectado valor para aletaoriedad en choques de simulados de moleculas. Generando aletorio...");
            Random r = new Random();
            this.randomRebound = r.nextInt(101);
            System.out.println("Rebotes aleatorios en " + this.randomRebound + "% de moleculas");
        }

        try {
            this.stepRandomRebound = configuration.getStepsRandomRebound();
            System.out.println("Rebotes aleatorios cada " + this.stepRandomRebound + " pasos");
        } catch (NullPointerException ex) {
            System.out.println("No se ha detectado valor para pasos entre choques simulados. Generando aletorio...");
            Random r = new Random();
            this.stepRandomRebound = r.nextInt(401);
            System.out.println("Rebotes aleatorios cada " + this.stepRandomRebound + " pasos");
        }

        String[] emails = configuration.getEmailTo();
        emailTo = new HashSet<String>();
        for (String email : emails) {
            if (!email.isEmpty()) emailTo.add(email);
        }

        this.outputPath = configuration.getDirOutput();
        this.fileResultsName = configuration.getFileOutput();
        this.activateGUI = configuration.isActivateGUI();
        this.simulationName = configuration.getSimName();
        this.simulationType = configuration.getSimulationType();

        this.mapNameInformation = new HashMap<String, String[]>();
        this.mapIDLastRebound = new HashMap<Integer, String>();
        this.mapIDLastReboundEnvironment = new HashMap<Integer, String>();
        if (singulator.getCells() != null) {
            this.hasCells = true;
        }
        if (singulator.getTransporters() != null) {
            this.hasTransporters = true;
        }
        if (singulator.getEvents() != null) {
            this.hasEvents = true;
        }
        if (singulator.getFeeder() != null) {
            this.hasFeeder = true;
        }
        if (singulator.getAgents().getRibosomes() != null) {
            this.hasRibosome = true;
        }
    }

    @Override
    public void start() {
        // Clear out the schedule
        super.start();
        System.out.println("Starting...");

        // Cut the board in pieces per processor for creation step
        this.sExtents = new SpaceExtents3D(availableProcessorsCreation, width, height, length);

        // Create variables
        this.mapNameNumber = new CustomMap();
        this.mapNameReactionNumber = new CustomMap();
        this.mapNameNumber = new CustomMap();
        this.mapLayerMolecules = new CustomMap();
        this.mapDoorNumber = new CustomMap();
        this.mapIdCell = new HashMap<Integer, Cell>();
        this.fileSuffix = String.valueOf(System.currentTimeMillis()) + "_";
        this.deadAgents = Collections.synchronizedSet(new HashSet<iMolecule>());
        this.toCreateAgents = new HashSet<iMolecule>();
        this.layerZoneNameMap = new HashMap<Integer, String>();
        // Fill variables with default information
        fillZoneMaps();

        // Crear simulationLogic
        simulatorLogic = new SINGulator_Logic(this);

        // Cargar datos de la unidad
        simulatorLogic.initialize.extractUnityInformation();
        // Cargar datos de moleculas y ribosomas (agentes que se mueven y
        // rebotan)
        Stack<iMolecule> molecules;
        molecules = simulatorLogic.extractMoleculeInformation(mapNameInformation);
        if (hasRibosome) molecules.addAll(simulatorLogic.extractRibosomeInformation(mapNameInformation));

        // Create the environment with the standard discretization (agent max
        // radius * 2)
        environment = new Continuous3D(100.0, width, height, length);

        // Load agents file
        try {
            // Cargar información restante y crear agentes
            simulatorLogic.loadConfigurationFile(molecules);

            if (hasEvents) {
                simulatorLogic.loadEventsFile();
            }
            if (hasFeeder) {
                this.feederList = simulatorLogic.loadFeederFromFile();
            }

            // Cut the board in pieces per processor for execution step
            this.sExtents = new SpaceExtents3D(availableProcessors, width, height, length);

            // First to go: ExecuteAll
            schedule.scheduleRepeating(Schedule.EPOCH, 1, new ExecuteAll(this));
            // Last to go: Anonymous agent to write results and stop the
            // simulation if needed
            schedule.scheduleRepeating(Schedule.EPOCH, 2, new Steppable() {
                private static final long serialVersionUID = 1L;

                @Override
                public void step(SimState arg0) {
                    long steps = schedule.getSteps();

                    if (finish) {
                        try {
                            System.out.println("TOTAL TIMESTEPS: " + steps);
                            Files.write(Paths.get(outputPath, fileResultsName + "_" + fileSuffix + "_lastLocation_" + steps + "ts.txt"), mapIDLastRebound.values(), StandardOpenOption.CREATE_NEW);
                            Files.write(Paths.get(outputPath, fileResultsName + "_" + fileSuffix + "_lastLocation_Environment_" + steps + "ts.txt"), mapIDLastReboundEnvironment.values(), StandardOpenOption.CREATE_NEW);

                            List<String> cellConsumed = new ArrayList<>();
                            for (Cell cell : mapIdCell.values()) {
                                cellConsumed.add(cell.getCellName() + "\t" + cell.getId() + "\t" + cell.getConsumed());
                            }
                            Files.write(Paths.get(outputPath, fileResultsName + "_" + fileSuffix + "_cellConsumed_" + steps + "ts.txt"), cellConsumed, StandardOpenOption.CREATE_NEW);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        finish();
                    }
                    if (mapNameNumber.get("Macromoleculeahl") == 0) {
                        finish = true;
                    }

                    // Write results in output file
                    if (steps % writeResultsEvery == 0) {
                        // PROVISIONAL PARA SACAR VECINDAD
                        //writeFinishedResults();

                        String resultsName = fileResultsName + "_" + fileSuffix + ".txt";

                        // Result file creation
                        File file = new File(outputPath + resultsName);

                        resultsFilePath = file.getAbsolutePath();

                        List<String> toWrite = new ArrayList<String>();

                        String header = "#TimeSteps\t", value = String.valueOf(steps) + "\t";

                        // Go over layers name map
                        for (Integer layerName : mapLayerMolecules.keySet()) {
                            header += layerZoneNameMap.get(layerName) + "\t";
                        }
                        // Go over layers name map
                        for (Integer layerNumber : mapLayerMolecules.values()) {
                            value += layerNumber + "\t";
                        }
                        // Go over mapDoor and retrieve data
                        for (String doorName : mapDoorNumber.keySet()) {
                            header += doorName + "\t";
                            value += mapDoorNumber.get(doorName) + "\t";
                        }
                        String reactionHeader = "", reactionValue = "";
                        // Go over mapMolecule and retrieve data
                        for (String molName : mapNameNumber.keySet()) {
                            header += "Non reaction " + molName + "\t";
                            value += mapNameNumber.get(molName) + "\t";

                            reactionHeader += "Reaction " + molName + "\t";
                            // Same map structure as mapNameNumber
                            reactionValue += mapNameReactionNumber.get(molName) + "\t";
                        }

                        header += reactionHeader + "Escaped";
                        value += reactionValue + escapedAgents;

                        try {
                            // Validate if checkpoint exists
                            if (!file.exists()) {
                                Files.createFile(Paths.get(file.getPath()));

                                // Write headers
                                toWrite.add(header);
                            }

                            // Write results
                            toWrite.add(value);

                            Files.write(Paths.get(file.getPath()), toWrite, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(option.equals("train") && steps % ts == 0) {
                        //Data Training
                        registerDataSimulation(0);
                    }

                    if(flag_ml == true && steps !=0 && steps % ts == 0) {
                        //Script de Python"
                        registerDataSimulation(1);
                        executePythonScript();
                        steps += jump + 1;
                    }

                }
            });
            System.out.println("Setting all in the environment...");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Fallo al leer el fichero");
            e.printStackTrace();
        }
    }

    public void registerDataSimulation(int aux) {

        String DataName = fileResultsName + "_data_" + fileSuffix + ".txt";
        File dataFile;
        if (aux == 0) {
            dataFile = new File(data+ DataName);
        } else {
            dataFile = new File(mlOutput+ DataName);
        }

        dataFilePath = dataFile.getAbsolutePath();
        List<String> toWriteData = new ArrayList<String>();

        try {
            // Validate if files exist
            if (!dataFile.exists()) {
                Files.createFile(Paths.get(dataFile.getPath()));
                // Creation of displacement file and its headers
                toWriteData.add("ID\tTimestep\tType\tName\tX\tY\tZ\tDate");
            }

            for (Object obj : environment.getAllObjects()) {
                if (obj instanceof iMolecule || obj instanceof Cell) {
                    double x, y, z;
                    String id, type, name;
                    if (obj instanceof iMolecule) {
                        iMolecule mol = (iMolecule) obj;
                        x = mol.getCurrentLocation().x;
                        y = mol.getCurrentLocation().y;
                        z = mol.getCurrentLocation().z;
                        id = String.valueOf(mol.getId());
                        type = mol.getType();
                        name = mol.getName();
                    } else {
                        Cell cell = (Cell) obj;
                        x = cell.getLocation().x;
                        y = cell.getLocation().y;
                        z = cell.getLocation().z;
                        id = String.valueOf(cell.getId());
                        type = cell.getType();
                        name = cell.getCellName();
                    }

                    LocalDateTime now = LocalDateTime.now(); // Get the current date and time
                    toWriteData.add(id + "\t" + schedule.getSteps() + "\t" + type + "\t" + name + "\t" + x + "\t" + y + "\t" + z + "\t" + now);
                }
            }
            try {
                Files.write(Paths.get(dataFile.getPath()), toWriteData, StandardCharsets.UTF_8, Files.exists(Paths.get(dataFile.getPath())) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Integer> readAgents(String filePath) {
        Map<String, Integer> map = new HashMap<>();
        try {
            List<String> allLines = Files.readAllLines(Paths.get(filePath));
            for (String line : allLines.subList(1, allLines.size())) {
                String[] columns = line.split(",");
                if (columns.length >= 3) {
                    String key = columns[0].trim() + ", " + columns[1].trim(); // Combinación de Sector y Target
                    Integer value = Integer.parseInt(columns[2].trim());
                    map.put(key, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static List<Sector> readSectors(String filePath) {
        Map<Integer, Sector> sectorMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine(); // Assuming this is to skip the header line
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 7) {
                    int sectorNumber = Integer.parseInt(parts[0].trim());
                    double xStart = Double.parseDouble(parts[1].trim());
                    double xEnd = Double.parseDouble(parts[2].trim());
                    double yStart = Double.parseDouble(parts[3].trim());
                    double yEnd = Double.parseDouble(parts[4].trim());
                    double zStart = Double.parseDouble(parts[5].trim());
                    double zEnd = Double.parseDouble(parts[6].trim());

                    Sector sector = new Sector(sectorNumber, xStart, xEnd, yStart, yEnd, zStart, zEnd);
                    sectorMap.put(sectorNumber, sector);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(sectorMap.values());
    }

    public void associateCellsToSectors(List<Sector> sectors) {
        for (Object obj : environment.getAllObjects()) {
            if (obj instanceof Cell) {
                Cell cell = (Cell) obj;

                double x = cell.getLocation().x;
                double y = cell.getLocation().y;
                double z = cell.getLocation().z;


                for (Sector sector : sectors) {
                    if (x >= sector.getXStart() && x <= sector.getXEnd() &&
                            y >= sector.getYStart() && y <= sector.getYEnd() &&
                            z >= sector.getZStart() && z <= sector.getZEnd()) {
                        sector.addCell(cell);
                        break;
                    }
                }
            }
        }
    }

    public void distributeMoleculesAmongCells(List<Sector> sectors, Map<String, Integer> moleculesPerSector) {
        for (int i = 0; i < sectors.size(); i++) {

            Sector sector = sectors.get(i);
            List<Cell> cells = sector.getCells();
            int numCells = cells.size();
            int sectorNumber = sector.getSectorNumber();

            for (String moleculeKey : moleculesPerSector.keySet()) {
                String[] keyParts = moleculeKey.split(", ");
                int sectorKey = Integer.parseInt(keyParts[0]);
                String moleculeName = keyParts[1];

                if (sectorKey == sectorNumber) {
                    int numMolecules = moleculesPerSector.get(moleculeKey);

                    if (numCells > 0) {
                        // Si hay células en el sector, distribuye las moléculas entre las células
                        int moleculesPerCell = numMolecules / numCells;
                        for (Cell cell : cells) {
                            for (int j = 0; j < moleculesPerCell; j++) {
                                    Iterator<iMolecule> iterator = deadAgents.iterator();
                                    while (iterator.hasNext()) {
                                        iMolecule agent = iterator.next();
                                        if (agent.getName().equals(moleculeName)) {
                                            iterator.remove();
                                            agent.setInitialPosition(cell.getLocation());
                                            mapIdCell.get(cell.getId()).addMoleculeToCell(agent);
                                            agent.putToStop(false);
                                            break;
                                        }
                                    }
                            }
                        }
                    } else {
                        // Si no hay células en el sector, añade las moléculas al siguiente sector que tenga células
                        for (int j = i + 1; j < sectors.size(); j++) {
                            Sector nextSector = sectors.get(j);
                            if (!nextSector.getCells().isEmpty()) {
                                moleculesPerSector.put(moleculeKey, moleculesPerSector.get(moleculeKey) + numMolecules);
                                break;
                            }
                        }
                    }
                }
            }

        }
    }


    private void executePythonScript () {

        String pythonExecutable = python + "\\venv\\Scripts\\python.exe";
        String pythonScriptPath = python + "\\app\\run.py";

        // Construye el comando con parámetros
        String[] command = new String[]{pythonExecutable, pythonScriptPath, "-j",json};

        // Crea el ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        List<iMolecule> agentsToRemove = new ArrayList<>();

        try {
            // Inicia el proceso
            Process process = processBuilder.start();

                for (Object obj : environment.getAllObjects()) {
                    if (obj instanceof Cell) {
                        Cell cell = (Cell) obj;
                        int cellID = cell.getId();
                        for (iMolecule agent : cell.getSetMoleculesInside()) {

                            agentsToRemove.add(agent);
                            agent.putToStop(true);
                            deadAgents.add(agent);
                        }
                        for (iMolecule agent : agentsToRemove) {
                            mapIdCell.get(cellID).removeMoleculeInCell(agent);
                        }
                    }
                }

            // Captura la salida del proceso
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            System.out.println("Here is the standard output of the command:\n");
            while ((line = stdInput.readLine()) != null) {
                System.out.println(line);
            }

            System.out.println("Here is the standard error of the command (if any):\n");
            while ((line = stdError.readLine()) != null) {
                System.out.println(line);
            }

            // Espera a que el proceso termine y captura el código de salida
            int exitCode = process.waitFor();
            System.out.println("El script de Python terminó con el código: " + exitCode);

            Map<String, Integer> recreateAgents = readAgents(mlOutput + "predictions.csv");
            List<Sector> sectors = readSectors(mlOutput + "limits.csv");
            associateCellsToSectors(sectors);
            distributeMoleculesAmongCells(sectors, recreateAgents);


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        super.finish();

        // Write displacement results
        writeFinishedResults();

        // Send email
        if (!emailTo.isEmpty() && (!canGUI || !activateGUI)) {
            String body = Functions.readFileInString(SINGulator_Model.class.getResourceAsStream("/resources/mailBody.html"));

            if (!outputPath.isEmpty() && resultsFilePath != null && displacementFilePath != null && zoneTimeStepPath != null) {
                Functions.sendEmailWithAttachment(emailTo, Constants.EMAIL_TITLE_1 + simulationName + Constants.EMAIL_TITLE_2, body, Paths.get(resultsFilePath), Paths.get(displacementFilePath), Paths.get(zoneTimeStepPath));
            } else {
                Functions.sendEmail(emailTo, Constants.EMAIL_TITLE_1 + simulationName + Constants.EMAIL_TITLE_2, body);
            }
        }

        // Reset number variables
        reactions = 0;
        escapedAgents = 0;

        System.out.println("Finishing...");
    }

    private void writeFinishedResults() {
        String displacementName = fileResultsName + "_Displacement_" + fileSuffix + ".txt";
        String zoneTimeStepName = fileResultsName + "_ZoneTimeStep_" + fileSuffix + ".txt";


        // Result file creation
        File displacementFile = new File(outputPath + displacementName);
        displacementFilePath = displacementFile.getAbsolutePath();
        List<String> toWriteDiplacement = new ArrayList<String>();

        // Result file creation
        File zoneTimeStepFile = new File(outputPath + zoneTimeStepName);
        zoneTimeStepPath = zoneTimeStepFile.getAbsolutePath();
        List<String> toWriteZoneTime = new ArrayList<String>();

        try {
            // Validate if files exist
            if (!displacementFile.exists() && !zoneTimeStepFile.exists()) {
                Files.createFile(Paths.get(displacementFile.getPath()));
                // Creation of displacement file and its headers
                toWriteDiplacement.add("ID\tType\tTimestep\tTotalDistance\tInitialPosition X\tInitialPosition Y\tInitialPosition Z\tFinalPosition X\tFinalPosition Y\tFinalPosition Z\tHasCrash\tStill alive\tX\tY\tZ\tR\tD=(R^2)/(6*t)");

                // Creation of zoneTimeStep file and its headers
                Files.createFile(Paths.get(zoneTimeStepFile.getPath()));
                toWriteZoneTime.add("ID\tType\tZone Outer membrane (ts) \tZone Outer periplasm (ts)\tZone Peptidoglycan (ts)\tZone Inner periplasm (ts)\tZone Inner membrane (ts)\tZone Cytoplasm (ts)");
            }

            Bag agents = environment.getAllObjects();
            iMolecule aux;
            double x, y, z, r;

            System.out.println("FINAL DEAD: " + deadAgents.size());
            System.out.println("FINAL LIVE: " + agents.size());

            // Go over dead or escaped agents
            for (iMolecule agent : deadAgents) {
                x = agent.getFinalPosition().x - agent.getInitialPosition().x;
                y = agent.getFinalPosition().y - agent.getInitialPosition().y;
                z = agent.getFinalPosition().z - agent.getInitialPosition().z;
                r = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));

                // Add data for displacement file
                toWriteDiplacement.add(agent.getId() + "\t" + agent.getName() + "\t" + schedule.getSteps() + "\t" + agent.getTotalDistance() + "\t" + agent.getInitialPosition().x + "\t" + agent.getInitialPosition().y + "\t" + agent.getInitialPosition().z + "\t" + agent.getFinalPosition().x + "\t" + agent.getFinalPosition().y + "\t" + agent.getFinalPosition().z + "\t" + agent.isHasCrashWihtEnvironment() + "\tfalse\t" + x + "\t" + y + "\t" + z + "\t" + r + "\t" + (Math.pow(r, 2)) / (6 * schedule.getSteps()));

                // Add data for zoneTimeStep file
                toWriteZoneTime.add(agent.getId() + "\t" + agent.getName() + "\t" + agent.getTimeStepForZone(1) + "\t" + agent.getTimeStepForZone(2) + "\t" + agent.getTimeStepForZone(3) + "\t" + agent.getTimeStepForZone(4) + "\t" + agent.getTimeStepForZone(5) + "\t" + agent.getTimeStepForZone(6));
            }

            // Go over alive agents
            for (Object agent : agents) {
                if (agent instanceof iMolecule) {
                    aux = (iMolecule) agent;

                    if (!aux.isToStop()) {
                        x = aux.getFinalPosition().x - aux.getInitialPosition().x;
                        y = aux.getFinalPosition().y - aux.getInitialPosition().y;
                        z = aux.getFinalPosition().z - aux.getInitialPosition().z;
                        r = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));

                        // Add data for displacement file
                        toWriteDiplacement.add(aux.getId() + "\t" + aux.getName() + "\t" + schedule.getSteps() + "\t" + aux.getTotalDistance() + "\t" + aux.getInitialPosition().x + "\t" + aux.getInitialPosition().y + "\t" + aux.getInitialPosition().z + "\t" + aux.getFinalPosition().x + "\t" + aux.getFinalPosition().y + "\t" + aux.getFinalPosition().z + "\t" + aux.isHasCrashWihtEnvironment() + "\ttrue\t" + x + "\t" + y + "\t" + z + "\t" + r + "\t" + (Math.pow(r, 2)) / (6 * schedule.getSteps()));

                        // Add data for zoneTimeStep file
                        toWriteZoneTime.add(aux.getId() + "\t" + aux.getName() + "\t" + aux.getTimeStepForZone(1) + "\t" + aux.getTimeStepForZone(2) + "\t" + aux.getTimeStepForZone(3) + "\t" + aux.getTimeStepForZone(4) + "\t" + aux.getTimeStepForZone(5) + "\t" + aux.getTimeStepForZone(6));
                    }
                }
            }

            try {
                // Write displacement file
                Files.write(Paths.get(displacementFile.getPath()), toWriteDiplacement, StandardCharsets.UTF_8, StandardOpenOption.APPEND);

                // Write zoneTimeStep file
                Files.write(Paths.get(zoneTimeStepFile.getPath()), toWriteZoneTime, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void calculateEnvironmentSize(JsonSingulator singulator) {
        double radius, molecularWeight;

        JsonUnity unity = singulator.getUnity();

        radius = unity.getRadius();

        JsonEnvironment environment = singulator.getEnvironment();
        this.width = environment.getWidth() / radius;
        this.height = environment.getHeight() / radius;
        this.length = environment.getLength() / radius;
    }

    public double getGuiWidth() {
        return width;
    }

    public double getGuiHeight() {
        return height;
    }

    public double getGuiLenght() {
        return length;
    }

    public int getTotalAgents() {
        if (environment != null) {
            return environment.allObjects.size();
        } else {
            return 0;
        }
    }

    public Map<Integer, Integer> getLayerNumberMap() {
        return mapLayerMolecules;
    }

    public int getTotalTries() {
        return totalTries;
    }

    public Map<String, Integer> getMapDoorNumber() {
        return mapDoorNumber;
    }

    public int getReactions() {
        return reactions;
    }

    public int getEscapedAgents() {
        return escapedAgents;
    }

    public Map<String, Integer> getAgentsNumber() {
        return mapNameNumber;
    }

    public int getCurrentMolecules() {
        int toRet = 0;

        if (mapNameNumber != null) {
            for (Integer number : mapNameNumber.values()) {
                toRet += number;
            }
        }

        return toRet;
    }

    public Map<String, Integer> getAgentsReactionNumber() {
        return mapNameReactionNumber;
    }

    public int getReactioningAgents() {
        int toRet = 0;

        if (environment != null) {
            for (Object obj : environment.allObjects) {
                if (obj instanceof iMolecule) {
                    iMolecule mol = (iMolecule) obj;
                    if (!mol.getTimeToWait()) {
                        toRet++;
                    }
                }
            }
        }

        return toRet;
    }



    public Map<Integer, Cell> getcreatedCells() {
        return mapIdCell;
    }

    private void fillZoneMaps() {
        this.layerZoneNameMap.put(Constants.EXTERIOR, Constants.EXTERIOR_NAME);
        this.layerZoneNameMap.put(Constants.OUTER_MEMBRANE, Constants.OUTER_MEMBRANE_NAME);
        this.layerZoneNameMap.put(Constants.OUTER_PERIPLASM, Constants.OUTER_PERIPLASM_NAME);
        this.layerZoneNameMap.put(Constants.PEPTIDOGLYCAN, Constants.PEPTIDOGLYCAN_NAME);
        this.layerZoneNameMap.put(Constants.INNER_PERIPLASM, Constants.INNER_PERIPLASM_NAME);
        this.layerZoneNameMap.put(Constants.INNER_MEMBRANE, Constants.INNER_MEMBRANE_NAME);
        this.layerZoneNameMap.put(Constants.CYTOPLASM, Constants.CYTOPLASM_NAME);
    }
}
