package es.uvigo.ei.sing.singulator.modules.physics;

import java.util.ArrayList;
import java.util.List;

import ec.util.MersenneTwisterFast;
import es.uvigo.ei.sing.singulator.constants.Constants;
import sim.util.Double3D;

public class PhysicsEngine {

	public static Double3D calculateDoorLocation(double scale, double radius, double h1min, double h1center,
			double h2center, double cellLocY, double cellLocZ, double random, double verticalAngle,
			double horizontalAngle) {
		Double3D location;
		double x = 0.0;
		double y = 0.0;
		double z = 0.0;
		// The point is in the edges of the form
		double sum;

		// Get a random point in X axis along the capsule
		random = h1min + (random * scale);

		// If the point is in the capsule height (the cylinder form)
		if (random > h1center && random < h2center) {
			x = random;
			y = radius * Math.sin(horizontalAngle);
			z = radius * Math.cos(horizontalAngle);
		} else {
			// The point is in the first hemisphere
			if (random <= h1center) {
				x = h1center;
				sum = radius * Math.cos(verticalAngle) * Math.sin(horizontalAngle);

				// Only negative values (180º degrees) to the left
				if (sum > 0) {
					sum = -1 * sum;
				}
			}
			// The point is in the second hemisphere
			else {
				x = h2center;
				sum = radius * Math.cos(verticalAngle) * Math.sin(horizontalAngle);

				// Only positive values (180º degrees) to the right
				if (sum < 0) {
					sum = -1 * sum;
				}
			}

			x += sum;
			y = radius * Math.sin(verticalAngle) * Math.sin(horizontalAngle);
			z = radius * Math.cos(horizontalAngle);
		}

		location = new Double3D(x, cellLocY + y, cellLocZ + z);

		return location;
	}

	public static boolean checkDoorLocationsInsideCapsule(Double3D locationInterior, Double3D locationExterior,
			Double3D layerInteriorLoc, Double3D layerExteriorLoc, double h1CenterInterior, double h2CenterInterior,
			double h1CenterExterior, double h2CenterExterior) {
		boolean toRet = false;

		// Deben estar en la misma parte de la layer (hemisferio o altura)
		if (locationInterior.x <= h1CenterInterior && locationExterior.x <= h1CenterExterior) {
			toRet = true;
		} else if (locationInterior.x > h1CenterInterior && locationInterior.x < h2CenterInterior
				&& locationExterior.x > h1CenterExterior && locationExterior.x < h2CenterExterior) {
			toRet = true;
		} else if (locationInterior.x >= h2CenterInterior && locationExterior.x >= h2CenterExterior) {
			toRet = true;
		}

		return toRet;
	}

	public static Double3D calculateRandomLocationInEnvironmentPerimeter(MersenneTwisterFast random, double radius,
			double minWidth, double maxWidth, double minHeight, double maxHeight, double minLenght, double maxLenght) {
		double x = 0, y = 0, z = 0;
		int count = 3, number, aux = 0;
		String variable;

		List<String> variables = new ArrayList<String>();
		variables.add("x");
		variables.add("y");
		variables.add("z");

		while (!variables.isEmpty()) {
			// Coger eje al azar
			number = random.nextInt(count);
			variable = variables.get(number);
			// Eliminar la tratada
			variables.remove(number);

			if (variable.equals("x")) {
				if (aux == 2) {
					// Coger min o max
					x = random.nextInt(2) * maxWidth;

					if (x < radius) {
						x = radius;
					} else if (x > maxWidth - radius) {
						x = maxWidth - radius;
					}
				} else {
					x = randDouble(random, minWidth, maxWidth);

					if (x < radius) {
						x = radius;
					} else if (x > maxWidth - radius) {
						x = maxWidth - radius;
					} else {
						aux++;
					}
				}
			} else if (variable.equals("y")) {
				if (aux == 2) {
					// Coger min o max
					y = random.nextInt(2) * maxHeight;

					if (y < radius) {
						y = radius;
					} else if (y > maxHeight - radius) {
						y = maxHeight - radius;
					}
				} else {
					y = randDouble(random, minHeight, maxHeight);

					if (y < radius) {
						y = radius;
					} else if (y > maxHeight - radius) {
						y = maxHeight - radius;
					} else {
						aux++;
					}
				}
			} else {
				if (aux == 2) {
					// Coger min o max
					z = random.nextInt(2) * maxLenght;

					if (z < radius) {
						z = radius;
					} else if (z > maxLenght - radius) {
						z = maxLenght - radius;
					}
				} else {
					z = randDouble(random, minLenght, maxLenght);

					if (z < radius) {
						z = radius;
					} else if (z > maxLenght - radius) {
						z = maxLenght - radius;
					} else {
						aux++;
					}
				}
			}
			count--;
		}

		return new Double3D(x, y, z);
	}

	public static Object[] calculateRandomLocationInPerimenterLayerCapsule(MersenneTwisterFast random,
			Double3D cellLocation, double innerHeigth, double cellHorizontalAngle, double cellVerticalAngle,
			double cellRadius, double moleculeRadius) {
		return calculateRandomLocationBetweenLayersCapsule(random, cellLocation, innerHeigth, cellHorizontalAngle,
				cellVerticalAngle, cellRadius, cellRadius, moleculeRadius);
	}

	public static Object[] calculateRandomLocationInsideLayerCapsule(MersenneTwisterFast random, Double3D cellLocation,
			double innerHeigth, double cellHorizontalAngle, double cellVerticalAngle, double cellRadius,
			double moleculeRadius) {
		return calculateRandomLocationBetweenLayersCapsule(random, cellLocation, innerHeigth, cellHorizontalAngle,
				cellVerticalAngle, 0, cellRadius, moleculeRadius);
	}

