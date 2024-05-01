package es.uvigo.ei.sing.singulator.modules.distribution;

import es.uvigo.ei.sing.singulator.interfaces.iMolecule;
import es.uvigo.ei.sing.singulator.simulator.SINGulator_Model;
import es.uvigo.ei.sing.singulator.utils.Functions;
import sim.engine.SimState;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class MoleculeHelper implements Callable<Void>, Serializable {
    private static final long serialVersionUID = 1L;

    private int extent;
    private int rootID;
    private int availableProccesors;

    // Shared variables
    private AtomicInteger counter;
    private Stack<iMolecule> insertedAgents;
    private List<iMolecule> copy;

    private SimState state;
    private transient CyclicBarrier barrier;

    public MoleculeHelper(int extent, int availableProccesors, AtomicInteger counter, SimState state) {
        this.extent = extent;
        this.state = state;
        this.availableProccesors = availableProccesors;
        this.counter = counter;
    }

    // TODO: 22/12/2017 Dividir esto en varias clases que hagan procesos mas pequeños para evitar que el hilo root haga cosas con la cola

    @Override
    public Void call() throws Exception {
        SINGulator_Model state = (SINGulator_Model) this.state;
        iMolecule mol;

        // PASO 1: Calcular siguiente posición
        try {
            synchronized (insertedAgents) {
                while (!insertedAgents.isEmpty()) {
                    mol = insertedAgents.pop();

                    mol.calculateDesired(state);
                }
            }
        } catch (NullPointerException e) {
        }

        //Espera al calculate desired location
        try {
            this.barrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (rootID == extent) {
            for (iMolecule imol : copy)
                insertedAgents.add(imol);
        }

        try {
            this.barrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // PASO 2: Hacer rebote contra environment y capas
        try {
            synchronized (insertedAgents) {
                while (!insertedAgents.isEmpty()) {
                    mol = insertedAgents.pop();

                    mol.firstPrepare(state);

                    // if (!mol.prepare1(state)) {
                    // copy.remove(mol);
                    // }
                }
            }
        } catch (NullPointerException e) {
        }

        try {
            // System.out.println("Thread: " + Thread.currentThread()
            // + ", me pongo a esperar desired1");
            this.barrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (rootID == extent) {
            for (iMolecule imol : copy)
                insertedAgents.add(imol);
        }

        try {
            this.barrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // PASO 3: Hacer rebotes contra molecules y reacciones
        int acceptable;
        boolean isValid;
        mol = null;
        synchronized (insertedAgents) {
            while ((!insertedAgents.isEmpty() || counter.get() > 0)
                    && (insertedAgents.size() > availableProccesors * 2 || rootID == extent)) {
                try {
                    // TODO: 22/12/2017 Sincronizar pilas

                    mol = insertedAgents.peek();
                    if (mol != null && mol.tryLock()) {
                        counter.incrementAndGet();
                        insertedAgents.pop();
                        isValid = true;
                    } else {
                        isValid = false;
                    }


                    if (isValid) {
                        if (mol.isAcceptablePosition()) {
                            acceptable = mol.secondPrepare(state, rootID, extent);

                            if (acceptable == 2)
                                insertedAgents.add(mol);
                        }
                        mol.unlock();
                        counter.decrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        }

        try {
            // System.out.println("Thread: " + Thread.currentThread()
            // + ", me pongo a esperar desired2");
            this.barrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (rootID == extent) {
            if (insertedAgents.size() != 0) {
                System.out.println("insertedAgents.size(): " + insertedAgents.size());
                Set<String> emails = new HashSet<>();
                emails.add("gprodriguez2@esei.uvigo.es");
                emails.add("mpperez3@esei.uvigo.es");
                Functions.sendEmail(emails, "Petada simulador", "Asco de distribución");
                int a1 = 1 / 0;
            }
            // System.out.println("============");
            for (iMolecule imol : copy)
                insertedAgents.add(imol);

            if (!state.toCreateAgents.isEmpty()) {
                for (iMolecule toCreate : state.toCreateAgents) {
                    state.environment.setObjectLocation(toCreate, toCreate.getCurrentLocation());
                }
            }
            // System.out.println("MOVE COPY: " + insertedAgents.size());
        }

        try {
            this.barrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println("MOVE");

        // PASO 4: Mover
        try {
            synchronized (insertedAgents) {
                while (!insertedAgents.isEmpty()) {
                    mol = insertedAgents.pop();

                    if (mol != null) {
                        // if (mol.tryLock()) {
                        // if (!mol.isToStop())
                        mol.move(state);

                        // mol.unlock();
                        // } else {
                        // insertedAgents.add(mol);
                        // }
                    }
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setInsertedAgents(Stack<iMolecule> insertedAgents, List<iMolecule> copy) {
        this.insertedAgents = insertedAgents;
        this.copy = copy;
        this.counter.set(0);
    }

    public void setFirstBarrier(CyclicBarrier firstBarrier) {
        this.barrier = firstBarrier;
    }

    public int getExtent() {
        return extent;
    }

    public void setRootID(int rootID) {
        this.rootID = rootID;
    }

    public void insertMolecule(iMolecule mol) {
        insertedAgents.add(mol);
    }
}
