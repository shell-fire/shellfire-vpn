/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui;

import java.util.HashMap;

import javax.swing.JLabel;

/**
 *
 * @author bettmenn
 */
class ContentPaneList {

    private HashMap<ContentPaneType, ContentPane> panes = new HashMap<ContentPaneType, ContentPane>();
    private ContentPaneType activePane = ContentPaneType.None;
    private ContentPaneType hoveredPane = ContentPaneType.None;

    void addPane(ContentPaneType paneType, ContentPane pane) {
        panes.put(paneType, pane);
    }

    void setActivePane(ContentPaneType paneType) {
        this.activePane = paneType;
    }

    void setHoveredPane(ContentPaneType paneType) {
        this.hoveredPane = paneType;
    }

    boolean isHovered(ContentPaneType paneType) {
        return this.hoveredPane == paneType;
    }

    boolean isActive(ContentPaneType paneType) {
        return this.activePane == paneType;
    }

    void updateButtons() {
        for (ContentPaneType paneType : this.panes.keySet()) {
            this.panes.get(paneType).setIdleState();
        }
        
        if (this.hoveredPane != ContentPaneType.None)
            this.panes.get(this.hoveredPane).setHoveredState();

        if (this.activePane != ContentPaneType.None)
            this.panes.get(this.activePane).setActiveState();
    }

    void setHoveredPane(JLabel button) {
        ContentPaneType paneType = this.getPaneByLabel(button);
        this.setHoveredPane(paneType);
    }

    private ContentPaneType getPaneByLabel(JLabel button) {
        for (ContentPaneType paneType : this.panes.keySet()) {
            ContentPane pane = this.panes.get(paneType);

            if (pane.getButton().equals(button)) {
                return paneType;
            }
        }

        return ContentPaneType.None;
    }

    void updateContentPanes() {
        for (ContentPaneType paneType : this.panes.keySet()) {
            if (paneType == this.activePane) {
                this.panes.get(paneType).setVisible(true);
            } else {
                this.panes.get(paneType).setVisible(false);
            }
        }
    }

    void setActivePane(JLabel button) {
        ContentPaneType paneType = this.getPaneByLabel(button);
        this.setActivePane(paneType);
    }
}