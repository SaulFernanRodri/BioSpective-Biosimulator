package es.uvigo.ei.sing.singulator.simulator;

import static es.uvigo.ei.sing.singulator.constants.Constants.GUI_NAME;
import static es.uvigo.ei.sing.singulator.constants.Constants.GUI_VISOR;

import java.awt.Color;

import javax.swing.JFrame;

import es.uvigo.ei.sing.singulator.agents.Door;
import es.uvigo.ei.sing.singulator.agents.DoorAgentDraw;
import es.uvigo.ei.sing.singulator.constants.Constants;
import es.uvigo.ei.sing.singulator.interfaces.iLayer;
import es.uvigo.ei.sing.singulator.json.JsonEnvironment;
import es.uvigo.ei.sing.singulator.json.JsonSingulator;
import es.uvigo.ei.sing.singulator.json.JsonUnity;
import es.uvigo.ei.sing.singulator.portrayals.CapsulePortrayal3D;
import es.uvigo.ei.sing.singulator.portrayals.HemispherePortrayal3D;
import sim.display.Controller;
import sim.display.GUIState;
import sim.display3d.Display3D;
import sim.engine.SimState;
import sim.portrayal.Inspector;
import sim.portrayal3d.SimplePortrayal3D;
import sim.portrayal3d.continuous.ContinuousPortrayal3D;
import sim.portrayal3d.simple.SpherePortrayal3D;
import sim.portrayal3d.simple.WireFrameBoxPortrayal3D;
import sim.util.Bag;

public class SINGulator_GUI extends GUIState {

	private Display3D display;
	private JFrame displayFrame;
	private ContinuousPortrayal3D vidPortrayal = new ContinuousPortrayal3D();

	private WireFrameBoxPortrayal3D wireFrameP;

	private double width, height, lenght;

	public SINGulator_GUI(JsonSingulator singulator, boolean canGUI) {
		super(new SINGulator_Model(System.currentTimeMillis(), singulator, canGUI));

		calculateEnvironmentSize(singulator);
	}

	public SINGulator_GUI(SimState state) {
		super(state);
	}

	@Override
	public void init(Controller c) {
		super.init(c);

		// Make the displayer
		display = new Display3D(800, 600, this);
		wireFrameP = new WireFrameBoxPortrayal3D(0.0, 0.0, 0.0, width, height, lenght, Color.blue);

		display.attach(wireFrameP, GUI_VISOR);
		display.attach(vidPortrayal, GUI_VISOR);

		display.scale(1 / width);
		display.setBackdrop(Color.WHITE);
		display.setShowsAxes(true);

		displayFrame = display.createFrame();
		displayFrame.setTitle(GUI_VISOR);

		c.registerFrame(displayFrame);
		displayFrame.setVisible(true);
		

	}

	@Override
	public void start() {
		super.start();
		setupPortrayals();
	}

	@Override
	public void load(SimState state) {
		super.load(state);
		setupPortrayals();
	}

	@Override
	public void finish() {
		super.finish();

	}

	@Override
	public void quit() {
		super.quit();

		if (displayFrame != null)
			displayFrame.dispose();
		displayFrame = null;
		display = null;

	}

	private void setupPortrayals() {
		SINGulator_Model cs = (SINGulator_Model) state;
		// Tell the portrayals what to portray and how to portray them
		vidPortrayal.setField(cs.environment);


		// At this point, agents are already loaded in the simulation
		Bag agents = cs.environment.getAllObjects();

		Object agent;
		for (int i = 0; i < agents.size(); i++) {
			agent = agents.get(i);

			if (agent instanceof iLayer) {
				iLayer layer = (iLayer) agent;
				if (layer.getForm().equals(Constants.CAPSULE)) {
					CapsulePortrayal3D portrayal = new CapsulePortrayal3D(layer.getRadius(), layer.getHeight(),
							SimplePortrayal3D.appearanceForColors(layer.getColor(), null, layer.getColor(), null, 1.0f,
									1.0f));

					vidPortrayal.setPortrayalForObject(layer, portrayal);
				} else if (layer.getForm().equals(Constants.SPHERE)) {
					SpherePortrayal3D portrayal = new SpherePortrayal3D(SimplePortrayal3D
							.appearanceForColors(layer.getColor(), null, layer.getColor(), null, 1.0f, 1.0f), true,
							false, (layer.getRadius() * 2), 50);

					vidPortrayal.setPortrayalForObject(layer, portrayal);
				} else if (layer.getForm().equals(Constants.HEMISPHERE)) {
					HemispherePortrayal3D portrayal = new HemispherePortrayal3D(
							layer.getRadius(), layer.getLocation(), width, height, lenght, SimplePortrayal3D
									.appearanceForColors(layer.getColor(), null, layer.getColor(), null, 1.0f, 0.2f),
							50);

					vidPortrayal.setPortrayalForObject(layer, portrayal);
				}
			} else if (agent instanceof Door) {
				Door door = (Door) agent;
				SpherePortrayal3D portrayal = new SpherePortrayal3D(
						SimplePortrayal3D.appearanceForColors(door.getColor(), null, door.getColor(), null, 1.0f, 0.2f),
						true, false, (door.getRadius() * 2));

				vidPortrayal.setPortrayalForObject(door, portrayal);
			} else if (agent instanceof DoorAgentDraw) {
				DoorAgentDraw door = (DoorAgentDraw) agent;
				SpherePortrayal3D portrayal = new SpherePortrayal3D(
						SimplePortrayal3D.appearanceForColors(door.getColor(), null, door.getColor(), null, 1.0f, 0.2f),
						true, false, (door.getRadius() * 2));

				vidPortrayal.setPortrayalForObject(door, portrayal);
			}
		}

		// Reschedule the displayer
		display.createSceneGraph();
		// Redraw the display
		display.reset();

	}

	public static String getName() {
		return GUI_NAME;
	}

	@Override
	public Object getSimulationInspectedObject() {
		return state;
	}

	@Override
	public Inspector getInspector() {
		Inspector i = super.getInspector();
		// Para que refresque el modelo cada turno (costoso)
		i.setVolatile(true);
		return i;
	}

	public static Object getInfo() {
		try {
			return SINGulator_GUI.class.getResource("/resources/index.html");
		} catch (Exception e) {
			return "Oops";
		}
	}

	private void calculateEnvironmentSize(JsonSingulator singulator) {
		double radius, molecularWeight;

		JsonUnity unity = singulator.getUnity();
		// Dar preferencia al peso molecular sobre el radio
		radius = unity.getRadius();

		molecularWeight = unity.getMolecularWeight();

		JsonEnvironment environment = singulator.getEnvironment();

		this.width = environment.getWidth() / radius;
		this.height = environment.getHeight() / radius;
		this.lenght = environment.getLength() / radius;
	}
}
