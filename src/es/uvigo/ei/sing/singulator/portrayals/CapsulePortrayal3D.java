package es.uvigo.ei.sing.singulator.portrayals;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3f;

import org.freehep.j3d.ConeSegment;
import org.freehep.j3d.SphereSegment;

import sim.portrayal.LocationWrapper;
import sim.portrayal3d.simple.PrimitivePortrayal3D;

public class CapsulePortrayal3D extends PrimitivePortrayal3D {

	private Appearance appearance;
	public static int divisions = 20;
	public float radius;
	public float heigth;

	public CapsulePortrayal3D(double radius, double heigth, Appearance appearance) {
		this.appearance = appearance;
		this.radius = (float) radius;
		this.heigth = (float) heigth;

		setPickable(true);
	}

	public CapsulePortrayal3D(double radius, double heigth, Appearance appearance, int divisions) {
		this.appearance = appearance;
		this.radius = (float) radius;
		this.heigth = (float) heigth;
		CapsulePortrayal3D.divisions = divisions;

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

			SphereSegment semi = new SphereSegment(this.radius, this.radius, 0, 180, 0, 180,
					CapsulePortrayal3D.divisions, appearance);

			SphereSegment semi2 = new SphereSegment(this.radius, this.radius, 0, 180, 0, 180,
					CapsulePortrayal3D.divisions, appearance);

			viewBranch.addChild(semi);

			ConeSegment cone = new ConeSegment(this.radius, this.radius, this.radius, this.radius, this.heigth, 0, 360,
					CapsulePortrayal3D.divisions, appearance);

			Vector3f vector = new Vector3f(0, 0, -this.heigth / 2);
			Transform3D transform3D = new Transform3D();
			transform3D.setTranslation(vector);
			TransformGroup transformGroup = new TransformGroup();
			transformGroup.setTransform(transform3D);
			transformGroup.addChild(cone);
			viewBranch.addChild(transformGroup);

			vector = new Vector3f(0, 0, -this.heigth);
			transform3D = new Transform3D();
			transform3D.rotX(Math.toRadians(180));
			transform3D.setTranslation(vector);
			transformGroup = new TransformGroup();
			transformGroup.setTransform(transform3D);
			transformGroup.addChild(semi2);
			viewBranch.addChild(transformGroup);

			vector = new Vector3f(this.heigth / 2, 0, 0);
			// vector = new Vector3f(0, 0, this.heigth / 2);
			transform3D = new Transform3D();
			transform3D.rotY(Math.toRadians(90));
			transform3D.setTranslation(vector);
			TransformGroup transformGroupParent = new TransformGroup();
			transformGroupParent.setTransform(transform3D);
			transformGroupParent.addChild(viewBranch);
			tg.addChild(transformGroupParent);

			// Como es pickable se necesita crear un locationWrapper para cada
			// shape de modo que se indica la superficie que va a ser clickable
			cone.setUserData(new LocationWrapper(obj, null, getCurrentFieldPortrayal()));
			setPickableFlags(cone);

			return tg;
		} else
			return tg;
	}

	@Override
	protected int numShapes() {
		return 3;
	}
}