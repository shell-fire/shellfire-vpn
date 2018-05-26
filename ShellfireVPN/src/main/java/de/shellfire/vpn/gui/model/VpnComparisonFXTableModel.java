/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.model;

import de.shellfire.vpn.webservice.model.VpnEntry;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Tcheutchoua Steve
 */
public class VpnComparisonFXTableModel {
    private StringProperty connection;
    private ObjectProperty<VpnEntry> free;
    private ObjectProperty<VpnEntry> premium ;
    private ObjectProperty<VpnEntry> premiumPlus;
    // Used to determine if the comparison model is a container or not
    private BooleanProperty isContainer ;

    public VpnComparisonFXTableModel() {
    }

    
    
    public VpnComparisonFXTableModel(String connection, Object free, Object premium, Object premiumPlus, boolean container) {
        this.connection = new SimpleStringProperty(connection);
        this.free = new SimpleObjectProperty( free);
        this.premium = new SimpleObjectProperty( premium);
        this.premiumPlus = new SimpleObjectProperty( premiumPlus);
        this.isContainer  = new SimpleBooleanProperty(container);
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
    
    public VpnEntry getFree() {
        return free.get();
    }

    public void setFree(VpnEntry free) {
        this.free.set( free);
    }

    public ObjectProperty<VpnEntry> freeProperty(){
        return free;
    }
    public VpnEntry getPremium() {
        return premium.get();
    }

    public void setPremium(VpnEntry premium) {
        this.premium.set(premium);
    }
    
    public ObjectProperty<VpnEntry> premiumProperty(){
        return premium;
    }
    
    public VpnEntry getPremiumPlus() {
        return premiumPlus.get();
    }

    public void setPremiumPlus(VpnEntry premiumPlus) {
        this.premiumPlus.set(premiumPlus);
    }
    
    public ObjectProperty<VpnEntry> premiumPlusProperty(){
        return premiumPlus ;
    }

    public Boolean getIsContainer() {
        return isContainer.get();
    }

    public void setIsContainer(Boolean isContainer) {
        this.isContainer.set(isContainer);
    }
    
    public BooleanProperty isContainerProperty(){
        return isContainer;
    }
}
