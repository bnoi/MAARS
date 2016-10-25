package edu.univ_tlse3.display;

import edu.univ_tlse3.cellstateanalysis.SetOfCells;

import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by tong on 25/10/16.
 */
public class SOCDisplayer extends JPanel{
    private JTabbedPane tabbedPane_ = new JTabbedPane();
    private SetOfCells setOfCells_;
    private ArrayList<Integer> tabAlreadyShown = new ArrayList<Integer>();
    public SOCDisplayer(SetOfCells setOfCells){
        super(new GridLayout(1,1));
        setOfCells_ = setOfCells;
        //Add the tabbed pane to this panel.
        add(tabbedPane_);

        //The following line enables to use scrolling tabs.
        tabbedPane_.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    public void updateCellsDisplay(SetOfCells setOfCells){
//        ImageIcon icon = createImageIcon("images/middle.gif");
        for (Integer cellIndex : setOfCells.getPotentialMitosisCell()) {
            int cellNb = setOfCells.getCell(cellIndex).getCellNumber();
            if (!tabAlreadyShown.contains(cellNb)) {
//            JComponent panel = makeTextPanel("Panel #" + cellNb);
                CellChartPanel cellChartPanel = new CellChartPanel("Cell " + cellNb);
                cellChartPanel.pack();
//            RefineryUtilities.centerFrameOnScreen(cellChartPanel);
//            cellChartPanel.setVisible(true);
                tabbedPane_.addTab("Cell " + cellNb, null, cellChartPanel.getChartPanel(),
                        "charts of cell " + cellNb);
                tabAlreadyShown.add(cellNb);
//            tabbedPane.setMnemonicAt(cellNb - 1, KeyEvent.VK_1);
            }
        }
        //Better put this inside the for loop but impossible considering the computer performance
//        tabbedPane_.updateUI();
        updateUI();
    }

//    protected JComponent makeTextPanel(String text) {
//        JPanel panel = new JPanel(false);
//        JLabel filler = new JLabel(text);
//        filler.setHorizontalAlignment(JLabel.CENTER);
//        panel.setLayout(new GridLayout(1,1));
//        panel.add(filler);
//        return panel;
//    }
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from
     * the event dispatch thread.
     */
    public static void createAndShowGUI(SOCDisplayer socDisplayer) {
        //Create and set up the window.
        JFrame frame = new JFrame("Display potential mitotic cells");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //Add content to the window.
        frame.add(socDisplayer, BorderLayout.CENTER);

        //Display the window.
//        frame.setSize(new java.awt.Dimension(500, 470));
        frame.setPreferredSize(new java.awt.Dimension(600, 570));
        frame.pack();
        frame.setVisible(true);
    }

//    public static void main(String[] args) {
//        //Schedule a job for the event dispatch thread:
//        //creating and showing this application's GUI.
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                final CellChartPanel demo = new CellChartPanel("Cell test");
//                demo.pack();
//                RefineryUtilities.centerFrameOnScreen(demo);
//                demo.setVisible(true);
//                //Turn off metal's use of bold fonts
//                UIManager.put("swing.boldMetal", Boolean.FALSE);
////                createAndShowGUI();
//            }
//        });
//    }
}
