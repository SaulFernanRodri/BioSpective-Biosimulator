package es.uvigo.ei.sing.singulator.utils;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JOptionPane;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.xerces.impl.io.UTF8Reader;

import es.uvigo.ei.sing.singulator.agents.Cell;
import es.uvigo.ei.sing.singulator.constants.Constants;
import es.uvigo.ei.sing.singulator.interfaces.iLayer;
import es.uvigo.ei.sing.singulator.interfaces.iMolecule;
import es.uvigo.ei.sing.singulator.json.JsonSingulator;
import es.uvigo.ei.sing.singulator.modules.physics.PhysicsEngine;
import es.uvigo.ei.sing.singulator.modules.physics.Vector3D;
import es.uvigo.ei.sing.singulator.simulator.SINGulator_Model;
import sim.util.Double3D;

public class Functions {

	private static final int POW10[] = { 1, 10, 100, 1000, 10000, 100000, 1000000 };

	public static JsonObject toJSON(String json) {
		return new JsonParser().parse(json).getAsJsonObject();
	}

	public static JsonSingulator fromJsonToJava(String json) {
		return new Gson().fromJson(json, JsonSingulator.class);
	}

	public static String format(double val, int precision) {
		StringBuilder sb = new StringBuilder();
		if (val < 0) {
			sb.append('-');
			val = -val;
		}
		int exp = POW10[precision];
		long lval = (long) (val * exp + 0.5);
		sb.append(lval / exp).append('.');
		long fval = lval % exp;
		for (int p = precision - 1; p > 0 && fval < POW10[p]; p--) {
			sb.append('0');
		}
		sb.append(fval);
		return sb.toString();
	}

	public static String drawProgress(long currentStep, long totalStep) {
		String toRet = "[";

		// Calculate progress in %
		long progress = currentStep * 100 / totalStep;

		for (long i = 0; i < 100; i++) {
			if (i < progress)
				toRet += "=";
			else
				toRet += " ";
		}
		toRet += "]" + progress + "%";

		return toRet;
	}

	public static List<String> readFileInList(InputStream stream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String sCurrentLine;

		List<String> toRet = new ArrayList<String>();
		while ((sCurrentLine = reader.readLine()) != null) {
			toRet.add(sCurrentLine);

		}

		return toRet;
	}

