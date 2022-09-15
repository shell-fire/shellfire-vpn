/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DownloadProgressDialog.java
 *
 * Created on 13.06.2011, 14:44:43
 */
package de.shellfire.vpn.updater;

import java.awt.Cursor;
import java.awt.Image;

import javax.swing.ImageIcon;

import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.i18n.VpnI18N;

/**
 *
 * @author bettmenn
 */
public class DownloadProgressDialog extends javax.swing.JDialog {

	private static I18n i18n = VpnI18N.getI18n();

	/** Creates new form DownloadProgressDialog */
	public DownloadProgressDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();
		this.loadIcon();
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		this.jProgressBar1.setIndeterminate(true);
		this.setLocationRelativeTo(null);
		this.pack();
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
	 * method is always regenerated by the Form Editor.
	 */
	private void initComponents() {

		jProgressBar1 = new javax.swing.JProgressBar();
		jLabel1 = new javax.swing.JLabel();
		jCurrent = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		jTotal = new javax.swing.JLabel();
		jByte = new javax.swing.JLabel();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		jLabel1.setText(i18n.tr("Downloading update..."));

		jCurrent.setText("0");

		jLabel3.setText("/");

		jTotal.setText("0");

		jByte.setText("KByte");

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout
				.createSequentialGroup().addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								.addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE).addContainerGap())
						.addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
								.addComponent(jCurrent, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGap(2, 2, 2).addComponent(jLabel3).addGap(18, 18, 18)
								.addComponent(jTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jByte)
								.addContainerGap()))));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jCurrent)
								.addComponent(jTotal).addComponent(jLabel3).addComponent(jByte))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		pack();
	}// </editor-fold>//GEN-END:initComponents

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JLabel jByte;
	private javax.swing.JLabel jCurrent;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JProgressBar jProgressBar1;
	private javax.swing.JLabel jTotal;
	// End of variables declaration//GEN-END:variables
	private Image iconIdle;

	public void setTotalLength(int len) {

		if (len > 0) {
			this.jProgressBar1.setIndeterminate(false);

			this.jProgressBar1.setMaximum(len);
			this.jTotal.setText("" + (int) (len / 1024));

		} else {
			this.jProgressBar1.setIndeterminate(true);
		}

	}

	public void setCurrentStatus(int count) {
		this.jProgressBar1.setValue(count);
		this.jCurrent.setText("" + (int) (count / 1024));

	}

	private void loadIcon() {
		this.iconIdle = new ImageIcon(getClass().getResource("/de/shellfire/vpn/updater/resources/sfvpn2-idle-big.png")).getImage();

		setIconImage(iconIdle);
	}
}
