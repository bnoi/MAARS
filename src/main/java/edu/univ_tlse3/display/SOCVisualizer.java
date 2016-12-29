package edu.univ_tlse3.display;

import edu.univ_tlse3.cellstateanalysis.Cell;
import edu.univ_tlse3.cellstateanalysis.SetOfCells;
import edu.univ_tlse3.utils.IOUtils;
import org.jfree.chart.ChartPanel;
import org.micromanager.internal.utils.ReportingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * Created by tong on 25/10/16.
 *
 */
public class SOCVisualizer extends JFrame{
    private DefaultListModel<Integer> alreadyShownList_ = new DefaultListModel<>();
    private JTextField pathToSoc_;

    public SOCVisualizer() {
        super("Display potential mitotic cells");
    }

    public void updateCellsDisplay(SetOfCells setOfCells) {
//        ImageIcon icon = createImageIcon("images/middle.gif");
        for (Integer cellIndex : setOfCells.getPotentialMitosisCell()) {
            int cellNb = setOfCells.getCell(cellIndex).getCellNumber();
            if (!alreadyShownList_.contains(cellNb)) {
                alreadyShownList_.addElement(cellNb);
            }
        }
//      sort();
    }

//    public void sort() {
//        Integer[] contents = (Integer[]) alreadyShownList_.toArray();
//        Arrays.sort(contents);
//        alreadyShownList_.copyInto(contents);
//    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from
     * the event dispatch thread.
     */
    public void createGUI(SetOfCells setOfCells) {
        //Create and set up the window.
        setLayout(new BorderLayout(2, 1));
        final Container contentPane = getContentPane();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        final JPanel[] lastChartPanel = {new CellChartPanel("Waiting for data...")};
        final JList<Integer> cellToDisplayList = new JList<>(alreadyShownList_);
        cellToDisplayList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = cellToDisplayList.locationToIndex(e.getPoint());
                    if (index != -1){
                        contentPane.remove(lastChartPanel[0]);
                        Object item = alreadyShownList_.getElementAt(index);
                        Cell c = setOfCells.getCell((Integer) item);
                        ChartPanel chartPanel = CellChartPanel.updateCellContent(c);
                        chartPanel.setVisible(true);
                        contentPane.add(chartPanel);
                        lastChartPanel[0] = chartPanel;
                        revalidate();
                    }
                }
            }
        });
        cellToDisplayList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                super.keyPressed(keyEvent);
                int index = Integer.MIN_VALUE;
                if (keyEvent.getKeyCode() == KeyEvent.VK_UP) {
                    index = cellToDisplayList.getSelectedIndex()-1;
                }else if(keyEvent.getKeyCode() == KeyEvent.VK_DOWN) {
                    index = cellToDisplayList.getSelectedIndex()+1;
                }else if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
                    index = cellToDisplayList.getSelectedIndex();
                }
                if (index >=0 && index <= cellToDisplayList.getLastVisibleIndex()) {
                    contentPane.remove(lastChartPanel[0]);
                    Object item = alreadyShownList_.getElementAt(index);
                    Cell c = setOfCells.getCell((Integer) item);
                    ChartPanel chartPanel = CellChartPanel.updateCellContent(c);
                    chartPanel.setVisible(true);
                    contentPane.add(chartPanel);
                    lastChartPanel[0] = chartPanel;
                    revalidate();
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
        loadSocBut.addActionListener(actionEvent -> {
            try {
                FileInputStream f_in = new
                        FileInputStream(pathToSoc_.getText() + File.separator +"SetOfCell.serialize");
                ObjectInputStream obj_in =
                        new ObjectInputStream (f_in);
                Object obj = obj_in.readObject();
                if (obj instanceof SetOfCells) {
                    // Cast object to a soc
                    ReportingUtils.logMessage("updated");
                    SetOfCells soc = (SetOfCells) obj;
                    updateCellsDisplay(soc);
                }
            } catch (IOException | ClassNotFoundException e) {
                IOUtils.printErrorToIJLog(e);
            }
        });
        loadSocPanel.add(pathToSocTFPanel, BorderLayout.CENTER);
        loadSocPanel.add(loadSocBut, BorderLayout.SOUTH);

        contentPane.add(scrollPane, BorderLayout.WEST, 0);
        contentPane.add(lastChartPanel[0], BorderLayout.CENTER, 1);
        contentPane.add(loadSocPanel,BorderLayout.EAST,2);

        validate();
        //Display the window.
        setPreferredSize(new java.awt.Dimension(600, 570));
        pack();
    }

    public void cleanUp(){
        alreadyShownList_.clear();
    }
}
