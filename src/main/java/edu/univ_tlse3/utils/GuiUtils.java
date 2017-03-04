package edu.univ_tlse3.utils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Created by tongli on 04/03/2017.
 */
public class GuiUtils {
    static Color titleColor = Color.decode("#FF8C00");
    static Color sectitleColor = Color.decode("#FF8000");
    static Font titleFont = new Font("helvetica", Font.BOLD, 14);
    static Font secTitleFont = new Font("helvetica", Font.PLAIN, 12);
    public static TitledBorder addPanelTitle(JPanel jPanel, String title){
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleColor(titleColor);
        border.setTitleFont(titleFont);
        return border;
    }

    public static TitledBorder addSecondaryTitle(JPanel jPanel, String title){
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleColor(sectitleColor);
        border.setTitleFont(secTitleFont);
        return border;
    }

}
