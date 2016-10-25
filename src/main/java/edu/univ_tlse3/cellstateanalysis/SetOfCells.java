package edu.univ_tlse3.cellstateanalysis;

import ij.IJ;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.frame.RoiManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Main object of MAARS, you got information about each cell (ROI measurement,
 * analysis related result)
 *
 * @author Tong LI
 */
public class SetOfCells implements Iterable<Cell>, Iterator<Cell> {
    private int iteratorCount = 0;
    private ArrayList<Cell> cellArray;
    private ArrayList<Integer> cellsWithMoreThan2Spots_ = new ArrayList<Integer>();

    public SetOfCells() {
    }

    /**
     */
    public void loadCells(String pathToSegDir) {
        IJ.log("Loading Cells");
        Roi[] roiArray = getRoisAsArray(pathToSegDir + "/ROI.zip");
        cellArray = new ArrayList<Cell>();
        for (int i = 1; i <= roiArray.length; i++) {
            cellArray.add(i - 1, new Cell(roiArray[i - 1], i));
        }
        IJ.log("Done.");
    }

    /**
     * Method to open ROI file and get them as ROI array
     *
     * @return array of ROI
     */
    private Roi[] getRoisAsArray(String pathToRois) {
        RoiManager roiManager = RoiManager.getInstance();
        if (roiManager == null) {
            roiManager = new RoiManager();
        }
        if (roiManager.getCount() == 0) {
            roiManager.runCommand("Open", pathToRois);
        }
        return roiManager.getRoisAsArray();
    }

    /**
     * Method to get Cell corresponding to index
     *
     * @param index cell number
     * @return Cell corresponding to index
     */
    public Cell getCell(int index) {
        return this.cellArray.get(index - 1);
    }

    /**
     * total number of cell
     *
     * @return the size of cell array
     */
    public int size() {
        return cellArray.size();
    }

    /**
     * @param rt rt from particle analyzer
     */
    public void setRoiMeasurementIntoCells(ResultsTable rt) {
        for (Cell c : cellArray) {
            c.setRoiMeasurement(rt.getRowAsString(c.getCellNumber() - 1));
        }
    }

    public void reset() {
        this.iteratorCount = 0;
        this.cellArray = null;
    }

    public void addPotentialMitosisCell(Integer cellNb){
        if (!cellsWithMoreThan2Spots_.contains(cellNb)){
            cellsWithMoreThan2Spots_.add(cellNb);
        }
    }

    public ArrayList<Integer> getPotentialMitosisCell(){
        return cellsWithMoreThan2Spots_;
    }

    // iterator related
    @Override
    public Iterator<Cell> iterator() {
        resetCount();
        return this;
    }

    @Override
    public boolean hasNext() {
        return iteratorCount < cellArray.size();
    }

    @Override
    public Cell next() {
        if (iteratorCount >= cellArray.size())
            throw new NoSuchElementException();
        iteratorCount++;
        return cellArray.get(iteratorCount - 1);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void resetCount() {
        this.iteratorCount = 0;
    }
}
