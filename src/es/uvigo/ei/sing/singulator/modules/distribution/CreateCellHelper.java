package es.uvigo.ei.sing.singulator.modules.distribution;

import es.uvigo.ei.sing.singulator.agents.Cell;
import es.uvigo.ei.sing.singulator.constants.Constants;
import es.uvigo.ei.sing.singulator.interfaces.iLayer;
import es.uvigo.ei.sing.singulator.modules.physics.PhysicsEngine;
import es.uvigo.ei.sing.singulator.simulator.SINGulator_Model;
import sim.util.Double3D;

import java.math.RoundingMode;
import java.util.*;

public class CreateCellHelper {

    private double minWidth;
    private double maxWidth;
    private double minLength;
    private double maxLength;
    private double minHeight;
    private double maxHeight;
    private Set<Cell> insertedCells;
    private Stack<Cell> toInsert;
    private int tries;
    private SINGulator_Model model;

    public CreateCellHelper(double minWidth, double maxWidth, double minHeight, double maxHeight, double minLength,
                            double maxLength, Stack<Cell> toInsert, int tries, SINGulator_Model model) {
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.toInsert = toInsert;
        this.tries = tries;
        this.model = model;

        this.insertedCells = new HashSet<Cell>();
    }

    public void calculateValidLocation() {
        String form;
        double scale, halfScale, height, radius;
        boolean canSpawn,random = false;
        Double3D location = null;
        double x = 0,y = 0,z = 0;
        double previous;
        Cell currentCell;
        double distanceBetweenCells = 0;

        //Para lograr que todas las celulas queden centradas se usa como base:
        //la celula con el mayor radio para "y" y "z" en caso de estar alineadas en el eje x
        //la celula con el mayor radio para "z" y la mas larga para "x" en caso de estar alineadas en el eje y
        //se añade +1 para evitar que el redondeo deja las celulas fuera del entorno
        try{
            if (this.model.singulator.getAlignment().getAxis().equalsIgnoreCase("rand")){
                System.out.println("Random positioning");
                random = true;
            }
            else if(this.model.singulator.getAlignment().getAxis().equals("X")){
                y = this.getBiggestRadius(toInsert.iterator()) + 1;
                z = this.getBiggestRadius(toInsert.iterator()) + 1;
                distanceBetweenCells = this.model.singulator.getAlignment().getDistance()/ this.model.singulator.getUnity().getRadius();
            }
            else if(this.model.singulator.getAlignment().getAxis().equals("Y")){
                x = this.getLongestCell(toInsert.iterator()) + 1;
                z = this.getBiggestRadius(toInsert.iterator()) + 1;
                distanceBetweenCells = this.model.singulator.getAlignment().getDistance()/ this.model.singulator.getUnity().getRadius();
            }
        }catch (NullPointerException ex){
            System.err.println("Error while searching alignment, starting with random positioning");
            random = true;
        }

        System.out.println("First Cell.");

        if (random){
            while (!toInsert.isEmpty()) {
                currentCell = toInsert.pop();

                form = currentCell.getForm();
                height = currentCell.getHeight();
                radius = currentCell.getRadius();
                scale = currentCell.getScale();
                halfScale = scale / 2;
                canSpawn = false;

                System.out.println("Hello "+currentCell.getCellName()+", ID: "+currentCell.getId()+", Form: "+form+", Height: "+height+", Radius: "+radius);

                while (tries > 0 && !canSpawn){
                    if (form.equals(Constants.CAPSULE)){
                        location = PhysicsEngine.calculateRandomPositionInEnvironmentForCapsule(model.random,minWidth,maxWidth, minHeight, maxHeight, minLength, maxLength, radius, halfScale);
                    }else if (form.equals(Constants.SPHERE)){
                        location = PhysicsEngine.calculateRandomPositionInEnvironmentForSphere(model.random, minWidth, maxWidth, minHeight, maxHeight, minLength, maxLength, radius);
                    }else if (form.equals(Constants.HEMISPHERE)){
                        location = PhysicsEngine.calculateRandomPositionInEnvironmentForHemisphere(model.random, minWidth, maxWidth, minHeight, maxHeight, minLength, maxLength, radius);
                    }
                    canSpawn = canSpawn(currentCell, location, form, radius, height, halfScale);
                    tries--;
                }

                if (canSpawn) {
                    setAgentInTheEnviroment(currentCell, location, model);
                    System.out.println("Created in x: "+location.getX()+", y: "+location.getY()+", z: "+location.getZ());
                } else {
                    System.err.println("Not enough space for cell ID: "+currentCell.getId());
                }
            }
        }
        else{
            currentCell = toInsert.pop();

            System.out.println("Hello "+currentCell.getCellName()+", ID: "+currentCell.getId()+", Form"+currentCell.getForm()+", " +
                    "Height: "+ currentCell.getHeight()+", Radius: "+currentCell.getRadius());

            if (this.model.singulator.getAlignment().getAxis().equals("X")){
                if(currentCell.getHeight()== 0){
                    location = new Double3D(Math.round(currentCell.getRadius()) + 1,y,z);
                    previous = Math.round(currentCell.getRadius() * 2) + 1;
                }
                else{
                    Double temp = (currentCell.getRadius()*2 + currentCell.getHeight())/2;
                    location = new Double3D(Math.round(temp) + 1,y,z);
                    previous=Math.round(temp*2) + 1;
                }
            }else{
                location = new Double3D(x,Math.round(currentCell.getRadius()) + 1,z);
                previous = Math.round(currentCell.getRadius() * 2) + 1;
            }

            if (canSpawn(currentCell, location, currentCell.getForm(), currentCell.getRadius(), currentCell.getHeight(), currentCell.getScale()/2)) {

                System.out.println("Created in x: "+location.getX()+", y: "+location.getY()+", z: "+location.getZ());

                setAgentInTheEnviroment(currentCell, location, model);

            } else {
                System.err.println("Not enough space for cell ID: "+currentCell.getId());
            }

            while (!toInsert.isEmpty()) {
                currentCell = toInsert.pop();

                form = currentCell.getForm();
                height = currentCell.getHeight();
                radius = currentCell.getRadius();
                scale = currentCell.getScale();
                halfScale = scale / 2;
                Double temp;

                System.out.println("Hello "+currentCell.getCellName()+", ID: "+currentCell.getId()+", Form: "+form+", Height: "+height+", Radius: "+radius);

                if (this.model.singulator.getAlignment().getAxis().equals("X")){
                    if(currentCell.getHeight()== 0){
                        temp = previous + distanceBetweenCells + radius;
                        location = new Double3D(Math.round(temp),y,z);
                        previous = Math.round(temp + radius);
                    }
                    else{
                        temp = previous + distanceBetweenCells + (radius * 2 + height)/2;
                        location = new Double3D(Math.round(temp),y,z);
                        previous= Math.round(previous + distanceBetweenCells + (radius * 2 + height));
                    }
                }else{
                    temp = previous + distanceBetweenCells + radius;
                    location = new Double3D(x,Math.round(temp),z);
                    previous = Math.round(temp + radius);
                }

                canSpawn = canSpawn(currentCell, location, form, radius, height, halfScale);

                if (canSpawn) {
                    setAgentInTheEnviroment(currentCell, location, model);
                    System.out.println("Created in x: "+location.getX()+", y: "+location.getY()+", z: "+location.getZ());
                } else {
                    System.err.println("Not enough space for cell ID: "+currentCell.getId());
                }
            }
        }

    }

