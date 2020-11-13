package de.shellfire.vpn.service;

import java.io.IOException;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.messaging.Message;
import de.shellfire.vpn.messaging.MessageBroker;
import de.shellfire.vpn.messaging.MessageType;

public class Service {

	private static Logger log = Util.getLogger(Service.class.getCanonicalName());
	private static MessageBroker messageBroker;
	private static Service service;
	private static boolean stop = false;
	private final static String MAN = "Run with atg \"start\" or \"stop\" to control the service";

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			log.debug(MAN);
			return;
		}
		String arg = args[0];

		if ("start".equals(arg)) {
			start(args);
		} else if ("stop".equals(arg)) {
			stop(args);
		} else {
			log.debug(MAN);
		}
	}

	public static void start(String[] args) {
		log.info("Service starting up");

		service = new Service();
		service.run();

	}

	public static void stop(String[] args) {
		log.info("stop");
		stop = true;
	}

	private void run() {
		ServiceMessageHandler serviceMessageHandler = null;
		try {
			log.debug("initializting ServiceMessageHandler");
			serviceMessageHandler = new ServiceMessageHandler();

			log.debug("Service started, waiting for stop");
			while (!stop) {
				// 50 ms is enough to not use ANY cpu during sleep.
				Util.sleep(50);
			}
			;

			log.debug("Stop received, exiting");

		} catch (IOException e) {
			Service.handleException(e);
		} finally {
			if (serviceMessageHandler == null) {
				log.warn("serviceMessageHandler is null - can not shutdown gracefully");
			} else {
				log.debug("closing serviceMessagehandler...");
				serviceMessageHandler.close();
				log.debug("...done");
			}

		}
		log.debug("Service finished graceful shutdown. Good Bye!");
	}

	public static void handleException(Exception e) {
		log.error("Exception occured. Sending to client: {}", e.getMessage(), e);
		Message<Exception, Void> errorMessage = new Message<Exception, Void>(MessageType.Error, e);
		try {
			if (messageBroker != null) {
				messageBroker.sendMessage(errorMessage);
			}

		} catch (IOException e1) {
			log.error("Error occured while sending message to client: ", e.getMessage(), e);
		}
	}

}