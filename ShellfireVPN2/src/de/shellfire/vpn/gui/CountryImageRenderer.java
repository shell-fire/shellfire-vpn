/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.xnap.commons.i18n.I18n;

/**
 * 
 * @author bettmenn
 */
public class CountryImageRenderer extends DefaultTableCellRenderer {

  JLabel lbl = new JLabel();
  private static I18n i18n = VpnI18N.getI18n();
  private static CountryI18n countryI18n = VpnI18N.getCountryI18n();

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    setEnabled(table == null || table.isEnabled());
    Country c = (Country) value;
    lbl.setOpaque(true);
    
    if (isEnabled()) {
    	lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ContentPane.colorLightGray));
    } else {
    	lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ContentPane.colorVeryLightGray));
    }
    
    
    if (isSelected) {
		if (isEnabled()) {
			lbl.setForeground(ContentPane.colorDarkGrey);
		} else {
			lbl.setForeground(Color.darkGray);
		}
      
      lbl.setBackground(table.getSelectionBackground());
    } else {
			if (isEnabled()) {
				lbl.setBackground(table.getBackground());
				lbl.setForeground(table.getForeground());
			} else {
				lbl.setBackground(Color.white);
				lbl.setForeground(Color.lightGray);
			}     
    }

    ImageIcon icon = CountryMap.getIcon(c);
    lbl.setIcon(icon);
    lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 11));
    lbl.setText(getText(c));
    lbl.setHorizontalAlignment(JLabel.LEFT);

    return lbl;
  }

  public String getText(Country country) {
    return countryI18n.getCountryName(country);
   /*
    switch (country) {
    case Germany:
      return i18n.tr("Deutschland");
    case Usa:
      return i18n.tr("Usa");
    case UnitedKingdom:
      return i18n.tr("England");
    case France:
      return i18n.tr("Frankreich");
    case Switzerland:
      return i18n.tr("Schweiz");
    case Canada:
      return i18n.tr("Kanada");
    default:
      return country.name();
    }
    */
  }

}
