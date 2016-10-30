package edu.univ_tlse3.display;

import edu.univ_tlse3.cellstateanalysis.Cell;
import edu.univ_tlse3.cellstateanalysis.SpotSetAnalyzor;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

/**
 * Created by tong on 25/10/16.
 */
public class CellChartPanel extends JPanel {
   /**
    *
    * @param s the string to be written on the label
    */
   public CellChartPanel(String s) {
      super(new BorderLayout());
      add(new JLabel(s), BorderLayout.CENTER);
   }

   /**
    *
    * @param cell cell to be investigated
    * @return  chartPanel of the cell
    */
   public static ChartPanel updateCellContent(Cell cell) {
      CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new NumberAxis());
      for (String geoPara : SpotSetAnalyzor.GeoParamSet) {
         XYPlot subPlot = drawSubplot(cell, geoPara);
         subPlot.setBackgroundPaint(Color.lightGray);
         subPlot.setDomainGridlinePaint(Color.white);
         subPlot.setRangeGridlinePaint(Color.white);
         plot.add(subPlot);
      }

      final JFreeChart chart = new JFreeChart(String.valueOf(cell.getCellNumber()), plot);
      chart.setBorderPaint(Color.black);
      chart.setBorderVisible(true);
      chart.setBackgroundPaint(Color.white);

      plot.setBackgroundPaint(Color.lightGray);
      plot.setDomainGridlinePaint(Color.white);
      plot.setRangeGridlinePaint(Color.white);
      final ValueAxis axis = plot.getDomainAxis();
      axis.setAutoRange(true);
      axis.setFixedAutoRange(90);

      ChartPanel chartPanel = new ChartPanel(chart);
      chartPanel.setPreferredSize(new java.awt.Dimension(500, 470));
      chartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      return chartPanel;
   }

   public static XYPlot drawSubplot(Cell cell, String param) {
      final NumberAxis rangeAxis = new NumberAxis(param);
      rangeAxis.setAutoRangeIncludesZero(true);
      XYSeries series;
      final XYSeriesCollection seriesCollection = new XYSeriesCollection();
      for (String channel : cell.getGeometryContainer().getUsingChannels()) {
         series = new XYSeries(param + "_" + channel);
         for (Integer frame : cell.getGeometryContainer().getGeosInChannel(channel).keySet()) {
            if (cell.getGeometryContainer().getGeosInChannel(channel).get(frame).get(param).toString() != "") {
               series.add(Double.valueOf(frame),
                       new Double(cell.getGeometryContainer().getGeosInChannel(channel).get(frame).get(param).toString()));
            }
         }
         seriesCollection.addSeries(series);
      }
      final XYPlot subplot = new XYPlot(
              seriesCollection, null, rangeAxis, new StandardXYItemRenderer()
      );

      return subplot;
   }

   // ****************************************************************************
   // * JFREECHART DEVELOPER GUIDE                                               *
   // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
   // * to purchase from Object Refinery Limited:                                *
   // *                                                                          *
   // * http://www.object-refinery.com/jfreechart/guide.html                     *
   // *                                                                          *
   // * Sales are used to provide funding for the JFreeChart project - please    *
   // * support us so that we can continue developing free software.             *
   // ****************************************************************************

//    /**
//     * Handles a click on the button by adding new (random) data.
//     *
//     * @param e  the action event.
//     */
//    public void actionPerformed(final ActionEvent e) {
//
//        for (int i = 0; i < SUBPLOT_COUNT; i++) {
//            if (e.getActionCommand().endsWith(String.valueOf(i))) {
//                final Millisecond now = new Millisecond();
//                System.out.println("Now = " + now.toString());
//                this.lastValue[i] = this.lastValue[i] * (0.90 + 0.2 * Math.random());
//                this.datasets[i].getSeries(0).add(new Millisecond(), this.lastValue[i]);
//            }
//        }
//
//        if (e.getActionCommand().equals("ADD_ALL")) {
//            final Millisecond now = new Millisecond();
//            System.out.println("Now = " + now.toString());
//            for (int i = 0; i < SUBPLOT_COUNT; i++) {
//                this.lastValue[i] = this.lastValue[i] * (0.90 + 0.2 * Math.random());
//                this.datasets[i].getSeries(0).add(new Millisecond(), this.lastValue[i]);
//            }
//        }
//
//    }
}
