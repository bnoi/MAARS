package edu.univ_tlse3.utils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Created by tongli on 04/03/2017.
 */
public class GuiUtils {
    static Color titleColor = Color.decode("#99a3a4");
    static Color sectitleColor = Color.decode("#99a3a4");
    static Font titleFont = new Font("helvetica", Font.BOLD, 14);
    static Font secTitleFont = new Font("helvetica", Font.PLAIN, 12);
    public static Color bgColor = Color.decode("#fef9e7");
    public static Color butColor = Color.decode("#8af988");

    public static TitledBorder addPanelTitle(String title){
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleColor(titleColor);
        border.setTitleFont(titleFont);
        return border;
    }

    public static TitledBorder addSecondaryTitle(String title){
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleColor(sectitleColor);
        border.setTitleFont(secTitleFont);
        return border;
    }

}
