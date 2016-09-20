package edu.univ_tlse3.cellstateanalysis.singleCellAnalysisFactory;

import edu.univ_tlse3.cellstateanalysis.Cell;
import fiji.plugin.trackmate.Spot;

public interface CellAnalyzorFactory {
	public CellAnalyzor getAnalyzer(Cell cell, Iterable<Spot> spotSet);
}
