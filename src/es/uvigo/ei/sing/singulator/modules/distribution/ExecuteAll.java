package es.uvigo.ei.sing.singulator.modules.distribution;

import es.uvigo.ei.sing.singulator.agents.Cell;
import es.uvigo.ei.sing.singulator.agents.Door;
import es.uvigo.ei.sing.singulator.agents.Feeder;
import es.uvigo.ei.sing.singulator.interfaces.iMolecule;
import es.uvigo.ei.sing.singulator.simulator.SINGulator_Model;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.Double3D;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecuteAll implements Steppable, Serializable {

    private static final long serialVersionUID = 1L;

    public SINGulator_Model parent;
    private int processors;
    private transient ExecutorService ex;
    private transient CyclicBarrier barrier;
    private Map<Integer, MoleculeHelper> extentsMolecule;
    private Map<Integer, DoorHelper> extentsDoor;
    private boolean hasVariables = false;
    private int rootID = -1;
    private Collection<Callable<Void>> tasks;

    public ExecuteAll(SINGulator_Model parent) {
        this.parent = parent;
        this.processors = parent.availableProcessors;

        this.extentsDoor = new HashMap<Integer, DoorHelper>(processors);
        this.extentsMolecule = new HashMap<Integer, MoleculeHelper>(processors);

        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < processors; i++) {
            if (parent.hasTransporters)
                this.extentsDoor.put(i, new DoorHelper(i, parent));
            this.extentsMolecule.put(i, new MoleculeHelper(i, processors, counter, parent));
        }

        this.tasks = new ArrayList<Callable<Void>>(processors);
    }

    @Override
    public void step(SimState state) {
        // Create variables
        SINGulator_Model cs = (SINGulator_Model) state;
        this.barrier = new CyclicBarrier(this.processors);
        if (this.ex == null) {
            this.ex = Executors.newFixedThreadPool(processors);
        }

        // Clear sorted map of X positions
        cs.mapLayerMolecules.clear();
        cs.mapDoorNumber.clear();
        cs.mapNameNumber.clear();
        cs.mapNameReactionNumber.clear();
        cs.toCreateAgents.clear();
        this.tasks.clear();

        // Add door and molecules in sub cubes
        Bag totalAgents = new Bag(cs.environment.getAllObjects());
        iMolecule mol;
        Door door;
        Double3D location;
        int zone;
        DoorHelper doorHelper;
        MoleculeHelper moleculeHelper;
        Object obj;
        Stack<iMolecule> sharedMolecules;
        List<iMolecule> sharedMoleculesCopy;

        
        sharedMolecules = new Stack<>();
        sharedMoleculesCopy = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < totalAgents.size(); i++) {
            obj = totalAgents.get(i);

            if (obj instanceof iMolecule) {
                mol = (iMolecule) obj;
                if (!mol.isToStop()) {
                    // Actualizar variables del modelo
                    if (mol.isInDoor() && !mol.getDoorName().isEmpty()) {
                        cs.mapDoorNumber.put(mol.getDoorName(), cs.mapDoorNumber.get(mol.getDoorName()) + 1);
                    }
                    cs.mapLayerMolecules.put(mol.getCurrentZone(), cs.mapLayerMolecules.get(mol.getCurrentZone()) + 1);

                    if (mol.isReaction()) {
                        cs.mapNameReactionNumber.put(mol.getName(), cs.mapNameReactionNumber.get(mol.getName()) + 1);
                    } else {
                        cs.mapNameNumber.put(mol.getName(), cs.mapNameNumber.get(mol.getName()) + 1);
                    }

                    // Añadir agentes compartidos
                    sharedMolecules.add(mol);
                    sharedMoleculesCopy.add(mol);
                } else if (mol.isToStop()) {
                    try {
                        // Eliminar molecula del feeder si ya la trató
                        // if (parent.hasFeeder)
                        // cs.simulatorLogic.parent.feeder.removeFromTreated(mol);
                        cs.environment.remove(mol);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if (obj instanceof Door && !hasVariables && parent.hasTransporters) {
                door = (Door) totalAgents.get(i);

                location = cs.environment.getObjectLocation(door);

                zone = parent.sExtents.liesIn(location.x, location.y, location.z);

                // Introducir puerta en su cuadrante correspondiente
                extentsDoor.get(zone).insertDoor(door);
            }
        }

        // Ejecutar si hay al menos una puerta
        if (parent.hasTransporters) {
            // Execute DoorHelpers
            for (int i = 0; i < extentsDoor.size(); i++) {
                doorHelper = extentsDoor.get(i);

                tasks.add(doorHelper);
            }

            // Step 1: Execute doors
            try {
                ex.invokeAll(tasks);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            tasks.clear();
        }

        // Execute MoleculeHelpers
        for (int i = 0; i < extentsMolecule.size(); i++) {
            moleculeHelper = extentsMolecule.get(i);
            moleculeHelper.setFirstBarrier(barrier);
            // Configuración inicial
            if (!hasVariables) {
                if (rootID == -1) {
                    rootID = moleculeHelper.getExtent();
                }
                moleculeHelper.setRootID(rootID);
            }
            moleculeHelper.setInsertedAgents(sharedMolecules, sharedMoleculesCopy);

            tasks.add(moleculeHelper);
        }

        // Step 2: Execute molecules
        try {
            List<Future<Void>> futures = ex.invokeAll(tasks);
            for (Future<Void> fut : futures) {
                fut.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Step 3: Execute feeder
        if (parent.hasFeeder) {
            for (Feeder feeder : parent.feederList) {
                feeder.step(cs);
            }
        }

        // Step 4: Execute cells
        for (Cell cell : parent.mapIdCell.values()) {
            cell.step(parent);
        }

        hasVariables = true;
    }
}