	public static Object[] calculateRandomLocationBetweenLayersCapsule(MersenneTwisterFast random,
			Double3D cellLocation, double innerHeigth, double cellHorizontalAngle, double cellVerticalAngle,
			double radiusInner, double radiusOuter, double moleculeRadius) {
		double x = 0.0;
		double y = 0.0;
		double z = 0.0;
		double verticalAngle = 0;
		double horizontalAngle = 0;
		double randomBetweenRadio = 0;
		Double3D randomPosition = null;
		String position = "";

		// TODO: PROVISIONAL PARA COINCIDIR CON LA ROTACION ESTANDAR
		cellHorizontalAngle += 90;

		if (radiusInner > radiusOuter) {
			double aux = radiusOuter;
			radiusOuter = radiusInner;
			radiusInner = aux;
		}

		double radiusOuterCopy = radiusOuter - moleculeRadius;
		double radiusInnerCopy = radiusInner + moleculeRadius;

		Double3D[] capsuleLimits = getCapsulePoints(cellHorizontalAngle, cellVerticalAngle, cellLocation,
				(innerHeigth + radiusOuterCopy * 2));
		Double3D[] capsulePoints = getCapsulePoints(cellHorizontalAngle, cellVerticalAngle, cellLocation, innerHeigth);

		synchronized (random) {
			verticalAngle = Math.toRadians(random.nextInt(360));
			horizontalAngle = Math.toRadians(random.nextInt(360));
			randomBetweenRadio = randDouble(random, radiusInnerCopy, radiusOuterCopy);
			randomPosition = randomBetweenPoints(random, capsuleLimits[0], capsuleLimits[1]);
			// if (randDouble(random, 0, 1) <= 0.2f) {
			// randomPosition = randomBetweenPoints(random,capsuleLimits[1],
			// capsuleLimits[1]+cellRadius);
			// }
			// else if (randDouble(random, 0, 1) > 0.8f) {
			// {
			// randomPosition = randomBetweenPoints(random, capsuleLimits[0],
			// capsuleLimits[0]-cellRadius);
			// }else
			// {
			// randomPosition = randomBetweenPoints(random, capsuleLimits[0],
			// capsuleLimits[1]);
			// }
		}

		Double3D proximityLocation = getCapsuleProximityPoint(cellHorizontalAngle, cellVerticalAngle, cellLocation,
				innerHeigth, radiusOuter, randomPosition);

		x = randomPosition.x;
		y = randomPosition.y;
		z = randomPosition.z;

		if (proximityLocation.equals(cellLocation)) {
			// System.out.println("CENTER");
			position = "CENTER";
			cellHorizontalAngle = Math.toRadians(cellHorizontalAngle);
			cellVerticalAngle = Math.toRadians(cellVerticalAngle);

			// cuando rotas los ejes cambian
			x += randomBetweenRadio * Math.cos(verticalAngle) * Math.cos(cellHorizontalAngle) + randomBetweenRadio
					* Math.sin(verticalAngle) * Math.sin(cellVerticalAngle) * Math.sin(cellHorizontalAngle);
			y += randomBetweenRadio * Math.sin(verticalAngle) * Math.cos(cellVerticalAngle);
			z += randomBetweenRadio * Math.cos(verticalAngle) * -Math.sin(cellHorizontalAngle) + randomBetweenRadio
					* Math.sin(verticalAngle) * Math.sin(cellVerticalAngle) * Math.cos(cellHorizontalAngle);
		} else if (proximityLocation.equals(capsulePoints[0])) {
			// System.out.println("RIGHT");
			position = "RIGHT";

			x = capsulePoints[0].x;
			y = capsulePoints[0].y;
			z = capsulePoints[0].z;

			double angleA = -90 + cellVerticalAngle;
			double angleB = 90 + cellVerticalAngle;
			int randomAngle = PhysicsEngine.randInt(random, (int) angleA, (int) angleB);
			verticalAngle = Math.toRadians(randomAngle);
			horizontalAngle = Math.toRadians(PhysicsEngine.randDouble(random, 0, 360));
			// horizontalAngle = Math.toRadians(PhysicsEngine.randDouble(random,
			// -90, 90));
			cellHorizontalAngle = Math.toRadians(cellHorizontalAngle);
			cellVerticalAngle = Math.toRadians(cellVerticalAngle);
			x += randomBetweenRadio * Math.sin(horizontalAngle) * Math.cos(verticalAngle);
			y += randomBetweenRadio * Math.sin(verticalAngle);
			z += randomBetweenRadio * Math.cos(horizontalAngle) * Math.cos(verticalAngle);
		} else if (proximityLocation.equals(capsulePoints[1])) {
			// System.out.println("LEFT");
			position = "LEFT";

			x = capsulePoints[1].x;
			y = capsulePoints[1].y;
			z = capsulePoints[1].z;

			double angleA = -90 + cellVerticalAngle;
			double angleB = 90 + cellVerticalAngle;
			int randomAngle = PhysicsEngine.randInt(random, (int) angleA, (int) angleB);
			verticalAngle = Math.toRadians(randomAngle);
			horizontalAngle = Math.toRadians(PhysicsEngine.randDouble(random, 0, 360));
			// horizontalAngle = Math.toRadians(PhysicsEngine.randDouble(random,
			// 90, 270));
			cellHorizontalAngle = Math.toRadians(cellHorizontalAngle);
			cellVerticalAngle = Math.toRadians(cellVerticalAngle);
			x += randomBetweenRadio * Math.sin(horizontalAngle) * Math.cos(verticalAngle);
			y += randomBetweenRadio * Math.sin(verticalAngle);
			z += randomBetweenRadio * Math.cos(horizontalAngle) * Math.cos(verticalAngle);
		}

		return new Object[] { new Double3D(x, y, z), position };
	}

	public static Double3D calculateRandomLocationInPerimeterLayerSphere(MersenneTwisterFast random,
			Double3D cellLocation, double cellRadius, double moleculeRadius) {
		return calculateRandomLocationBetweenLayersSphere(random, cellLocation, cellRadius, cellRadius, moleculeRadius);
	}

	public static Double3D calculateRandomLocationInsideLayerSphere(MersenneTwisterFast random, Double3D cellLocation,
			double cellRadius, double moleculeRadius) {
		return calculateRandomLocationBetweenLayersSphere(random, cellLocation, 0, cellRadius, moleculeRadius);
	}

