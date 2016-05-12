package org.univ_tlse3.cellstateanalysis.singleCellAnalysisFactory;

import org.univ_tlse3.cellstateanalysis.Cell;

import fiji.plugin.trackmate.Spot;

public interface CellAnalyzorFactory {
	public CellAnalyzor getAnalyzer(Cell cell, Iterable<Spot> spotSet);
}
