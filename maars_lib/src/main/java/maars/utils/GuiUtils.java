package maars.utils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Created by tongli on 04/03/2017.
 */
public class GuiUtils {
   public static Color bgColor = Color.decode("#fef9e7");
   public static Color butColor = Color.decode("#8af988");
   static Color titleColor = Color.BLACK;
   static Font titleFont = new Font("helvetica", Font.BOLD, 14);
   static Font secTitleFont = new Font("helvetica", Font.PLAIN, 12);

   public static TitledBorder addPanelTitle(String title) {
      TitledBorder border = BorderFactory.createTitledBorder(title);
      border.setTitleColor(titleColor);
      border.setTitleFont(titleFont);
      return border;
   }

   public static TitledBorder addSecondaryTitle(String title) {
      TitledBorder border = BorderFactory.createTitledBorder(title);
      border.setTitleColor(titleColor);
      border.setTitleFont(secTitleFont);
      return border;
   }

}