	public static Double3D calculateRandomLocationBetweenLayersSphere(MersenneTwisterFast random, Double3D cellLocation,
			double radiusInner, double radiusOuter, double moleculeRadius) {
		Double3D location = null;
		double x = 0.0;
		double y = 0.0;
		double z = 0.0;

		int verticalAngle = 0;
		int horizontalAngle = 0;
		double randomBetweenRadio = 0;

		if (radiusInner > radiusOuter) {
			double aux = radiusOuter;
			radiusOuter = radiusInner;
			radiusInner = aux;
		}

		radiusOuter -= moleculeRadius;
		radiusInner += moleculeRadius;

		synchronized (random) {
			verticalAngle = (random.nextInt(360));
			horizontalAngle = (random.nextInt(360));
			randomBetweenRadio = randDouble(random, radiusInner, radiusOuter);
		}

		x = cellLocation.x;
		y = cellLocation.y;
		z = cellLocation.z;

		x += randomBetweenRadio * Math.cos(verticalAngle) * Math.sin(horizontalAngle);
		y += randomBetweenRadio * Math.sin(verticalAngle) * Math.sin(horizontalAngle);
		z += randomBetweenRadio * Math.cos(horizontalAngle);

		location = new Double3D(x, y, z);
		return location;
	}

	public static boolean cellCanSpawnInEnvironment(Double3D location, String type, double radius, double scale,
			double environmentMinWidth, double environmentMaxWidth, double environmentMinHeight,
			double environmentMaxHeight, double environmentMinLenght, double environmentMaxLenght) {
		// spherocylinders proportion: height = 1, radius = height / 4
		boolean toRet = false;

		if (type.equals(Constants.CAPSULE)) {
			if (((location.x - scale) >= environmentMinWidth && ((location.x + scale) <= environmentMaxWidth))
					&& (((location.y - radius) >= environmentMinHeight)
							&& ((location.y + radius) <= environmentMaxHeight))
					&& (((location.z - radius) >= environmentMinLenght)
							&& ((location.z + radius) <= environmentMaxLenght))) {
				toRet = true;
			}
		} else if (type.equals(Constants.SPHERE)) {
			if ((location.x - radius) >= environmentMinWidth && (location.x + radius) <= environmentMaxWidth
					&& (location.y - radius) >= environmentMinHeight && (location.y + radius) <= environmentMaxHeight
					&& (location.z - radius) >= environmentMinLenght && (location.z + radius) <= environmentMaxLenght) {
				toRet = true;
			}
		} else {
			if (location.x == environmentMinWidth) {
				if ((location.x + radius) <= environmentMaxWidth && (location.y - radius) >= environmentMinHeight
						&& (location.y + radius) <= environmentMaxHeight
						&& (location.z - radius) >= environmentMinLenght
						&& (location.z + radius) <= environmentMaxLenght) {
					return true;
				}
			} else if (location.x == environmentMaxWidth) {
				if ((location.x - radius) <= environmentMinWidth && (location.y - radius) >= environmentMinHeight
						&& (location.y + radius) <= environmentMaxHeight
						&& (location.z - radius) >= environmentMinLenght
						&& (location.z + radius) <= environmentMaxLenght) {
					return true;
				}
			} else if (location.y == environmentMinHeight) {
				if ((location.x - radius) >= environmentMinWidth && (location.x - radius) <= environmentMinWidth
						&& (location.y + radius) <= environmentMaxHeight
						&& (location.z - radius) >= environmentMinLenght
						&& (location.z + radius) <= environmentMaxLenght) {
					return true;
				}
			} else if (location.y == environmentMaxHeight) {
				if ((location.x - radius) >= environmentMinWidth && (location.x - radius) <= environmentMinWidth
						&& (location.y - radius) >= environmentMinHeight
						&& (location.z - radius) >= environmentMinLenght
						&& (location.z + radius) <= environmentMaxLenght) {
					return true;
				}
			} else if (location.z == environmentMinLenght) {
				if ((location.x - radius) >= environmentMinWidth && (location.x - radius) <= environmentMinWidth
						&& (location.y + radius) <= environmentMaxHeight
						&& (location.y - radius) >= environmentMinHeight
						&& (location.z + radius) <= environmentMaxLenght) {
					return true;
				}
			} else if (location.z == environmentMaxLenght) {
				if ((location.x - radius) >= environmentMinWidth && (location.x - radius) <= environmentMinWidth
						&& (location.y + radius) <= environmentMaxHeight
						&& (location.y - radius) >= environmentMinHeight
						&& (location.z - radius) >= environmentMinLenght) {
					return true;
				}
			}
		}

		return toRet;
	}

	public static boolean moleculeCanSpawnInEnvironment(Double3D location, double radius, double environmentMinWidth,
			double environmentMaxWidth, double environmentMinHeight, double environmentMaxHeight,
			double environmentMinLenght, double environmentMaxLenght) {
		boolean toRet = false;

		if ((location.x - radius) >= environmentMinWidth && (location.x + radius) <= environmentMaxWidth
				&& (location.y - radius) >= environmentMinHeight && (location.y + radius) <= environmentMaxHeight
				&& (location.z - radius) >= environmentMinLenght && (location.z + radius) <= environmentMaxLenght) {
			toRet = true;
		}

		return toRet;
	}

