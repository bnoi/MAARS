package maars.display;

import maars.agents.Cell;
import maars.agents.DefaultSetOfCells;
import maars.cellAnalysis.SpotSetAnalyzor;
import maars.io.IOUtils;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

/**
 * Created by tong on 25/10/16.
 */
public class SOCVisualizer extends JFrame implements MouseListener, KeyListener{
   private DefaultListModel<Integer> alreadyShownList_ = new DefaultListModel<>();
   private JTextField pathToSoc_;
   private JList<Integer> cellToDisplayList_;

   private DefaultSetOfCells currentSoc = null;

   /** The number of subplots. */
   public static final int SUBPLOT_COUNT = SpotSetAnalyzor.GeoParamSet.length;

   /** The datasets. */
   private XYSeriesCollection[] datasets;

   /** The most recent value added to series 1. */
   private double[] lastValue = new double[SUBPLOT_COUNT];

   private String[] channels;

   private final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new NumberAxis());

   public SOCVisualizer(String[] channels) {
      super("Display cells with at least 1 spot detected");
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

      this.channels = channels;
      prepareDataSet(channels);

      preparePlots();

      final JFreeChart chart = new JFreeChart("Plots of parameters", plot);
      chart.setBorderPaint(Color.black);
      chart.setBorderVisible(true);
      chart.setBackgroundPaint(Color.white);

      plot.setBackgroundPaint(Color.lightGray);
      plot.setDomainGridlinePaint(Color.white);
      plot.setRangeGridlinePaint(Color.white);
      final ValueAxis axis = plot.getDomainAxis();
      axis.setAutoRange(true);

      final JPanel content = new JPanel(new BorderLayout());

      setContentPane(content);

      final ChartPanel chartPanel = new ChartPanel(chart);
      chartPanel.setBorder(BorderFactory.createTitledBorder("Parameters"));
      chartPanel.setToolTipText("parameters of selected cell");

      chartPanel.setPreferredSize(new java.awt.Dimension(450, 350));

      RefineryUtilities.centerFrameOnScreen(this);
      cellToDisplayList_ = new JList<>(alreadyShownList_);
      cellToDisplayList_.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      cellToDisplayList_.setLayoutOrientation(JList.VERTICAL);

      JScrollPane scrollPane = new JScrollPane(cellToDisplayList_);
      scrollPane.setBorder(BorderFactory.createTitledBorder("Cell Numbers"));
      scrollPane.setToolTipText("number of cells with > 1 spot");

      JPanel loadSocPanel = new JPanel(new BorderLayout());
      loadSocPanel.setBorder(BorderFactory.createTitledBorder("Serialize Soc"));
      loadSocPanel.setToolTipText("Load existing soc object");
      JButton loadSocBut = new JButton("Load");
      JPanel pathToSocTFPanel = new JPanel();
      pathToSoc_ = new JFormattedTextField(String.class);
      pathToSoc_.setColumns(10);
      pathToSoc_.setText("");
      pathToSocTFPanel.add(pathToSoc_);
      loadSocBut.addActionListener(actionEvent -> {
         try {
            FileInputStream f_in;
            if (pathToSoc_.getText().endsWith("SetOfCell.serialize")) {
               f_in = new FileInputStream(pathToSoc_.getText());
            } else {
               f_in = new FileInputStream(pathToSoc_.getText() + File.separator + "SetOfCell.serialize");
            }

            ObjectInputStream obj_in =
                  new ObjectInputStream(f_in);
            Object obj = obj_in.readObject();
            if (obj instanceof DefaultSetOfCells) {
               // Cast object to a soc
               DefaultSetOfCells soc = (DefaultSetOfCells) obj;
               updateCellList(soc);
            }
         } catch (IOException | ClassNotFoundException e) {
            IOUtils.printErrorToIJLog(e);
         }
      });
      loadSocPanel.add(new JLabel("Path to soc object"), BorderLayout.NORTH);
      loadSocPanel.add(pathToSocTFPanel, BorderLayout.CENTER);
      loadSocPanel.add(loadSocBut, BorderLayout.SOUTH);

      JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            scrollPane, chartPanel);
      splitPane.setOneTouchExpandable(true);
      splitPane.setDividerLocation(120);

      content.setPreferredSize(new java.awt.Dimension(900, 650));
      content.add(splitPane, BorderLayout.CENTER);
      content.add(loadSocPanel, BorderLayout.EAST);

      cellToDisplayList_.addMouseListener(this);

      cellToDisplayList_.addKeyListener(this);

      cellToDisplayList_.setCellRenderer(new DefaultListCellRenderer() {

         @Override
         public Component getListCellRendererComponent(JList list,
                                                       Object value, int index, boolean isSelected,
                                                       boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index,
                  isSelected, cellHasFocus);

            Integer num = (Integer) value;
            if (num > 0){
               if (currentSoc.getCell(num).hasSpotsInBetween()) {
                  setBackground(Color.lightGray);
               }
               if (currentSoc.getCell(num).hasUnalignedDots()) {
                  setBackground(Color.magenta);
               }
               if (currentSoc.getCell(num).hasSpotsInBetween() & currentSoc.getCell(num).hasUnalignedDots()) {
                  setBackground(Color.RED);
               }
            }

            return this;
         }
      });

      this.pack();
   }

   private void preparePlots() {
      for (int f = 0; f < SUBPLOT_COUNT; f++) {
         final NumberAxis rangeAxis = new NumberAxis(SpotSetAnalyzor.GeoParamSet[f]);
         rangeAxis.setAutoRangeIncludesZero(false);
         final XYPlot subplot = new XYPlot(
               this.datasets[f], null, rangeAxis, new StandardXYItemRenderer()
         );
         XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
         for (int c =0; c < channels.length; c++) {
            renderer.setSeriesShapesVisible(c, false);
            renderer.setSeriesPaint(c, getColor(channels[c]));
            subplot.setRenderer(c, renderer);
         }
         subplot.setBackgroundPaint(Color.lightGray);
         subplot.setDomainGridlinePaint(Color.white);
         subplot.setRangeGridlinePaint(Color.white);
         plot.add(subplot);
      }
   }

   private void prepareDataSet(String[] channels) {
      this.datasets = new XYSeriesCollection[SUBPLOT_COUNT];
      XYSeries series;
      XYSeriesCollection collectionForChannels;
      for (int f = 0; f < SUBPLOT_COUNT; f++) {
         collectionForChannels = new XYSeriesCollection();
         for (String channel : channels) {
            series = new XYSeries(SpotSetAnalyzor.GeoParamSet[f] + "_" + channel);
            collectionForChannels.addSeries(series);
         }
         this.datasets[f] = collectionForChannels;
      }
   }

   public void updateCellList(DefaultSetOfCells defaultSetOfCells) {
      this.currentSoc = defaultSetOfCells;
      assert currentSoc.size() > 0;
      for (Integer cellIndex : currentSoc.getPotentialMitosisCell()) {
         int cellNb = currentSoc.getCell(cellIndex).getCellNumber();
         if (!alreadyShownList_.contains(cellNb)){
            alreadyShownList_.addElement(cellNb);
         }
      }
      cellToDisplayList_.validate();
   }

   private void updatePlot(Cell cell){
      clearPlot();
      for (int f = 0; f < SUBPLOT_COUNT; f++) {
         for (int c = 0; c < channels.length; c++){
            HashMap<Integer, HashMap<String, Double>> currentGeos = cell.getGeometryContainer().getGeosInChannel(this.channels[c]);
            for (int t :currentGeos.keySet()){
               if ( currentGeos.get(t).keySet().contains(SpotSetAnalyzor.GeoParamSet[f])){
                  this.datasets[f].getSeries(c).addOrUpdate(Integer.valueOf(t),
                        currentGeos.get(t).get(SpotSetAnalyzor.GeoParamSet[f]));
               }
            }
         }
      }
   }

   private static Color getColor(String channel) {
      switch (channel) {
         case "CFP":
            return Color.BLUE;
         case "GFP":
            return Color.GREEN;
         case "TxRed":
            return Color.RED;
         case "DAPI":
            return Color.CYAN;
         default:
            return null;
      }
   }

   private void clearPlot(){
      for (int f = 0; f < SUBPLOT_COUNT; f++) {
         for (int c = 0; c < channels.length; c++) {
            this.datasets[f].getSeries(c).clear();
         }
      }
   }
   public void clear(){
      alreadyShownList_.clear();
      cellToDisplayList_.validate();
      clearPlot();
   }
   @Override
   public void mouseClicked(MouseEvent e) {
      if (e.getClickCount() == 2) {
         int index = cellToDisplayList_.locationToIndex(e.getPoint());
         if (index != -1) {
            Object item = alreadyShownList_.getElementAt(index);
            updatePlot(currentSoc.getCell((Integer) item));
         }
      }
   }

   @Override
   public void mousePressed(MouseEvent e) {

   }

   @Override
   public void mouseReleased(MouseEvent e) {

   }

   @Override
   public void mouseEntered(MouseEvent e) {

   }

   @Override
   public void mouseExited(MouseEvent e) {

   }

   @Override
   public void keyTyped(KeyEvent e) {

   }

   @Override
   public void keyPressed(KeyEvent e) {
      int index = Integer.MIN_VALUE;
      if (e.getKeyCode() == KeyEvent.VK_UP) {
         index = cellToDisplayList_.getSelectedIndex() - 1;
      } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
         index = cellToDisplayList_.getSelectedIndex() + 1;
      } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
         index = cellToDisplayList_.getSelectedIndex();
      }
      if (index >= 0 && index <= cellToDisplayList_.getLastVisibleIndex()) {
         Object item = alreadyShownList_.getElementAt(index);
         updatePlot(currentSoc.getCell((Integer) item));
      }
   }

   @Override
   public void keyReleased(KeyEvent e) {

   }

   public static void main(String[] args) {
      SOCVisualizer visualizer = new SOCVisualizer(new String[]{"CFP", "GFP"});
      DefaultSetOfCells soc = new DefaultSetOfCells("test");
      visualizer.updateCellList(soc);
      visualizer.setVisible(true);
   }
}
