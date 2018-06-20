/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.model;

import de.shellfire.vpn.Storage;
import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.RegisterForm;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.WebService;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

/**
 *
 * @author TcheutchouaSteve on May 20, 2018
 */
public class VpnComparisonFXHelper {

    private static final long serialVersionUID = 1L;
    private static final I18n i18n = VpnI18N.getI18n();
    private AttributeList vpnAttributeList;
    private String[] header = {"", i18n.tr("Free"), i18n.tr("Premium"), i18n.tr("Premium Plus")};
    private static final Logger log = Util.getLogger(RegisterForm.class.getCanonicalName());
    private WebService shellfireService = null;

    public VpnComparisonFXHelper() {
        //this.initData();
    }

    public String getColumnName(int columnIndex) {
        return this.header[columnIndex];
    }

    public void initData() {
        if (null == shellfireService) {
            shellfireService = (WebService) Storage.get(WebService.class);
        }

        vpnAttributeList = new AttributeList(shellfireService.getVpnComparisonTable());
        log.debug("VpnComparisonTableModel: Attribute list is " + vpnAttributeList.toString());

    }

    public void setService(WebService service) {
        this.shellfireService = service;
    }

    public LinkedList<AttributeContainer> getAttributeContainers() {
        return vpnAttributeList.getContainers();
    }
}
