package es.uvigo.ei.sing.singulator.portrayals;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;

import org.freehep.j3d.SphereSegment;

import sim.portrayal.LocationWrapper;
import sim.portrayal3d.simple.PrimitivePortrayal3D;
import sim.util.Double3D;

public class HemispherePortrayal3D extends PrimitivePortrayal3D {

	private Appearance appearance;
	public static int divisions = 20;
	public float radius;
	private double width;
	private double height;
	private double length;
	private Double3D location;

	public HemispherePortrayal3D(double radius, Double3D location, double width, double height, double length,
			Appearance appearance) {
		this.appearance = appearance;
		this.location = location;
		this.width = width;
		this.height = height;
		this.length = length;
		this.radius = (float) radius;

		setPickable(true);
	}

	public HemispherePortrayal3D(double radius, Double3D location, double width, double height, double length,
			Appearance appearance, int divisions) {
		this.appearance = appearance;
		this.location = location;
		this.width = width;
		this.height = height;
		this.length = length;
		this.radius = (float) radius;
		HemispherePortrayal3D.divisions = divisions;

		setPickable(true);
	}

	@Override
	public TransformGroup getModel(Object obj, TransformGroup tg) {
		if (tg == null) {
			tg = new TransformGroup();

			// Appearance appearance = new Appearance();
			// Color3f color = new Color3f(Color.yellow);
			// Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
			// Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
			// Texture texture = new Texture2D();
			// TextureAttributes texAttr = new TextureAttributes();
			// // texAttr.setTextureMode(TextureAttributes.MODULATE);
			// // texture.setBoundaryModeS(Texture.WRAP);
			// // texture.setBoundaryModeT(Texture.WRAP);
			// texture.setBoundaryColor(new Color4f(0.0f, 1.0f, 0.0f, 0.0f));
			// Material mat = new Material(color, black, color, white, 70f);
			// appearance.setTextureAttributes(texAttr);
			// appearance.setMaterial(mat);
			// appearance.setTexture(texture);

			BranchGroup viewBranch = new BranchGroup();

			Transform3D transform3D = new Transform3D();
			SphereSegment semi = null;
			if (location.x == 0.0) {
				semi = new SphereSegment(this.radius, this.radius, 0, 180, 0, 180, HemispherePortrayal3D.divisions,
						appearance);
				transform3D.rotY(Math.toRadians(90));
			} else if (location.x == width) {
				semi = new SphereSegment(this.radius, this.radius, 0, -180, 0, -180, HemispherePortrayal3D.divisions,
						appearance);
				transform3D.rotY(Math.toRadians(90));
			} else if (location.y == 0.0) {
				semi = new SphereSegment(this.radius, this.radius, 0, -180, 0, -180, HemispherePortrayal3D.divisions,
						appearance);
				transform3D.rotX(Math.toRadians(90));
			} else if (location.y == height) {
				semi = new SphereSegment(this.radius, this.radius, 0, -180, 0, -180, HemispherePortrayal3D.divisions,
						appearance);
				transform3D.rotX(Math.toRadians(-90));
			} else if (location.z == 0.0) {
				semi = new SphereSegment(this.radius, this.radius, 0, -180, 0, -180, HemispherePortrayal3D.divisions,
						appearance);
				transform3D.rotX(Math.toRadians(180));
			} else if (location.z == length) {
				semi = new SphereSegment(this.radius, this.radius, 0, -180, 0, -180, HemispherePortrayal3D.divisions,
						appearance);
			}
			viewBranch.addChild(semi);

			TransformGroup transformGroupParent = new TransformGroup();
			transformGroupParent.setTransform(transform3D);
			transformGroupParent.addChild(viewBranch);
			tg.addChild(transformGroupParent);

			// Como es pickable se necesita crear un locationWrapper
			semi.setUserData(new LocationWrapper(obj, null, getCurrentFieldPortrayal()));
			setPickableFlags(semi);

			return tg;
		} else
			return tg;
	}

	@Override
	protected int numShapes() {
		return 1;
	}
}