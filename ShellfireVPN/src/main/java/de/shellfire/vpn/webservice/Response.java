package de.shellfire.vpn.webservice;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.exception.VpnException;

public class Response<ResponseType> {
  
  private static Logger log = Util.getLogger(Response.class.getCanonicalName());
  public static final String STATUS_SUCCESS = "success";
  public static final String STATUS_ERROR = "error";

  private String status = null;
  private String message = null;
  private String errorCode = null;

  private ResponseType data = null;

  public String getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public ResponseType getData() {
    return this.data;
  }

  public void setMessage(String message) {
    this.message = message;
  }
  
  public boolean isSuccess() {
    return this.status.equals(STATUS_SUCCESS);
  }
  
  public boolean isError() {
    return this.status.equals(STATUS_ERROR);
  }

  /**
   * validate the response of a JSON HTtp Query.
   * 
   * In some instances, the error handling is done more on the gui side, e.g. 
   * login error with wrong passwords should not force an exception
   * @param resp the response to validate
   * @throws VpnException in case an error occured (resp is null, error or no data contained
   */
  public void validate() throws VpnException {
    if (isError()) {
      log.error("Could not load - error: {}", getMessage());
      throw new VpnException("Could not load  error: " + getMessage());
    }

    ResponseType data = getData();
    if (data == null) {
      log.error("Could not load - status success but no data: {}", getMessage());
      throw new VpnException("Could not load - status success but no data: {}" + getMessage());
    }
  }

  public void setStatus(String status) {
    this.status = status;
  }
}