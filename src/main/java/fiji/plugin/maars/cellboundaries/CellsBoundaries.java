package fiji.plugin.maars.cellboundaries;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Label;
import java.awt.Panel;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import ij.IJ;
import ij.gui.NonBlockingGenericDialog;
import ij.plugin.PlugIn;

public class CellsBoundaries implements PlugIn {

	

	public void run(String arg) {
		setMainWindow();
		mainWindow.showDialog();



	}

}
