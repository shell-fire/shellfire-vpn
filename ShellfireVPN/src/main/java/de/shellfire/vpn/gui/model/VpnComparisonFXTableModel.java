/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Tcheutchoua Steve
 */
public class VpnComparisonFXTableModel {
    private StringProperty connection;
    private ObjectProperty free;
    private ObjectProperty premium ;
    private ObjectProperty premiumPlus;

    public VpnComparisonFXTableModel() {
    }

    
    
    public VpnComparisonFXTableModel(String connection, Object free, Object premium, Object premiumPlus) {
        this.connection = new SimpleStringProperty(connection);
        this.free = new SimpleObjectProperty( free);
        this.premium = new SimpleObjectProperty( premium);
        this.premiumPlus = new SimpleObjectProperty( premiumPlus);
    }

    public String getConnection() {
        return connection.get();
    }

    public void setConnection(String connection) {
        this.connection.set(connection);
    }

    public StringProperty connectionProperty(){
        return connection;
    }
    
    public Object getFree() {
        return free.get();
    }

    public void setFree(String free) {
        this.free.set( free);
    }

    public ObjectProperty freeProperty(){
        return free;
    }
    public Object getPremium() {
        return premium.get();
    }

    public void setPremium(String premium) {
        this.premium.set(premium);
    }
    
    public ObjectProperty premiumProperty(){
        return premium;
    }
    
    public Object getPremiumPlus() {
        return premiumPlus.get();
    }

    public void setPremiumPlus(ObjectProperty premiumPlus) {
        this.premiumPlus.set(premiumPlus);
    }
    
    public ObjectProperty premiumPlusProperty(){
        return premiumPlus ;
    }
}