	public static boolean checkCellInsideCell(String cellType, Double3D cellLocation, double cellRadius,
			double cellHeight, String anotherType, Double3D anotherLocation, double anotherRadius,
			double anotherHeight) {
		cellHeight = cellHeight / 2;
		anotherHeight = anotherHeight / 2;
		// If cell is a capsule
		if (cellType.equals(Constants.CAPSULE) && anotherType.equals(Constants.CAPSULE)) {
			if ((cellLocation.x - cellHeight - cellRadius) <= (anotherLocation.x + anotherRadius + anotherHeight)
					&& (cellLocation.x + cellHeight + cellRadius) >= (anotherLocation.x - anotherHeight - anotherRadius)
					&& (cellLocation.y + cellRadius) >= (anotherLocation.y - anotherRadius)
					&& (cellLocation.y - cellRadius) <= (anotherLocation.y + anotherRadius)
					&& (cellLocation.z + cellRadius) >= (anotherLocation.z - anotherRadius)
					&& (cellLocation.z - cellRadius) <= (anotherLocation.z + anotherRadius)) {
				return true;
			}
		} else if (cellType.equals(Constants.CAPSULE) && anotherType.equals(Constants.SPHERE)) {
			if ((cellLocation.x - cellHeight - cellRadius) <= (anotherLocation.x + anotherRadius)
					&& (cellLocation.x + cellHeight + cellRadius) >= (anotherLocation.x - anotherRadius)
					&& (cellLocation.y + cellRadius) >= (anotherLocation.y - anotherRadius)
					&& (cellLocation.y - cellRadius) <= (anotherLocation.y + anotherRadius)
					&& (cellLocation.z + cellRadius) >= (anotherLocation.z - anotherRadius)
					&& (cellLocation.z - cellRadius) <= (anotherLocation.z + anotherRadius)) {
				return true;
			}
		} else if (cellType.equals(Constants.SPHERE) && anotherType.equals(Constants.CAPSULE)) {
			if ((cellLocation.x - cellRadius) <= (anotherLocation.x + anotherRadius + anotherHeight)
					&& (cellLocation.x + cellRadius) >= (anotherLocation.x - anotherRadius - anotherHeight)
					&& (cellLocation.y + cellRadius) >= (anotherLocation.y - anotherRadius)
					&& (cellLocation.y - cellRadius) <= (anotherLocation.y + anotherRadius)
					&& (cellLocation.z + cellRadius) >= (anotherLocation.z - anotherRadius)
					&& (cellLocation.z - cellRadius) <= (anotherLocation.z + anotherRadius)) {
				return true;
			}
		} else if (cellType.equals(Constants.SPHERE) && anotherType.equals(Constants.SPHERE)) {
			if (checkCollisionBetweenSpheres(cellRadius, cellLocation, anotherRadius, anotherLocation)) {
				return true;
			}
		}

		return false;
	}

	public static boolean canSpawnMoleculeInsideLayer(double molRadius, double cellRadius) {
		boolean canSpawn = false;

		if (cellRadius > molRadius) {
			canSpawn = true;
		}

		return canSpawn;
	}

	public static boolean canSpawnMoleculeBetweenLayers(double molRadius, double innerLayerRadius,
			double outerLayerRadius) {
		boolean canSpawn = false;

		if ((outerLayerRadius - innerLayerRadius) > molRadius) {
			canSpawn = true;
		}

		return canSpawn;
	}

	public static boolean checkCollisionBetweenSpheres(double r1, Double3D loc, double r2, Double3D anotherLocation) {
		double distX = loc.x - anotherLocation.x;
		double distY = loc.y - anotherLocation.y;
		double distZ = loc.z - anotherLocation.z;

		double squaredist = (distX * distX) + (distY * distY) + (distZ * distZ);

		return squaredist <= (r1 + r2) * (r1 + r2);
	}

	/**
	 * @return 0 = collision whit exterior door radius of action , 1 = molecule
	 *         is inside door, 2 = molecule have a collision with the walls of
	 *         door ,3 = collsion with interior door radius of action
	 */
	public static int checkCollisionWithDoor(double moleculeRadius, boolean isInDoor, Double3D desiredLoc,
			double doorRadius, double doorMinToCenter, double doorMaxToCenter, double doorVerticalAngle,
			double doorHorizontalAngle, Double3D doorIntLocation, Double3D doorExtLocation, Double3D cellCenter) {
		// Mirar distancia entre centros si es mayor miras con el output si es
		// menor con el input
		// si esta en el medio coges la distancia entre el centro de la part y
		// de la cel y hayas el punto tangente con los angulos de la puerta
		// creas una esfera imaginaria en ese centro con radio de la puerta y
		// chequeas colision
		Vector3D center = new Vector3D(cellCenter.x, cellCenter.y, cellCenter.z);
		Double x, y, z;
		int returnFlag = -1;

		double distX = center.x - desiredLoc.x;
		double distY = center.y - desiredLoc.y;
		double distZ = center.z - desiredLoc.z;

		double distance = Math.sqrt((distX * distX) + (distY * distY) + (distZ * distZ));

		// double distance = center.magnitude() - desiredLocVector.magnitude();

		Double3D anotherLocation;

		// InterMembrane collision
		if (distance > doorMinToCenter && distance < doorMaxToCenter) {

			x = cellCenter.x + distance * Math.cos(doorVerticalAngle) * Math.sin(doorHorizontalAngle);
			y = cellCenter.y + distance * Math.sin(doorVerticalAngle) * Math.sin(doorVerticalAngle);
			z = cellCenter.z + distance * Math.cos(doorHorizontalAngle);

			anotherLocation = new Double3D(x, y, z);

			distX = desiredLoc.x - anotherLocation.x;
			distY = desiredLoc.y - anotherLocation.y;
			distZ = desiredLoc.z - anotherLocation.z;

			double squaredist = (distX * distX) + (distY * distY) + (distZ * distZ);
			double squareCenter = doorRadius * 2;

			returnFlag = 1;

			// check lateral collision in to out
			if ((squaredist >= squareCenter) && isInDoor) {
				returnFlag = 2;
			}
			// check lateral collision out to in
			squareCenter = (doorRadius + moleculeRadius) * (doorRadius + moleculeRadius);
			if ((squaredist <= squareCenter) && !isInDoor) {
				returnFlag = 2;
			}

		}
		// Interior collision
		else if (distance <= doorMinToCenter) {
			if (checkCollisionBetweenSpheres(moleculeRadius, desiredLoc, doorRadius, doorIntLocation)) {
				returnFlag = 3;
			}

		}
		// Exterior collision
		else {
			if (checkCollisionBetweenSpheres(moleculeRadius, desiredLoc, doorRadius, doorExtLocation)) {
				returnFlag = 0;
			}
		}

		return returnFlag;
	}

