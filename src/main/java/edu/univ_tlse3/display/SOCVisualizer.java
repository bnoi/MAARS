package edu.univ_tlse3.display;

import edu.univ_tlse3.cellstateanalysis.Cell;
import edu.univ_tlse3.cellstateanalysis.SetOfCells;
import org.jfree.chart.ChartPanel;
import org.micromanager.internal.utils.ReportingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

/**
 * Created by tong on 25/10/16.
 */
public class SOCVisualizer {
    private DefaultListModel alreadyShownList_ = new DefaultListModel();
    private SetOfCells setOfCells_;
    private JFrame frame_;

    public SOCVisualizer() {
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
//      sort();
    }

    public void sort() {
        Object[] contents = alreadyShownList_.toArray();
        Arrays.sort(contents);
        alreadyShownList_.copyInto(contents);
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
        frame_.setLayout(new BorderLayout(2, 1));
        final Container contentPane = frame_.getContentPane();
        frame_.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        final JPanel[] lastChartPanel = {new CellChartPanel("Waiting for data...")};
        final JList cellToDisplayList = new JList(alreadyShownList_);
        cellToDisplayList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    contentPane.remove(lastChartPanel[0]);
                    int index = cellToDisplayList.locationToIndex(e.getPoint());
                    Object item = alreadyShownList_.getElementAt(index);
                    Cell c = setOfCells_.getCell((Integer) item);
                    ChartPanel chartPanel = CellChartPanel.updateCellContent(c);
                    chartPanel.setVisible(true);
                    contentPane.add(chartPanel);
                    lastChartPanel[0] = chartPanel;
                    SwingUtilities.updateComponentTreeUI(frame_);
                }
            }
        });
        cellToDisplayList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                super.keyTyped(keyEvent);
                ReportingUtils.logMessage(keyEvent.getKeyChar() + "-" + "r".charAt(0));
                if (keyEvent.getKeyChar() == "r".charAt(0) || keyEvent.getKeyChar() == "R".charAt(0)) {
                    contentPane.remove(lastChartPanel[0]);
                    int index = cellToDisplayList.getSelectedIndex();
                    Object item = alreadyShownList_.getElementAt(index);
                    Cell c = setOfCells_.getCell((Integer) item);
                    ChartPanel chartPanel = CellChartPanel.updateCellContent(c);
                    chartPanel.setVisible(true);
                    contentPane.add(chartPanel);
                    lastChartPanel[0] = chartPanel;
                    SwingUtilities.updateComponentTreeUI(frame_);
                }
            }
        });
        //Add content to the window.
        JScrollPane scrollPane = new JScrollPane(cellToDisplayList);
        scrollPane.setMinimumSize(new Dimension(100, 200));
        contentPane.add(scrollPane, BorderLayout.WEST, 0);
        contentPane.add(lastChartPanel[0], BorderLayout.CENTER, 1);

        frame_.validate();
        //Display the window.
        frame_.setPreferredSize(new java.awt.Dimension(600, 570));
        frame_.pack();
        frame_.setVisible(true);
    }
}
