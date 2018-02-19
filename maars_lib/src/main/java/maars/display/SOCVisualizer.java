package maars.display;

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

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
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

   /** The datasets. */
   private XYSeriesCollection[] datasets = new XYSeriesCollection[SUBPLOT_COUNT];;

   /** The number of subplots. */
   public static final int SUBPLOT_COUNT = SpotSetAnalyzor.GeoParamSet.length;

   /** The most recent value added to series 1. */
   private double[] lastValue = new double[SUBPLOT_COUNT];

   final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new NumberAxis());

   public SOCVisualizer() {
      super("Display cells with at least 1 spot detected");
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

      for (int i = 0; i < SUBPLOT_COUNT; i++) {
         this.lastValue[i] = 0.0;
         final XYSeries series = new XYSeries("Random " + i);
         this.datasets[i] = new XYSeriesCollection(series);
         final NumberAxis rangeAxis = new NumberAxis("Y" + i);
         rangeAxis.setAutoRangeIncludesZero(false);
         final XYPlot subplot = new XYPlot(
               this.datasets[i], null, rangeAxis, new StandardXYItemRenderer()
         );
         subplot.setBackgroundPaint(Color.lightGray);
         subplot.setDomainGridlinePaint(Color.white);
         subplot.setRangeGridlinePaint(Color.white);
         plot.add(subplot);
      }

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

      chartPanel.setPreferredSize(new java.awt.Dimension(500, 470));

      alreadyShownList_.add(0,1);
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

      content.add(splitPane, BorderLayout.WEST);
      content.add(loadSocPanel, BorderLayout.EAST);

      cellToDisplayList_.addMouseListener(this);

      cellToDisplayList_.addKeyListener(this);

      this.pack();
   }

   public void updateCellList(DefaultSetOfCells defaultSetOfCells) {
      alreadyShownList_.clear();
      cellToDisplayList_.clearSelection();
      this.currentSoc = defaultSetOfCells;
      for (Integer cellIndex : currentSoc.getPotentialMitosisCell()) {
         int cellNb = currentSoc.getCell(cellIndex).getCellNumber();
         alreadyShownList_.addElement(cellNb);
      }
   }

   public void updatePlot(DefaultSetOfCells soc, int cellNb){
      for (int i = 0; i < SUBPLOT_COUNT; i++) {
         this.datasets[i].getSeries(0).clear();
         soc.getCell(cellNb).getGeometryContainer();
      }
   }

   @Override
   public void mouseClicked(MouseEvent e) {
      if (e.getClickCount() == 2) {
         int index = cellToDisplayList_.locationToIndex(e.getPoint());
         if (index != -1) {
            Object item = alreadyShownList_.getElementAt(index);
            updatePlot(currentSoc, (Integer) item);
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
         updatePlot(currentSoc, (Integer) item);
      }
   }

   @Override
   public void keyReleased(KeyEvent e) {

   }

   public static void main(String[] args) {
      SOCVisualizer visualizer = new SOCVisualizer();
      DefaultSetOfCells soc = new DefaultSetOfCells("test");
      visualizer.updateCellList(soc);
      visualizer.setVisible(true);
   }
}
