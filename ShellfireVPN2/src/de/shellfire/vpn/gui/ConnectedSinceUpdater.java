/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.swing.JLabel;
import javax.swing.SwingWorker;

import org.xnap.commons.i18n.I18n;

/**
 *
 * @author bettmenn
 */
class ConnectedSinceUpdater extends SwingWorker<String, Void> {

    private final Date connectedSince;
    private final JLabel jConnectedSince;
    private static I18n i18n = VpnI18N.getI18n();

    public ConnectedSinceUpdater(Date connectedSince, JLabel jConnectedSince) {
        this.connectedSince = connectedSince;
        this.jConnectedSince = jConnectedSince;
    }

    protected void done() {
    	try {
			String result = get();
			this.jConnectedSince.setText(result);
			(new ConnectedSinceUpdater(connectedSince, jConnectedSince)).execute();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
    }
    
	@Override
	protected String doInBackground() throws Exception {
        Date now = new Date();
        long diffInSeconds = (now.getTime() - connectedSince.getTime()) / 1000;

        long diff[] = new long[]{0, 0, 0, 0};
        diff[3] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
        diff[2] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        diff[1] = (diffInSeconds = (diffInSeconds / 60));
        String since = String.format(
                "%d " + i18n.trn("Stunde", "Stunden", diff[1]) + 
                ", %d " + i18n.trn("Minute", "Minuten", diff[2]) + 
                ", %d " + i18n.trn("Sekunde", "Sekunden", diff[3]),
                diff[1],
                diff[2],
                diff[3]
        );

        SimpleDateFormat df = new SimpleDateFormat("E, H:m");
        String start = df.format(connectedSince);

        String text = start + " Uhr (" + since + ")";
        
        return text;
	}
}
