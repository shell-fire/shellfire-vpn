/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.model;

import javax.swing.table.AbstractTableModel;

import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Storage;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.ShellfireService;

/**
 * 
 * @author bettmenn
 */
public class VpnComparisonTableModel extends AbstractTableModel {

  private static final long serialVersionUID = 1L;
  private static I18n i18n = VpnI18N.getI18n();
  private AttributeList vpnAttributeList;
  private String[] header = { "", i18n.tr("Free"), i18n.tr("Premium"), i18n.tr("Premium Plus") };

  public VpnComparisonTableModel() {
    this.initData();
  }

  @Override
  public int getRowCount() {
    int rowCount = vpnAttributeList.getRowCount();
    return rowCount;
  }

  @Override
  public int getColumnCount() {
    return this.header.length;
  }

  public String getColumnName(int columnIndex) {
    return this.header[columnIndex];
  }

  private void initData() {
    /*
    vpnAttributeList = new VpnAttributeList();
    VpnAttributeContainer attributeContainer = new VpnAttributeContainer(i18n.tr("Verbindung"));
    attributeContainer.add(i18n.tr("Anonym im Internet surfen"), true, true);
    attributeContainer.add(i18n.tr("Eigene IP-Adresse wird versteckt"), true, true);
    attributeContainer.add(i18n.tr("Internet-Verbindung verschlüsselt"), true, true);
    attributeContainer.add(i18n.tr("Sicher in öffentlichen WLANs surfen"), true, true);
    attributeContainer.add(i18n.tr("Traffic / Übertragungsvolumen"), i18n.tr("unlimitiert"), i18n.tr("unlimitiert"));
    attributeContainer.add(i18n.tr("Geschwindigkeit"), new Star(1, i18n.tr("384 kbit/sek")), new Star(3, i18n.tr("7200 kbit/sek")));
    attributeContainer.add(i18n.tr("Verschlüsselung"), new Star(2, i18n.tr("128 bit")), new Star(3, i18n.tr("192 bit")));
    attributeContainer.add(i18n.tr("Server in Deutschland"), true, true);
    attributeContainer.add(i18n.tr("Server in USA"), false, true);
    attributeContainer.add(i18n.tr("Serverort jederzeit wechseln"), false, true);
    vpnAttributeList.add(attributeContainer);

    attributeContainer = new VpnAttributeContainer(i18n.tr("Streaming Seiten"));
    attributeContainer.add(i18n.tr("YouTube (alle Videos)"), false, true);
    attributeContainer.add(i18n.tr("Hulu"), false, true);
    attributeContainer.add(i18n.tr("ABC, CBS, FOX, NBC u.v.m."), false, true);
    vpnAttributeList.add(attributeContainer);

    attributeContainer = new VpnAttributeContainer(i18n.tr("Sonstiges"));
    attributeContainer.add(i18n.tr("Keine Wartezeit beim Verbinden"), false, true);
    attributeContainer.add(i18n.tr("Keine Werbe Popups"), false, true);
    vpnAttributeList.add(attributeContainer);

    attributeContainer = new VpnAttributeContainer(i18n.tr("VPN Typen"));
    attributeContainer.add(i18n.tr("OpenVPN"), true, true);
    attributeContainer.add(i18n.tr("PPTP VPN"), false, true);
    attributeContainer.add(i18n.tr("L2TP IPSec VPN"), false, true);
    vpnAttributeList.add(attributeContainer);

    attributeContainer = new VpnAttributeContainer(i18n.tr("Geräte / Betriebssysteme"));
    attributeContainer.add(i18n.tr("Windows XP, Vista, 7"), true, true);
    attributeContainer.add(i18n.tr("Mac OS X"), false, true);
    attributeContainer.add(i18n.tr("Linux"), false, true);
    attributeContainer.add(i18n.tr("iPhone, iPad, iPod"), false, true);
    attributeContainer.add(i18n.tr("Android Handys und Tablets"), false, true);
    attributeContainer.add(i18n.tr("dd-wrt Router"), false, true);

    vpnAttributeList.add(attributeContainer);

    attributeContainer = new VpnAttributeContainer(i18n.tr("Preise"));
    attributeContainer.add(i18n.tr("1 Monat"), i18n.tr("kostenlos"), i18n.tr(" 6,49 Euro"));
    attributeContainer.add(i18n.tr("3 Monate"), i18n.tr("kostenlos"), i18n.tr("19,47 Euro"));
    attributeContainer.add(i18n.tr("12 Monate (-10%)"), i18n.tr("kostenlos"), i18n.tr("70,10 Euro"));
    vpnAttributeList.add(attributeContainer);
*/
    ShellfireService service = (ShellfireService)Storage.get(ShellfireService.class);
    
    vpnAttributeList = new AttributeList(service.getVpnComparisonTable());
    
    
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    Object result = vpnAttributeList.getValueAt(rowIndex, columnIndex);
    return result;
  }
}
