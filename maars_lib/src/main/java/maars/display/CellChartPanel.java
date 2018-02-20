//package maars.display;
//
//import maars.agents.Cell;
//import maars.cellAnalysis.SpotSetAnalyzor;
//import org.jfree.chart.ChartPanel;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.axis.NumberAxis;
//import org.jfree.chart.axis.ValueAxis;
//import org.jfree.chart.plot.CombinedDomainXYPlot;
//import org.jfree.chart.plot.XYPlot;
//import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
//import org.jfree.data.xy.XYSeries;
//import org.jfree.data.xy.XYSeriesCollection;
//
//import javax.swing.*;
//import java.awt.*;
//
///**
// * Created by tong on 25/10/16.
// */
//class CellChartPanel extends JPanel {
//   /**
//    *
//    */
//   CellChartPanel() {
//      super(new BorderLayout());
//      add(new JLabel("Waiting for data..."), BorderLayout.CENTER);
//   }
//
//   /**
//    * @param cell cell to be investigated
//    * @return chartPanel of the cell
//    */
//   static ChartPanel updateCellContent(Cell cell) {
//      CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new NumberAxis());
//      for (String geoPara : SpotSetAnalyzor.GeoParamSet) {
//         XYPlot subPlot = drawSubplot(cell, geoPara);
//         plot.add(subPlot);
//      }
//
//      final JFreeChart chart = new JFreeChart(String.valueOf(cell.getCellNumber()), plot);
//      chart.setBorderPaint(Color.black);
//      chart.setBorderVisible(true);
//      chart.setBackgroundPaint(Color.white);
//
//      plot.setBackgroundPaint(Color.lightGray);
//      plot.setDomainGridlinePaint(Color.white);
//      plot.setRangeGridlinePaint(Color.white);
//      final ValueAxis axis = plot.getDomainAxis();
//      axis.setAutoRange(true);
////      axis.setFixedAutoRange(90);
//
//      ChartPanel chartPanel = new ChartPanel(chart);
//      chartPanel.setPreferredSize(new java.awt.Dimension(500, 470));
//      chartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//      return chartPanel;
//   }
//
//   private static XYPlot drawSubplot(Cell cell, String param) {
//      final NumberAxis rangeAxis = new NumberAxis(param);
//      rangeAxis.setAutoRangeIncludesZero(true);
//      XYSeries series;
//      final XYPlot subplot = new XYPlot();
//      int i = 0;
//      for (String channel : cell.getGeometryContainer().getUsingChannels()) {
//         XYSeriesCollection seriesCollection = new XYSeriesCollection();
//         series = new XYSeries(param + "_" + channel);
//         for (Integer frame : cell.getGeometryContainer().getGeosInChannel(channel).keySet()) {
//            String currnetParamvalue = cell.getGeometryContainer().getGeosInChannel(channel).get(frame).get(param).toString();
//            if (!currnetParamvalue.isEmpty()) {
//               series.add(Double.valueOf(frame),
//                     new Double(currnetParamvalue));
//            }
//         }
//         seriesCollection.addSeries(series);
//         XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
//         renderer.setSeriesShapesVisible(0, false);
//         renderer.setSeriesPaint(0, getColor(channel));
//         subplot.setRenderer(i, renderer);
//         subplot.setDataset(i, seriesCollection);
//         i += 1;
//      }
//      subplot.setRangeAxis(rangeAxis);
//      subplot.setBackgroundPaint(Color.lightGray);
//      subplot.setDomainGridlinePaint(Color.white);
//      subplot.setRangeGridlinePaint(Color.white);
//      return subplot;
//   }
//
//
//   private static Color getColor(String channel) {
//      switch (channel) {
//         case "CFP":
//            return Color.BLUE;
//         case "GFP":
//            return Color.GREEN;
//         case "TxRed":
//            return Color.RED;
//         case "DAPI":
//            return Color.CYAN;
//         default:
//            return null;
//      }
//   }
//}
////
////   // ****************************************************************************
////   // * JFREECHART DEVELOPER GUIDE                                               *
////   // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
////   // * to purchase from Object Refinery Limited:                                *
////   // *                                                                          *
////   // * http://www.object-refinery.com/jfreechart/guide.html                     *
////   // *                                                                          *
////   // * Sales are used to provide funding for the JFreeChart project - please    *
////   // * support us so that we can continue developing free software.             *
////   // ****************************************************************************
////
//////    /**
//////     * Handles a click on the button by adding new (random) data.
//////     *
//////     * @param e  the action event.
//////     */
//////    public void actionPerformed(final ActionEvent e) {
//////
//////        for (int i = 0; i < SUBPLOT_COUNT; i++) {
//////            if (e.getActionCommand().endsWith(String.valueOf(i))) {
//////                final Millisecond now = new Millisecond();
//////                System.out.println("Now = " + now.toString());
//////                this.lastValue[i] = this.lastValue[i] * (0.90 + 0.2 * Math.random());
//////                this.datasets[i].getSeries(0).add(new Millisecond(), this.lastValue[i]);
//////            }
//////        }
//////
//////        if (e.getActionCommand().equals("ADD_ALL")) {
//////            final Millisecond now = new Millisecond();
//////            System.out.println("Now = " + now.toString());
//////            for (int i = 0; i < SUBPLOT_COUNT; i++) {
//////                this.lastValue[i] = this.lastValue[i] * (0.90 + 0.2 * Math.random());
//////                this.datasets[i].getSeries(0).add(new Millisecond(), this.lastValue[i]);
//////            }
//////        }
//////
//////    }
////}
