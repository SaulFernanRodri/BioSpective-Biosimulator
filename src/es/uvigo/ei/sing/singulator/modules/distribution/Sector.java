package es.uvigo.ei.sing.singulator.modules.distribution;

import es.uvigo.ei.sing.singulator.agents.Cell;

import java.util.List;

public class Sector {
    private int sectorNumber;
    private double xStart, xEnd;
    private double yStart, yEnd;
    private double zStart, zEnd;
    private List<Cell> cells;

    public Sector(int sectorNumber, double xStart, double xEnd, double yStart, double yEnd, double zStart, double zEnd) {
        this.sectorNumber = sectorNumber;
        this.xStart = xStart;
        this.xEnd = xEnd;
        this.yStart = yStart;
        this.yEnd = yEnd;
        this.zStart = zStart;
        this.zEnd = zEnd;
    }

    // Getters
    public int getSectorNumber() {
        return sectorNumber;
    }

    public double getXStart() {
        return xStart;
    }

    public double getXEnd() {
        return xEnd;
    }

    public double getYStart() {
        return yStart;
    }

    public double getYEnd() {
        return yEnd;
    }

    public double getZStart() {
        return zStart;
    }

    public double getZEnd() {
        return zEnd;
    }

    // Setters
    public void setSectorNumber(int sectorNumber) {
        this.sectorNumber = sectorNumber;
    }

    public void setXStart(double xStart) {
        this.xStart = xStart;
    }

    public void setXEnd(double xEnd) {
        this.xEnd = xEnd;
    }

    public void setYStart(double yStart) {
        this.yStart = yStart;
    }

    public void setYEnd(double yEnd) {
        this.yEnd = yEnd;
    }

    public void setZStart(double zStart) {
        this.zStart = zStart;
    }

    public void setZEnd(double zEnd) {
        this.zEnd = zEnd;
    }

    public List<Cell> getCells() {
        return cells;
    }

    public void addCell(Cell cell) {
        this.cells.add(cell);
    }

    @Override
    public String toString() {
        return "Sector " + sectorNumber + ":\n" +
                "X: " + xStart + ", " + xEnd + "\n" +
                "Y: " + yStart + ", " + yEnd + "\n" +
                "Z: " + zStart + ", " + zEnd + "\n";
    }
}