	public static int checkCollisionWithLayer(double moleculeRadius, Double3D moleculeCurrentLocation,
			Double3D moleculeDesiredLocation, String layerType, double layerRadius, double layerH1Center,
			double layerH2Center, Double3D layerCenter) {
		// Scale of the capsule (height + 2 * radius)
		// h + 2r = scale; h = 4r -> 4r + 2r = scale -> r = scale / 6
		double squareDistDesired, squareDistCurrent;

		// Layer is a capsule
		if (layerType.equals(Constants.CAPSULE)) {
			if (moleculeDesiredLocation.x > layerH1Center && moleculeDesiredLocation.x < layerH2Center) {
				double distX = 0;
				double distY = moleculeDesiredLocation.y - layerCenter.y;
				double distZ = moleculeDesiredLocation.z - layerCenter.z;
				squareDistDesired = (distX * distX) + (distY * distY) + (distZ * distZ);

				distX = 0;
				distY = moleculeCurrentLocation.y - layerCenter.y;
				distZ = moleculeCurrentLocation.z - layerCenter.z;
				squareDistCurrent = (distX * distX) + (distY * distY) + (distZ * distZ);
			} else if (moleculeDesiredLocation.x <= layerH1Center) {
				double distX = moleculeDesiredLocation.x - layerH1Center;
				double distY = moleculeDesiredLocation.y - layerCenter.y;
				double distZ = moleculeDesiredLocation.z - layerCenter.z;
				squareDistDesired = (distX * distX) + (distY * distY) + (distZ * distZ);

				distX = moleculeCurrentLocation.x - layerH1Center;
				distY = moleculeCurrentLocation.y - layerCenter.y;
				distZ = moleculeCurrentLocation.z - layerCenter.z;
				squareDistCurrent = (distX * distX) + (distY * distY) + (distZ * distZ);
			} else {
				double distX = moleculeDesiredLocation.x - layerH2Center;
				double distY = moleculeDesiredLocation.y - layerCenter.y;
				double distZ = moleculeDesiredLocation.z - layerCenter.z;
				squareDistDesired = (distX * distX) + (distY * distY) + (distZ * distZ);

				distX = moleculeCurrentLocation.x - layerH2Center;
				distY = moleculeCurrentLocation.y - layerCenter.y;
				distZ = moleculeCurrentLocation.z - layerCenter.z;
				squareDistCurrent = (distX * distX) + (distY * distY) + (distZ * distZ);
			}
		}
		// Layer is a sphere
		else {
			double distX = moleculeDesiredLocation.x - layerCenter.x;
			double distY = moleculeDesiredLocation.y - layerCenter.y;
			double distZ = moleculeDesiredLocation.z - layerCenter.z;
			squareDistDesired = (distX * distX) + (distY * distY) + (distZ * distZ);

			distX = moleculeCurrentLocation.x - layerCenter.x;
			distY = moleculeCurrentLocation.y - layerCenter.y;
			distZ = moleculeCurrentLocation.z - layerCenter.z;
			squareDistCurrent = (distX * distX) + (distY * distY) + (distZ * distZ);
		}

		int toRet = 0;

		double distanceLayerToLayer = -1;
		if (squareDistCurrent <= squareDistDesired) {
			// Inside to Outside

			// If molecule outside cell?
			distanceLayerToLayer = (Math.sqrt(squareDistDesired) - layerRadius) + moleculeRadius;

			if (distanceLayerToLayer >= -moleculeRadius && distanceLayerToLayer <= moleculeRadius) {
				toRet = -1;
			}
		} else {
			// Outside to Inside
			// Is molecule inside cell less than mol radius its more continue
			// if distance is between cell radius + mol radius or cell radius -
			// mol radius
			distanceLayerToLayer = Math.sqrt(squareDistDesired) - layerRadius - moleculeRadius;

			if (distanceLayerToLayer < 0) {
				toRet = 1;
			}
		}

		return toRet;
	}

