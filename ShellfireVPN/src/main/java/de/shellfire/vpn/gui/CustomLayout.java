/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui;

import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.LayoutStyle;

/**
 * 
 * @author bettmenn
 */
public class CustomLayout extends LayoutStyle {

	private static LayoutStyle instance;
	private static LayoutStyle originalStyle;

	public static void register() {
		if (instance == null) {
			instance = new CustomLayout();
			originalStyle = LayoutStyle.getInstance();
			LayoutStyle.setInstance(instance);
		}
	}

	public int getPreferredGap(JComponent component1, JComponent component2, ComponentPlacement type, int position, Container parent) {
		if (parent != null && parent.getName() != null) {
			if (parent.getName().equals("jLoginPanel") || parent.getName().equals("jSettingsPanel")
					|| parent.getName().equals("jProgressPanel")) {
				return originalStyle.getPreferredGap(component1, component2, type, position, parent);
			} else {
				return 0;
			}
		}

		return originalStyle.getPreferredGap(component1, component2, type, position, parent);
	}

	public int getContainerGap(JComponent component, int position, Container parent) {
		if (parent != null && parent.getName() != null) {
			if (parent.getName().equals("jLoginPanel") || parent.getName().equals("jSettingsPanel")
					|| parent.getName().equals("jProgressPanel")) {
				return originalStyle.getContainerGap(component, position, parent);
			} else {
				return 0;
			}
		}
		return originalStyle.getContainerGap(component, position, parent);
	}
}
