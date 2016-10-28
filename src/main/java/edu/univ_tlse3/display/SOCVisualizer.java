package edu.univ_tlse3.display;

import edu.univ_tlse3.cellstateanalysis.Cell;
import edu.univ_tlse3.cellstateanalysis.SetOfCells;
import org.micromanager.internal.utils.ReportingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by tong on 25/10/16.
 */
public class SOCVisualizer {
   private DefaultListModel alreadyShownList_ = new DefaultListModel();
   private SetOfCells setOfCells_;
   private JFrame frame_;

   public SOCVisualizer() {
      super();
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

   public void showDialog() {
      frame_.setVisible(true);
   }

   /**
    * Create the GUI and show it.  For thread safety,
    * this method should be invoked from
    * the event dispatch thread.
    */
   public void createAndShowGUI() {
      //Create and set up the window.
      frame_ = new JFrame("Display potential mitotic cells");
      final Container contentPane = frame_.getContentPane();
      frame_.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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
               SwingUtilities.updateComponentTreeUI(frame_);
            }
         }
      });
      cellToDisplayList.addKeyListener(new KeyAdapter() {
         @Override
         public void keyTyped(KeyEvent keyEvent) {
            super.keyTyped(keyEvent);
            ReportingUtils.logMessage(keyEvent.getKeyChar() + "-" + "r".charAt(0));
            if(keyEvent.getKeyChar() == "r".charAt(0) || keyEvent.getKeyChar() == "R".charAt(0)) {
               contentPane.remove(lastChartPanel[0]);
               int index = cellToDisplayList.getSelectedIndex();
               Object item = alreadyShownList_.getElementAt(index);
               Cell c = setOfCells_.getCell((Integer) item);
               CellChartPanel cellChartPanel = new CellChartPanel(c);
               cellChartPanel.getChartPanel().setVisible(true);
               contentPane.add(cellChartPanel.getChartPanel());
               lastChartPanel[0] = cellChartPanel.getChartPanel();
               SwingUtilities.updateComponentTreeUI(frame_);
            }
         }
      });
      //Add content to the window.
      JScrollPane scrollPane = new JScrollPane(cellToDisplayList);
      scrollPane.setMinimumSize(new Dimension(200, 400));
      contentPane.add(scrollPane, BorderLayout.WEST, 0);
      contentPane.add(lastChartPanel[0], BorderLayout.CENTER, 1);

      frame_.validate();
      //Display the window.
      frame_.setPreferredSize(new java.awt.Dimension(600, 570));
      frame_.pack();
      frame_.setVisible(true);
   }
}