	public static boolean checkMoleculeInsideLayer(double moleculeRadius, Double3D desiredLoc, double cellRadius,
			double h1center, double h2center, String cellType, Double3D layerCenter) {
		double squareDistDesired;

		// If cell is a capsule
		if (cellType.equals(Constants.CAPSULE)) {
			if (desiredLoc.x > h1center && desiredLoc.x < h2center) {
				double distX = 0;
				double distY = desiredLoc.y - layerCenter.y;
				double distZ = desiredLoc.z - layerCenter.z;
				squareDistDesired = (distX * distX) + (distY * distY) + (distZ * distZ);
			} else if (desiredLoc.x <= h1center) {
				double distX = desiredLoc.x - h1center;
				double distY = desiredLoc.y - layerCenter.y;
				double distZ = desiredLoc.z - layerCenter.z;
				squareDistDesired = (distX * distX) + (distY * distY) + (distZ * distZ);
			} else {
				double distX = desiredLoc.x - h2center;
				double distY = desiredLoc.y - layerCenter.y;
				double distZ = desiredLoc.z - layerCenter.z;
				squareDistDesired = (distX * distX) + (distY * distY) + (distZ * distZ);
			}
		}
		// If cell is a sphere
		else {
			double distX = desiredLoc.x - layerCenter.x;
			double distY = desiredLoc.y - layerCenter.y;
			double distZ = desiredLoc.z - layerCenter.z;

			squareDistDesired = (distX * distX) + (distY * distY) + (distZ * distZ);
		}

		double distance = (Math.sqrt(squareDistDesired) - cellRadius) - moleculeRadius;

		// Solo se detecta si la molecula esta completamente dentro
		// asi si choca con una molecula en el limite no puede entrar
		if (distance < -moleculeRadius * 2) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean checkMoleculeOutsideLayer(double moleculeRadius, Double3D desiredLoc, double cellRadius,
			double h1center, double h2center, String cellType, Double3D layerCenter) {
		double squareDistDesired;

		// If cell is a capsule
		if (cellType.equals(Constants.CAPSULE)) {
			if (desiredLoc.x > h1center && desiredLoc.x < h2center) {
				double distX = 0;
				double distY = desiredLoc.y - layerCenter.y;
				double distZ = desiredLoc.z - layerCenter.z;
				squareDistDesired = (distX * distX) + (distY * distY) + (distZ * distZ);
			} else if (desiredLoc.x <= h1center) {
				double distX = desiredLoc.x - h1center;
				double distY = desiredLoc.y - layerCenter.y;
				double distZ = desiredLoc.z - layerCenter.z;
				squareDistDesired = (distX * distX) + (distY * distY) + (distZ * distZ);
			} else {
				double distX = desiredLoc.x - h2center;
				double distY = desiredLoc.y - layerCenter.y;
				double distZ = desiredLoc.z - layerCenter.z;
				squareDistDesired = (distX * distX) + (distY * distY) + (distZ * distZ);
			}
		}
		// If cell is a sphere
		else {
			double distX = desiredLoc.x - layerCenter.x;
			double distY = desiredLoc.y - layerCenter.y;
			double distZ = desiredLoc.z - layerCenter.z;

			squareDistDesired = (distX * distX) + (distY * distY) + (distZ * distZ);
		}

		double distance = (Math.sqrt(squareDistDesired) - cellRadius) - moleculeRadius;

		// Solo se detecta si la molecula esta completamente dentro
		// asi si choca con una molecula en el limite no puede entrar
		if (distance > 0) {
			return true;
		} else {
			return false;
		}
	}

	public static Double3D[] getCapsulePoints(double horizontalAngle, double verticalAngle, Double3D location,
			double heigth) {
		horizontalAngle = Math.toRadians(horizontalAngle);
		verticalAngle = Math.toRadians(verticalAngle);

		double x = location.x;
		double y = location.y;
		double z = location.z;

		x += (heigth / 2) * Math.cos(verticalAngle) * Math.sin(horizontalAngle);
		y -= (heigth / 2) * Math.sin(verticalAngle);
		z += (heigth / 2) * Math.cos(horizontalAngle) * Math.cos(verticalAngle);

		Double3D firstPoint = new Double3D(x, y, z);

		x = location.x;
		y = location.y;
		z = location.z;

		x -= (heigth / 2) * Math.cos(verticalAngle) * Math.sin(horizontalAngle);
		y += (heigth / 2) * Math.sin(verticalAngle);
		z -= (heigth / 2) * Math.cos(horizontalAngle) * Math.cos(verticalAngle);

		Double3D secondPoint = new Double3D(x, y, z);

		return new Double3D[] { firstPoint, secondPoint };
	}

	public static Double3D getCapsuleProximityPoint(double horizontalAngle, double verticalAngle, Double3D cellLocation,
			double heigth, double cellRadius, Double3D location) {
		Double3D proximityLocation = cellLocation;
		Double3D[] capsulePoints = getCapsulePoints(horizontalAngle, verticalAngle, cellLocation, heigth);
		Double3D[] capsuleLimits = getCapsulePoints(horizontalAngle, verticalAngle, cellLocation,
				(heigth + (cellRadius * 2)));

		// si esta en el medio se devuelve la posicion central
		if (isBetweenPoints(capsulePoints[0], location, capsuleLimits[0])) {
			proximityLocation = capsulePoints[0];
		} else if (isBetweenPoints(capsulePoints[1], location, capsuleLimits[1])) {
			proximityLocation = capsulePoints[1];
		} else if (!isBetweenPoints(capsuleLimits[0], location, capsuleLimits[1])) {
			// esta parte se ejecuta cuando no se esta entre los limites de la
			// esfera
			double distanceA = getDistanceBetweenPoints(capsuleLimits[0], location);
			double distanceB = getDistanceBetweenPoints(capsuleLimits[1], location);
			double distanceCenter = getDistanceBetweenPoints(cellLocation, location);

			// si esta mas cerca de un limite que del centro
			if (distanceA < distanceB && distanceA < distanceCenter) {
				proximityLocation = capsulePoints[0];
			} else if (distanceB < distanceA && distanceB < distanceCenter) {
				proximityLocation = capsulePoints[1];
			}
		}

		return proximityLocation;
	}

	// public static Double3D calculateInterMembraneLocation(Layer layer1, Layer
	// layer2,
	// double random, double verticalAngle, double horizontalAngle,
	// SINGulator_Model parent) {
	// Double3D location;
	// double x = 0.0;
	// double y = 0.0;
	// double z = 0.0;
	// // The point is in the edges of the form
	// double sum;
	//
	// // Get a random point in X axis along the capsule
	// random = layer.getH1Min() + (random * layer.getScale());
	//
	// // If the point is in the capsule height (the cylinder form)
	// if (random > layer.getH1Center() && random < layer.getH2Center()) {
	// x = random;
	// y = layer.getRadius() * Math.sin(horizontalAngle);
	// z = layer.getRadius() * Math.cos(horizontalAngle);
	// } else {
	// // The point is in the first hemisphere
	// if (random <= layer.getH1Center()) {
	//
	// x = layer.getH1Center();
	// sum = layer.getRadius() * Math.cos(verticalAngle)
	// * Math.sin(horizontalAngle);
	//
	// // Only negative values (180º degrees) to the left
	// if (sum > 0) {
	// sum = -1 * sum;
	// }
	// }
	// // The point is in the second hemisphere
	// else {
	// x = layer.getH2Center();
	// sum = layer.getRadius() * Math.cos(verticalAngle)
	// * Math.sin(horizontalAngle);
	//
	// // Only positive values (180º degrees) to the right
	// if (sum < 0) {
	// sum = -1 * sum;
	// }
	// }
	//
	// x += sum;
	// y = layer.getRadius() * Math.sin(verticalAngle)
	// * Math.sin(horizontalAngle);
	// z = layer.getRadius() * Math.cos(horizontalAngle);
	// }
	//
	// location = new Double3D(x, parent.cellCenter.y + y, parent.cellCenter.z
	// + z);
	//
	// return location;
	// }

	public static double getDistanceBetweenPoints(Double3D locationA, Double3D locationB) {
		Vector3D locationAVector = new Vector3D(locationA.x, locationA.y, locationA.z);
		Vector3D locationBVector = new Vector3D(locationB.x, locationB.y, locationB.z);
		double distance = Vector3D.distance(locationAVector, locationBVector);

		return distance;
	}

	public static boolean hasToReboundWithLayer(int maxLayer, int minLayer, int zone, int toGo) {
		// Vector3D vec = mol.getSpeed();
		// double zoneDiffRate = mol.getMapZoneDiffRate().get(zone);
		boolean flag = false;

		if (toGo == 1) {
			// Outside to inside
			if (zone <= maxLayer) {
				// Calculate vector speeds
				// mol.setCurrentDiffusionRate(zoneDiffRate);
				// vec.maxLimit(zoneDiffRate);
				// vec.minLimit(zoneDiffRate);
				// mol.setCurrentZone(zone);
				// mol.setSpeed(vec);
				flag = true;
			} else {
				// System.out.println("ToGo1, else ");
				// vec.mult(-1);
				// mol.setNegativeSpeed();
				// vec.maxLimit(mol.getCurrentDiffusionRate());
				// vec.minLimit(mol.getCurrentDiffusionRate());
				flag = false;
			}
		} else {
			// Inside to outside
			if (zone > minLayer && zone <= maxLayer) {
				// double nextZoneDiffRate = mol.getMapZoneDiffRate()
				// .get(zone - 1);
				// // Calculate vector speeds
				// mol.setCurrentDiffusionRate(nextZoneDiffRate);
				//
				// vec.maxLimit(nextZoneDiffRate);
				// vec.minLimit(nextZoneDiffRate);
				//
				// mol.setCurrentZone(zone - 1);
				// mol.setSpeed(vec);
				flag = true;
			} else {
				// vec.mult(-1);
				// vec.rotate2D(90);
				// vec.maxLimit(mol.getCurrentDiffusionRate());
				// vec.minLimit(mol.getCurrentDiffusionRate());
				flag = false;
			}
		}

		return flag;
	}

	public static Double3D calculateRandomPositionInEnvironmentForSphere(MersenneTwisterFast random, double minWidth,
			double maxWidth, double minHeigth, double maxHeigth, double minLength, double maxLength, double radius) {
		double x, y, z;

		// Calculate random position in the partition and
		// validate it
		synchronized (random) {
			x = random.nextDouble() * ((maxWidth - radius) - (minWidth + radius)) + minWidth;
			y = random.nextDouble() * ((maxHeigth - radius) - (minHeigth + radius)) + minHeigth;
			z = random.nextDouble() * ((maxLength - radius) - (minLength + radius)) + minLength;
		}

		if (x < radius)
			x = radius;
		if (y < radius)
			y = radius;
		if (z < radius)
			z = radius;

		return new Double3D(x, y, z);
	}

	public static Double3D calculateRandomPositionInEnvironmentForHemisphere(MersenneTwisterFast random,
			double minWidth, double maxWidth, double minHeigth, double maxHeigth, double minLength, double maxLength,
			double radius) {
		double x = 0, y = 0, z = 0;
		// 0: xMin, 1: yMax, 2: xMax, 3: yMin, 4: zMax, 5: zMin
		int localization;

		// Calculate random position in the partition and
		// validate it
		synchronized (random) {
			// [0 to 5]
			localization = random.nextInt(6);

			switch (localization) {
			case 0:
				// xMin
				x = minWidth;
				y = random.nextDouble() * (maxHeigth - radius) + minHeigth + radius;
				z = random.nextDouble() * (maxLength - radius) + minLength + radius;

				if (y + radius > maxHeigth)
					y = maxHeigth - radius;
				if (z + radius > maxLength)
					z = maxLength - radius;

				break;
			case 1:
				// yMax
				x = random.nextDouble() * (maxWidth - radius) + minWidth + radius;
				y = maxHeigth;
				z = random.nextDouble() * (maxLength - radius) + minLength + radius;

				if (x + radius > maxWidth)
					x = maxWidth - radius;
				if (z + radius > maxLength)
					z = maxLength - radius;

				break;
			case 2:
				// xMax
				x = maxWidth;
				y = random.nextDouble() * (maxHeigth - radius) + minHeigth + radius;
				z = random.nextDouble() * (maxLength - radius) + minLength + radius;

				if (y + radius > maxHeigth)
					y = maxHeigth - radius;
				if (z + radius > maxLength)
					z = maxLength - radius;

				break;
			case 3:
				// yMin
				x = random.nextDouble() * (maxWidth - radius) + minWidth + radius;
				y = minHeigth;
				z = random.nextDouble() * (maxLength - radius) + minLength + radius;

				if (x + radius > maxWidth)
					x = maxWidth - radius;
				if (z + radius > maxLength)
					z = maxLength - radius;

				break;
			case 4:
				// zMax
				x = random.nextDouble() * (maxWidth - radius) + minWidth + radius;
				y = random.nextDouble() * (maxHeigth - radius) + minHeigth + radius;
				z = maxLength;

				if (x + radius > maxWidth)
					x = maxWidth - radius;
				if (y + radius > maxHeigth)
					y = maxHeigth - radius;

				break;
			case 5:
				// zMin
				x = random.nextDouble() * (maxWidth - radius) + minWidth + radius;
				y = random.nextDouble() * (maxHeigth - radius) + minHeigth + radius;
				z = minLength;

				if (x + radius > maxWidth)
					x = maxWidth - radius;
				if (y + radius > maxHeigth)
					y = maxHeigth - radius;

				break;
			}
		}

		return new Double3D(x, y, z);
	}

	public static Double3D calculateRandomPositionInEnvironmentForCapsule(MersenneTwisterFast random, double minWidth,
			double maxWidth, double minHeigth, double maxHeigth, double minLength, double maxLength, double radius,
			double halfScale) {
		double x, y, z;

		// Calculate random position in the partition and
		// validate it
		synchronized (random) {
			x = random.nextDouble() * ((maxWidth - halfScale)) + minWidth;
			y = random.nextDouble() * ((maxHeigth - radius)) + minHeigth;
			z = random.nextDouble() * ((maxLength - radius)) + minLength;

			// x = random.nextDouble()
			// * ((maxWidth - halfScale) - (minWidth + halfScale))
			// + minWidth;
			// y = random.nextDouble()
			// * ((maxHeigth - radius) - (minHeigth + radius)) + minHeigth;
			// z = random.nextDouble()
			// * ((maxLength - radius) - (minLength + radius)) + minLength;
		}

		if (x < halfScale)
			x = halfScale;
		if (y < radius)
			y = radius;
		if (z < radius)
			z = radius;

		return new Double3D(x, y, z);
	}

	public static boolean isBetweenPoints(Double3D locationA, Double3D pointToCheck, Double3D locationB) {
		double sum = getDistanceBetweenPoints(locationA, pointToCheck)
				+ getDistanceBetweenPoints(pointToCheck, locationB);
		double total = getDistanceBetweenPoints(locationA, locationB);

		double precision = Math.pow(10, 11);
		total = Math.round(total * precision) / precision;
		sum = Math.round(sum * precision) / precision;

		return sum == total;
	}

	public static synchronized double randDouble(MersenneTwisterFast random, double min, double max) {
		return min + (max - min) * random.nextDouble();
	}

	public static synchronized int randInt(MersenneTwisterFast random, int min, int max) {
		return random.nextInt((max - min) + 1) + min;
	}

	public synchronized static Double3D randomBetweenPoints(MersenneTwisterFast random, Double3D locationA,
			Double3D locationB) {
		// http://math.stackexchange.com/questions/83404/finding-a-point-along-a-line-in-three-dimensions-given-two-points
		Vector3D locationAVector = new Vector3D(locationA.x, locationA.y, locationA.z);
		Vector3D locationBVector = new Vector3D(locationB.x, locationB.y, locationB.z);

		Vector3D difference = new Vector3D(locationBVector.x, locationBVector.y, locationBVector.z);
		difference.sub(locationAVector);

		// obtenemos la normal de un vector
		difference.normalize();
		Vector3D normal = new Vector3D(difference.x, difference.y, difference.z);

		// obtenemos la distancia entre los dos puntos
		// y lo dividimos entre el tamaño de la normal para
		// obtener el maximo multiplicador de la normal
		double distance = Vector3D.distance(locationAVector, locationBVector);
		double normalSize = normal.magnitude();
		double max = distance / normalSize;

		double p = randDouble(random, 0, max);
		normal.mult(p);
		normal.add(locationAVector);
		return new Double3D(normal.x, normal.y, normal.z);
	}

	public static double round(double num) {
		double precision = Math.pow(10, 12);

		return Math.round(num * precision) / precision;
	}

	// public static void separateAgents(iMolecule agent1,
	// Double3D agent1Location, Double3D agent2Location, iMolecule agent2,
	// SINGulator_Model parent) {
	// agent1.putReaction(false);
	// agent2.putReaction(false);
	//
	// // Push molecules
	// double radius = agent1.getRadius() + agent2.getRadius();
	// Double3D location1 = agent1Location;
	// Double3D location2 = agent2Location;
	// Vector3D p1 = new Vector3D(location1.x, location1.y, location1.z);
	// Vector3D p2 = new Vector3D(location2.x, location2.y, location2.z);
	// Vector3D n = new Vector3D();
	// Vector3D un = new Vector3D();
	//
	// n = Vector3D.sub(p1, p2);
	// // Move to start of collision
	// double dr = (radius - n.magnitude()) / 2;
	//
	// un.setXYZ(p1);
	// un.normalize();
	// un.mult(dr);
	// p1.add(un);
	//
	// un.setXYZ(p2);
	// un.normalize();
	// un.mult(-dr);
	// p2.add(un);
	//
	// synchronized (parent.environment) {
	// parent.environment.setObjectLocation(agent1, new Double3D(p1.x,
	// p1.y, p1.z));
	// parent.environment.setObjectLocation(agent2, new Double3D(p2.x,
	// p2.y, p2.z));
	//
	// }
	// }

	public static boolean spawnMoleculeWithLayer(double agentRadius, Double3D desiredLoc, double cellRadius,
			double h1center, double h2center, String cellType, Double3D layerCenter) {
		double squareDistDesired;

		// If cell is a capsule
		if (cellType.equals(Constants.CAPSULE)) {
			if (desiredLoc.x > h1center && desiredLoc.x < h2center) {
				double distX = 0;
				double distY = desiredLoc.y - layerCenter.y;
				double distZ = desiredLoc.z - layerCenter.z;
				squareDistDesired = (distX * distX) + (distY * distY) + (distZ * distZ);
			} else if (desiredLoc.x <= h1center) {
				double distX = desiredLoc.x - h1center;
				double distY = desiredLoc.y - layerCenter.y;
				double distZ = desiredLoc.z - layerCenter.z;
				squareDistDesired = (distX * distX) + (distY * distY) + (distZ * distZ);
			} else {
				double distX = desiredLoc.x - h2center;
				double distY = desiredLoc.y - layerCenter.y;
				double distZ = desiredLoc.z - layerCenter.z;
				squareDistDesired = (distX * distX) + (distY * distY) + (distZ * distZ);
			}
		}
		// If cell is a sphere
		else {
			double distX = desiredLoc.x - layerCenter.x;
			double distY = desiredLoc.y - layerCenter.y;
			double distZ = desiredLoc.z - layerCenter.z;

			squareDistDesired = (distX * distX) + (distY * distY) + (distZ * distZ);
		}

		double distance = (Math.sqrt(squareDistDesired) - cellRadius) - agentRadius;

		// Solo se detecta si la molecula esta completamente dentro
		// asi si choca con una molecula en el limite no puede entrar
		if (distance < 0) {
			return true;
		} else {
			return false;
		}
	}
}