    private double getBiggestRadius(Iterator<Cell> c){
        double toret = 0.0;
        Cell current;

        while(c.hasNext()){
            current = c.next();
            if (current.getRadius() > toret)
                toret = current.getRadius();
        }

        return Math.round(toret);
    }

    private double getLongestCell(Iterator<Cell> c) {
        double toret = 0.0;
        Cell current;

        while (c.hasNext()){
            current = c.next();
            if (current.getHeight() == 0 && current.getRadius() > toret)
                toret = current.getRadius();
            else if(current.getHeight() != 0 && (current.getRadius() * 2 + current.getHeight()) / 2 > toret)
                toret = (current.getRadius() * 2 + current.getHeight()) / 2;
        }

        return Math.round(toret);
    }

    private boolean canSpawn(final Cell cell, final Double3D location, String form, double radius, double height,
                             double halfScale) {
        boolean crash = false;
        boolean canSpawn = true;

        // Si está dentro del entorno
        if (PhysicsEngine.cellCanSpawnInEnvironment(location, form, radius, halfScale, minWidth, maxWidth, minHeight,
                maxHeight, minLength, maxLength)) {
            // Comprobar con el resto de células
            for (Cell anotherCell : insertedCells) {
                if (PhysicsEngine.checkCellInsideCell(form, location, radius, height, anotherCell.getForm(),
                        anotherCell.getLocation(), anotherCell.getRadius(), anotherCell.getHeight())
                        && !anotherCell.getType().equals("hemisphere")) {
                    crash = true;
                    break;
                }
            }

            // Si está dentro de alguna, la posición es incorrecta
            if (crash) {
                canSpawn = false;
                tries--;
            }
        } else {
            canSpawn = false;
        }

        return canSpawn;
    }

    private void setAgentInTheEnviroment(Cell cell, Double3D location, SINGulator_Model state) {
        for (iLayer layer : cell.getMapZoneLayers().values()) {
            model.environment.setObjectLocation(layer, location);
            layer.setExtent(model.sExtents.liesIn(location.x, location.y, location.z));
            layer.setLocation(location);
        }

        insertedCells.add(cell);
        model.mapIdCell.put(cell.getId(), cell);
    }
}