	public static String readFileInString(InputStream stream) {
		String toRet = "";

		try {
			UTF8Reader ur = new UTF8Reader(stream);
			BufferedReader in = new BufferedReader(ur);

			String str;
			while ((str = in.readLine()) != null) {
				if (!str.isEmpty()) {
					toRet += str + "\n";
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return toRet;
	}

	public static boolean sendEmail(Set<String> to, String title, String content) {
		boolean toRet = false;

		// Get system properties
		Properties props = System.getProperties();
		// Setup mail server
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", Constants.SMTP_SERVER);
		props.put("mail.smtp.port", Constants.SMTP_PORT);

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(Constants.EMAIL_ACCOUNT, Constants.EMAIL_PASSWORD);
			}
		});

		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);
			for (String destiny : to) {
				// Set To: header field of the header.
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(destiny));
			}
			// Set Subject: header field
			message.setSubject(title);

			// Create HTML
			Multipart multipart = new MimeMultipart();
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(content, "text/html");
			// Set text message part
			multipart.addBodyPart(messageBodyPart);

			// Set HTML + Attachment to original message
			message.setContent(multipart);

			// Send message
			Transport.send(message);

			toRet = true;
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}

		return toRet;
	}

	public static boolean sendEmailWithAttachment(Set<String> to, String title, String content, Path... filePath) {
		boolean toRet = false;

		// Get system properties
		Properties props = System.getProperties();
		// Setup mail server
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", Constants.SMTP_SERVER);
		props.put("mail.smtp.port", Constants.SMTP_PORT);

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(Constants.EMAIL_ACCOUNT, Constants.EMAIL_PASSWORD);
			}
		});

		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);
			for (String destiny : to) {
				// Set To: header field of the header.
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(destiny));
			}
			// Set Subject: header field
			message.setSubject(title);

			// Create HTML
			Multipart multipart = new MimeMultipart();
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(content, "text/html");
			// Set text message part
			multipart.addBodyPart(messageBodyPart);

			// Create Attachment
			messageBodyPart = new MimeBodyPart();
			for (Path path : filePath) {
				addAttachment(multipart, path);
			}

			// Set HTML + Attachment to original message
			message.setContent(multipart);

			// Send message
			Transport.send(message);

			toRet = true;
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}

		return toRet;
	}

	private static void addAttachment(Multipart multipart, Path filename) throws MessagingException {
		DataSource source = new FileDataSource(filename.toString());
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(filename.getFileName().toString());
		multipart.addBodyPart(messageBodyPart);
	}

	public static void showErrorDialog(boolean existGui, String message) {
		if (existGui) {
			JOptionPane.showMessageDialog(null, message, Constants.JSON_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
		} else {
			System.err.println(message);
		}
	}

	public static Color getContrastColor(Color color) {
		double y = (299 * color.getRed() + 587 * color.getGreen() + 114 * color.getBlue()) / 1000;

		return y >= 128 ? Color.black : Color.white;
	}

	public static double calculateRealRadiusFromMW(double molecularWeight) {
		// MW = g/mol
		double toRet;

		// Formula Denise
		// toRet = 0.0515 * Math.pow(molecularWeight, 0.392) * 0.000000001;

		// Formula Paulo
		toRet = Math.pow(((3.0 * molecularWeight) / (4.0 * Math.PI)), (1.0 / 3.0));
		// To um
		toRet = toRet * 0.0001;

		return toRet;
	}

	public static double calculateMWFromRealRadius(double radius) {
		double toRet;

		// Formula Denise (real radius = m)
		// toRet = Math.pow((radius / 0.0000000000515), (1 / 0.392));

		// Formula Paulo (radius in formula in A) (input radius in um)
		radius = radius * 10000;
		toRet = Math.pow(radius, 3) * 4 * Math.PI / 3;

		return toRet;
	}

	@Deprecated
	public static double calculateRealRadiusFromTheoricalRadius(double theoricalRadius) {
		// theoricalRadius = micro meters
		double toRet;

		toRet = theoricalRadius * 1E-6;

		return toRet;
	}

	public static double calculateDRUsingStokesExtracellular(double theoricalRadius) {
		// Micrometers^2/s
		double toRet;

		// Calculate diffusion rate based on stokes-einsten formula
		// D = (KB*T)/(6*PI*n*r)
		toRet = (1.3806e-23 * 310) / (6 * Math.PI * 0.0007 * theoricalRadius);

		// Transform DR units to um2/s
		toRet = toRet * Math.pow(10, 12);

		return toRet;
	}

	public static double calculateDRUsingStokesIntracellular(double theoricalRadius) {
		// Micrometers^2/s
		double toRet;

		// Calculate diffusion rate based on stokes-einsten formula
		// D = (KB*T)/(6*PI*n*r)
		// 17.5 = Viscosidade macroscópica da água no citoplasma.
		toRet = (1.3806e-23 * 310) / (6 * Math.PI * 17.5 * theoricalRadius);

		// Transform DR units to um2/s
		toRet = toRet * Math.pow(10, 12);

		return toRet;
	}

	// PSEUDOMONAS DEPENDACE FOR MATRIX
	public static void findCurrentZone(iMolecule mol, Map<Integer, Cell> mapIdCell) {
		boolean inside = false;
		iLayer layer;
		Cell insideCell = mapIdCell.get(mol.getCellId());
		// double diffRate = 0.0;
		double diffRate = mol.getCurrentDiffusionRate();

		if (insideCell == null || insideCell.getForm().equals("hemisphere")) {
			// Comprobar si esta dentro de alguna celula
			for (Cell possibleCell : mapIdCell.values()) {
				// Validate if a molecule is inside the cell
				if (PhysicsEngine.checkMoleculeInsideLayer(mol.getRadius(), mol.getDesiredLocation(),
						possibleCell.getRadius(), possibleCell.getH1Center(), possibleCell.getH2Center(),
						possibleCell.getForm(), possibleCell.getLocation())) {
					insideCell = possibleCell;
					
					if(!insideCell.getForm().equals("hemisphere")){
						break;
					}
				}
			}
		}

		// Si esta dentro de alguna celula
		if (insideCell != null) {
			mol.setCellId(insideCell.getId());
			insideCell.addMoleculeToCell(mol);
			
			NavigableSet<Integer> keys = insideCell.getMapZoneLayers().descendingKeySet();
			// Iterar capas de la celula
			for (int zone : keys) {
				layer = insideCell.getLayer(zone);
				// Validate if a molecule is inside the cell
				if (PhysicsEngine.checkMoleculeInsideLayer(mol.getRadius(), mol.getDesiredLocation(), layer.getRadius(),
						layer.getH1Center(), layer.getH2Center(), layer.getForm(), layer.getLocation())
						&& zone <= mol.getMaxLayer()) {
					// Solo cambia la puerta dentro de ellas
					if (!mol.isInDoor() && !mol.isUnstoppable()) {
						mol.setCurrentZone(zone);
						diffRate = mol.getMapZoneDiffRate().get(zone);
						mol.setCurrentDiffusionRate(diffRate);
					}

					inside = true;

					break;
				}
			}

			if (!inside) {
				if (PhysicsEngine.checkMoleculeOutsideLayer(mol.getRadius(), mol.getDesiredLocation(),
						insideCell.getRadius(), insideCell.getH1Center(), insideCell.getH2Center(),
						insideCell.getForm(), insideCell.getLocation())) {
					insideCell.removeMoleculeInCell(mol);

					// Solo cambia la puerta dentro de ellas
					if (!mol.isInDoor() && !mol.isUnstoppable()) {
						mol.setCurrentZone(Constants.EXTERIOR);
						diffRate = mol.getMapZoneDiffRate().get(Constants.EXTERIOR);
						mol.setCurrentDiffusionRate(diffRate);
					}

					mol.setCellId(-1);
				}
			}
		} else {
			// Solo cambia la puerta dentro de ellas
			if (!mol.isInDoor() && !mol.isUnstoppable()) {
				mol.setCurrentZone(Constants.EXTERIOR);
				diffRate = mol.getMapZoneDiffRate().get(Constants.EXTERIOR);
				mol.setCurrentDiffusionRate(diffRate);
			}
			mol.setCellId(-1);
		}

		// Solo cambia la puerta dentro de ellas
		if (!mol.isInDoor()) {
			// Set speed with diffusion rate
			Vector3D speed = mol.getSpeed();

			speed.maxLimit(diffRate);
			speed.minLimit(diffRate);

			mol.setSpeed(speed);
		}
	}

	public static boolean insideBoard(iMolecule mol, SINGulator_Model parent) {
		boolean toRet = true;
		// Get the ball's bounds, offset by the radius of the ball
		double ballMinX = 0 + mol.getRadius();
		double ballMinY = 0 + mol.getRadius();
		double ballMinZ = 0 + mol.getRadius();
		double ballMaxX = parent.width - mol.getRadius();
		double ballMaxY = parent.height - mol.getRadius();
		double ballMaxZ = parent.length - mol.getRadius();

		Double3D desiredLocation = mol.getDesiredLocation();
		// Save positions (Double2D is immutable)
		double x = desiredLocation.x;
		double y = desiredLocation.y;
		double z = desiredLocation.z;

		Vector3D vec = mol.getSpeed();
		// Check if the ball moves over the bounds. If so, adjust the position
		// and speed.
		if (desiredLocation.x < ballMinX) {
			// Reflect along normal
			vec.setX(vec.x * -1.0);
			mol.setSpeed(vec);
			// Re-position the ball at the edge
			x = ballMinX;
			toRet = false;
		} else if (desiredLocation.x > ballMaxX) {
			vec.setX(vec.x * -1.0);
			mol.setSpeed(vec);
			x = ballMaxX;
			toRet = false;
		}

		// May cross both x and y bounds
		if (desiredLocation.y < ballMinY) {
			vec.setY(vec.y * -1.0);
			mol.setSpeed(vec);
			y = ballMinY;
			toRet = false;
		} else if (desiredLocation.y > ballMaxY) {
			vec.setY(vec.y * -1.0);
			mol.setSpeed(vec);
			y = ballMaxY;
			toRet = false;
		}

		if (desiredLocation.z < ballMinZ) {
			vec.setZ(vec.z * -1.0);
			mol.setSpeed(vec);
			z = ballMinZ;
			toRet = false;
		} else if (desiredLocation.z > ballMaxZ) {
			vec.setZ(vec.z * -1.0);
			mol.setSpeed(vec);
			z = ballMaxZ;
			toRet = false;
		}

		// Set new desired location
		mol.setDesiredLocation(new Double3D(x, y, z));

		return toRet;
	}

	public static void rebound(iMolecule mol1, Double3D desiredLocation, iMolecule mol2, Double3D anotherLocation,
			SINGulator_Model parent) {
		Double3D location1 = desiredLocation;
		Double3D location2 = anotherLocation;
		Vector3D p1 = new Vector3D(location1.x, location1.y, location1.z);
		Vector3D p2 = new Vector3D(location2.x, location2.y, location2.z);
		Vector3D n = new Vector3D();

		n = Vector3D.sub(p1, p2);

		// Find the length of the component of each of the movement
		// vectors along n.
		// a1 = v1 . n
		// a2 = v2 . n
		Vector3D v1 = mol1.getOriginalSpeed();
		Vector3D v2 = mol2.getOriginalSpeed();

		n.normalize();

		double a1 = v1.dot(n);
		double a2 = v2.dot(n);

		// TODO: Solucion cutre para los rebotes
		a1 = -Math.abs(a1);
		a2 = Math.abs(a2);

		// Using the optimized version,
		// optimizedP = 2(a1 - a2)
		double optimizedP = (2.0 * (a1 - a2)) / 2;

		// Calculate v1', the new movement vector of circle1
		// v1' = v1 - optimizedP * n
		n.mult(optimizedP);
		v1.sub(n);

		// Calculate v1', the new movement vector of circle1
		// v2' = v2 + optimizedP * n
		v2.add(n);

		if (!mol1.isHasCrash()) {
			mol1.setSpeed(v1);
		}

		// v2.minLimit(mol2.getCurrentDiffusionRate());
		// v2.maxLimit(mol2.getCurrentDiffusionRate());

		if (!mol2.isHasCrash()) {
			mol2.setSpeed(v2);
		}

		mol1.setHasCrash(true);
		mol2.setHasCrash(true);
	}

	public static double calculateMaxRadius(Map<String, String[]> mapNameInformation) {
		double toRet = -1.0, aux;

		for (String[] information : mapNameInformation.values()) {
			aux = Double.parseDouble(information[2]);
			if (toRet == -1.0 || aux > toRet) {
				// Coger radio
				toRet = aux;
			}
		}

		return toRet;
	}

	public static double calculateMinRadius(Map<String, String[]> mapNameInformation) {
		double toRet = -1.0, aux;

		for (String[] information : mapNameInformation.values()) {
			aux = Double.parseDouble(information[2]);
			if (toRet == -1.0 || aux < toRet) {
				// Coger radio
				toRet = aux;
			}
		}

		return toRet;
	}
}
