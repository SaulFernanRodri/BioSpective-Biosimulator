package es.uvigo.ei.sing.singulator.simulator;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.NumberFormat;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

import com.google.gson.JsonElement;


import es.uvigo.ei.sing.singulator.constants.Constants;

import es.uvigo.ei.sing.singulator.json.JsonGeneralConfiguration;
import es.uvigo.ei.sing.singulator.json.JsonSingulator;
import es.uvigo.ei.sing.singulator.utils.Functions;
import sim.display.Console;
import sim.engine.SimState;

public class Launcher {

    private File inputFile;
    private boolean canGUI, change = true;

    private String fileCheckpoint1;
    private String fileCheckpoint2;

    public Launcher(File inputFile) {
        // TODO Auto-generated constructor stub
        this.inputFile = inputFile;
        this.canGUI = !GraphicsEnvironment.isHeadless();
        Constants.CAN_GUI = this.canGUI;

        // / TODO Auto-generated catch block
    }

    public void launchSimulation() {
        try {
            if (!canGUI && inputFile == null) {
                System.err.println("GUI is not supported...\n" + "Please, use next command to execute the SINGulator: "
                        + "java -jar SINGulator.jar path/to/simulation.json");
            } else {
                // TODO: JSON MALFORMED

                // Get JSON root node
                String json;
                JsonElement jsonRoot;
                JsonSingulator singulator;
                if (inputFile != null && inputFile.getName().endsWith(".json")) {
                    // Read JSON input file from disk
                    json = Functions.readFileInString(new FileInputStream(inputFile));
                } else {
                    // Retrieve properties file from disk
                    json = getJSONFile();
                }

                // Obtener información del JSON en clases Java
                singulator = Functions.fromJsonToJava(json);

                if (singulator != null) {
                    if (singulator.getGeneralConfiguration() != null) {
                        JsonGeneralConfiguration configuration = singulator.getGeneralConfiguration();
                        // User want GUI?
                        boolean activateGUI = configuration.isActivateGUI();

                        if (canGUI && activateGUI) {
                            SINGulator_GUI vid = new SINGulator_GUI(singulator, canGUI);
                            Console c = new Console(vid);
                            c.setVisible(true);
                        } else {
                            // No GUI
                            long numberOfSteps = configuration.getNumberOfSteps();
                            int saveSimulationEvery = configuration.getSaveSimEvery();
                            long totalJobs = configuration.getNumberOfJobs();
                            long currentJob = 0;
                            String readFromCheckpoint = configuration.getReadFromCheckpoint();
                            SINGulator_Model state;

                            if (!readFromCheckpoint.isEmpty()) {
                                state = (SINGulator_Model) SimState.readFromCheckpoint(new File(readFromCheckpoint));
                                currentJob = state.job();

                                // Finish at least the saved simulation
                                if (currentJob >= totalJobs) {
                                    totalJobs = currentJob + 1;
                                }
                            } else {
                                // Create the state
                                state = new SINGulator_Model(System.currentTimeMillis(), singulator, canGUI);
                                // Execute simulation
                                state.start();
                            }

                            for (; currentJob < totalJobs; currentJob++) {
                                // Set sim job
                                state.setJob(currentJob);

                                // To calculate the rate of the simulation
                                NumberFormat rateFormat = NumberFormat.getInstance();
                                rateFormat.setMaximumFractionDigits(5);
                                rateFormat.setMinimumIntegerDigits(1);
                                long oldClock = System.currentTimeMillis();
                                long firstSteps = state.schedule.getSteps();
                                long clock;
                                long steps = 0;
                                double simRate, estimatedTime;


                                do {
                                    if (!state.schedule.step(state))
                                        break;

                                    steps = state.schedule.getSteps();

                                    // Write checkpoint
                                    if (steps % saveSimulationEvery == 0) {
                                        if (change) {
                                            fileCheckpoint1 = "Checkpoint_" + state.fileSuffix + state.fileResultsName
                                                    + "_1.checkpoint";
                                            // Write checkpoint
                                            state.writeToCheckpoint(new File(state.outputPath + fileCheckpoint1));
                                        } else {
                                            fileCheckpoint2 = "Checkpoint_" + state.fileSuffix + state.fileResultsName
                                                    + "_2.checkpoint";
                                            // Write checkpoint
                                            state.writeToCheckpoint(new File(state.outputPath + fileCheckpoint2));
                                        }
                                        change = !change;

                                        steps = state.schedule.getSteps();
                                        clock = System.currentTimeMillis();
                                        simRate = (1000.0 * (steps - firstSteps)) / (clock - oldClock);
                                        estimatedTime = (numberOfSteps - steps) / simRate;

                                        // SimState.printlnSynchronized();
                                        System.err.println("Job " + currentJob + ": " + "Steps: " + steps + " Rate: "
                                                + rateFormat.format(simRate) + " Estimated time: "
                                                + Functions.format(estimatedTime, 1) + " (s), "
                                                + Functions.format(estimatedTime / 60, 1) + " (m), "
                                                + Functions.format(estimatedTime / 3600, 1) + " (h) # Agents: "
                                                + state.environment.allObjects.size() + "\n Progress: "
                                                + Functions.drawProgress(steps, numberOfSteps));


                                        firstSteps = steps;
                                        oldClock = clock;
                                    }

                                } while (steps < numberOfSteps);

                                state.finish();

                                // We’re not done yet
                                if (currentJob < totalJobs - 1) {
                                    // Notice we put it here so as not to start
                                    // when
                                    // reading from a checkpoint
                                    state.start();
                                }
                            }

                            System.exit(0);
                        }
                    } else {
                        Functions.showErrorDialog(canGUI, Constants.JSON_ERROR);
                    }
                } else {
                    Functions.showErrorDialog(canGUI, Constants.JSON_NO_SELECTED);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getJSONFile() throws IOException {
        String toRet = "";
        File selectedFile = null;

        if (!canGUI && inputFile != null) {
            selectedFile = inputFile;
        } else if (canGUI) {
            // Open JFileChooser to load the simulation.properties
            JFileChooser fileChooser = new JFileChooser(new File("./")) {
                private static final long serialVersionUID = 1L;

                @Override
                protected JDialog createDialog(Component parent) throws HeadlessException {
                    JDialog dialog = super.createDialog(parent);
                    // dialog.setIconImages(IconFactory.getIcons());
                    return dialog;
                }
            };
            int result = fileChooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
            }
        }

        if (selectedFile != null)
            toRet = Functions.readFileInString(new FileInputStream(selectedFile));

        return toRet;
    }

    public static void main(String[] args) throws IOException {
        Launcher launcher;
        if (args.length == 0)
            launcher = new Launcher(null);
        else {
            System.out.println(args[0]);
            launcher = new Launcher(new File(args[0]));
        }
        launcher.launchSimulation();
    }
}
