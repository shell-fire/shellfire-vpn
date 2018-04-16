/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Tcheutchoua Steve
 */
public class VpnComparisonFXTableModel {
    private StringProperty free;
    private StringProperty premium ;
    private StringProperty premiumPlus;

    public VpnComparisonFXTableModel() {
    }

    
    
    public VpnComparisonFXTableModel(String free, String premium, String premiumPlus) {
        this.free = new SimpleStringProperty( free);
        this.premium = new SimpleStringProperty( premium);
        this.premiumPlus = new SimpleStringProperty( premiumPlus);
    }

    
    public String getFree() {
        return free.get();
    }

    public void setFree(String free) {
        this.free.set( free);
    }

    public String getPremium() {
        return premium.get();
    }

    public void setPremium(String premium) {
        this.premium.set(premium);
    }

    public String getPremiumPlus() {
        return premiumPlus.get();
    }

    public void setPremiumPlus(StringProperty premiumPlus) {
        this.premiumPlus = premiumPlus;
    }
    
    
}
