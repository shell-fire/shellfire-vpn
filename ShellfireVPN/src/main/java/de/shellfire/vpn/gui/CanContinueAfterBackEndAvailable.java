package de.shellfire.vpn.gui;

//import de.shellfire.vpn.gui.controller.ProgressDialogController;
import java.awt.Frame;

public interface CanContinueAfterBackEndAvailable {

  void continueAfterBackEndAvailabled();

  ProgressDialog getDialog();
  
  //ProgressDialogController getDialogFx();
}
