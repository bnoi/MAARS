package edu.univ_tlse3.display;

import edu.univ_tlse3.cellstateanalysis.Cell;
import edu.univ_tlse3.cellstateanalysis.SetOfCells;
import org.jfree.chart.ChartPanel;
import org.micromanager.internal.utils.ReportingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by tong on 25/10/16.
 */
public class SOCVisualizer {
    private DefaultListModel alreadyShownList_ = new DefaultListModel();
    private SetOfCells setOfCells_;
    private JFrame frame_;
    private JTextField pathToSoc_;

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
        Integer[] contents = (Integer[]) alreadyShownList_.toArray();
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
                    int index = cellToDisplayList.locationToIndex(e.getPoint());
                    if (index != -1){
                        contentPane.remove(lastChartPanel[0]);
                        Object item = alreadyShownList_.getElementAt(index);
                        Cell c = setOfCells_.getCell((Integer) item);
                        ChartPanel chartPanel = CellChartPanel.updateCellContent(c);
                        chartPanel.setVisible(true);
                        contentPane.add(chartPanel);
                        lastChartPanel[0] = chartPanel;
                        frame_.revalidate();
                    }
                }
            }
        });
        cellToDisplayList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                super.keyTyped(keyEvent);
                if (keyEvent.getKeyCode() == KeyEvent.VK_UP || keyEvent.getKeyCode() == KeyEvent.VK_DOWN) {
                    contentPane.remove(lastChartPanel[0]);
                    int index = cellToDisplayList.getSelectedIndex();
                    Object item = alreadyShownList_.getElementAt(index);
                    Cell c = setOfCells_.getCell((Integer) item);
                    ChartPanel chartPanel = CellChartPanel.updateCellContent(c);
                    chartPanel.setVisible(true);
                    contentPane.add(chartPanel);
                    lastChartPanel[0] = chartPanel;
                    frame_.revalidate();
                }
            }
        });
        //Add content to the window.
        JScrollPane scrollPane = new JScrollPane(cellToDisplayList);
        scrollPane.setMinimumSize(new Dimension(100, 200));
        JPanel loadSocPanel = new JPanel(new BorderLayout(2,1));
        JButton loadSocBut = new JButton("Load");
        JPanel pathToSocTFPanel = new JPanel();
        pathToSoc_ = new JFormattedTextField(String.class);
        pathToSoc_.setColumns(10);
        pathToSoc_.setText("Path to soc object");
        pathToSocTFPanel.add(pathToSoc_);
        loadSocBut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    FileInputStream f_in = new
                            FileInputStream(pathToSoc_.getText() + File.separator +"SetOfCell.serialize");
                    ObjectInputStream obj_in =
                            new ObjectInputStream (f_in);
                    Object obj = obj_in.readObject();
                    if (obj instanceof SetOfCells)
                    {
                        // Cast object to a soc
                        ReportingUtils.logMessage("updated");
                        SetOfCells soc = (SetOfCells) obj;
                        updateCellsDisplay(soc);

                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }


            }
        });
        loadSocPanel.add(pathToSocTFPanel, BorderLayout.CENTER);
        loadSocPanel.add(loadSocBut, BorderLayout.SOUTH);

        contentPane.add(scrollPane, BorderLayout.WEST, 0);
        contentPane.add(lastChartPanel[0], BorderLayout.CENTER, 1);
        contentPane.add(loadSocPanel,BorderLayout.EAST,2);

        frame_.validate();
        //Display the window.
        frame_.setPreferredSize(new java.awt.Dimension(600, 570));
        frame_.pack();
        frame_.setVisible(true);
    }
}
