package edu.univ_tlse3.display;

import edu.univ_tlse3.cellstateanalysis.Cell;
import edu.univ_tlse3.cellstateanalysis.SetOfCells;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by tong on 25/10/16.
 */
public class SOCVisualizer extends JPanel {
   private DefaultListModel alreadyShownList_ = new DefaultListModel();
   private SetOfCells setOfCells_;

   public SOCVisualizer() {
      super(new GridLayout(1, 1));
   }

   public void updateCellsDisplay(SetOfCells setOfCells) {
//        ImageIcon icon = createImageIcon("images/middle.gif");
      setOfCells_ = setOfCells;
      for (Integer cellIndex : setOfCells_.getPotentialMitosisCell()) {
         int cellNb = setOfCells_.getCell(cellIndex).getCellNumber();
         if (!alreadyShownList_.contains(cellNb)) {
            alreadyShownList_.addElement(cellNb);
         }
      }
   }

   /**
    * Create the GUI and show it.  For thread safety,
    * this method should be invoked from
    * the event dispatch thread.
    */
   public void createAndShowGUI() {
      //Create and set up the window.
      final JFrame frame = new JFrame("Display potential mitotic cells");
      final Container contentPane = frame.getContentPane();
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

      final JPanel[] lastChartPanel = {new CellChartPanel().getChartPanel()};
      final JList cellToDisplayList = new JList(alreadyShownList_);
      cellToDisplayList.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
               contentPane.remove(lastChartPanel[0]);
               int index = cellToDisplayList.locationToIndex(e.getPoint());
               Object item = alreadyShownList_.getElementAt(index);
               Cell c = setOfCells_.getCell((Integer) item);
               CellChartPanel cellChartPanel = new CellChartPanel(c);
               cellChartPanel.getChartPanel().setVisible(true);
               contentPane.add(cellChartPanel.getChartPanel());
               lastChartPanel[0] = cellChartPanel.getChartPanel();
               SwingUtilities.updateComponentTreeUI(frame);
            }
         }
      });
      //Add content to the window.
      JScrollPane scrollPane = new JScrollPane(cellToDisplayList);
      scrollPane.setMinimumSize(new Dimension(200, 400));
      contentPane.add(scrollPane, BorderLayout.WEST, 0);
      contentPane.add(lastChartPanel[0], BorderLayout.CENTER, 1);

      frame.validate();
      //Display the window.
      frame.setPreferredSize(new java.awt.Dimension(600, 570));
      frame.pack();
      frame.setVisible(true);
   }
}
