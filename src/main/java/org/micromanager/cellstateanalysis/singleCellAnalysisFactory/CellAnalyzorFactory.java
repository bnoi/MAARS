package org.micromanager.cellstateanalysis.singleCellAnalysisFactory;

import org.micromanager.cellstateanalysis.Cell;

import fiji.plugin.trackmate.Spot;

public interface CellAnalyzorFactory {
	public CellAnalyzor getAnalyzer(Cell cell, Iterable<Spot> spotSet);
}
