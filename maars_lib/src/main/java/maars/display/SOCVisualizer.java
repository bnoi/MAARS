package maars.display;

import maars.agents.Cell;
import maars.agents.SetOfCells;
import maars.io.IOUtils;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by tong on 25/10/16.
 */
public class SOCVisualizer extends JFrame {
   private DefaultListModel<Integer> alreadyShownList_ = new DefaultListModel<>();
   private JTextField pathToSoc_;
   private JList<Integer> cellToDisplayList_;

   public SOCVisualizer() {
      super("Display cells with at least 1 spot detected");
      cellToDisplayList_ = new JList<>(alreadyShownList_);
      cellToDisplayList_.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      cellToDisplayList_.setLayoutOrientation(JList.VERTICAL);
   }

   /**
    * Create the GUI and show it.  For thread safety,
    * this method should be invoked from
    * the event dispatch thread.
    *
    * @param setOfCells soc object
    */
   public void createGUI(SetOfCells setOfCells) {
      //Create and set up the window.
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new BorderLayout());
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

      JScrollPane scrollPane = new JScrollPane(cellToDisplayList_);
      scrollPane.setBorder(BorderFactory.createTitledBorder("Cell Numbers"));
      scrollPane.setToolTipText("number of cells with > 1 spot");

      JPanel chartPanel = new CellChartPanel();
      JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            scrollPane, chartPanel);
      chartPanel.setBorder(BorderFactory.createTitledBorder("Parameters"));
      chartPanel.setToolTipText("parameters of selected cell");

      cellToDisplayList_.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
               int index = cellToDisplayList_.locationToIndex(e.getPoint());
               if (index != -1) {
                  Object item = alreadyShownList_.getElementAt(index);
                  updateSplitPane(splitPane, setOfCells, (Integer) item);
               }
            }
         }
      });
      cellToDisplayList_.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent keyEvent) {
            super.keyPressed(keyEvent);
            int index = Integer.MIN_VALUE;
            if (keyEvent.getKeyCode() == KeyEvent.VK_UP) {
               index = cellToDisplayList_.getSelectedIndex() - 1;
            } else if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN) {
               index = cellToDisplayList_.getSelectedIndex() + 1;
            } else if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
               index = cellToDisplayList_.getSelectedIndex();
            }
            if (index >= 0 && index <= cellToDisplayList_.getLastVisibleIndex()) {
               Object item = alreadyShownList_.getElementAt(index);
               updateSplitPane(splitPane, setOfCells, (Integer) item);
            }
         }
      });

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
            if (obj instanceof SetOfCells) {
               // Cast object to a soc
               SetOfCells soc = (SetOfCells) obj;
               updateParameters(soc);
            }
         } catch (IOException | ClassNotFoundException e) {
            IOUtils.printErrorToIJLog(e);
         }
      });
      loadSocPanel.add(new JLabel("Path to soc object"), BorderLayout.NORTH);
      loadSocPanel.add(pathToSocTFPanel, BorderLayout.CENTER);
      loadSocPanel.add(loadSocBut, BorderLayout.SOUTH);


      splitPane.setOneTouchExpandable(true);
      splitPane.setDividerLocation(120);
      mainPanel.add(splitPane, BorderLayout.CENTER);
      mainPanel.add(loadSocPanel, BorderLayout.EAST);
      add(mainPanel);

      validate();
      //Display the window.
      setPreferredSize(new Dimension(600, 570));
      setSize(600, 570);
      pack();
   }

   public void updateParameters(SetOfCells setOfCells) {
      for (Integer cellIndex : setOfCells.getPotentialMitosisCell()) {
         int cellNb = setOfCells.getCell(cellIndex).getCellNumber();
         if (!alreadyShownList_.contains(cellNb)) {
            alreadyShownList_.addElement(cellNb);
         }
      }
   }

   private void updateSplitPane(JSplitPane splitPane, SetOfCells soc, int cellNb) {
      splitPane.remove(1);
      Cell c = soc.getCell(cellNb);
      ChartPanel chartPanel = CellChartPanel.updateCellContent(c);
      chartPanel.setVisible(true);
      splitPane.add(chartPanel, 1);
      splitPane.updateUI();
      splitPane.revalidate();
   }

   public void cleanUp() {
      alreadyShownList_.clear();
   }
//    public static void main(String[] args) {
//    }
}
