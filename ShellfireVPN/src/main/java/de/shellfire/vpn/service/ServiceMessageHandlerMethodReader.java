package de.shellfire.vpn.service;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.util.InvocationTargetRuntimeException;
import net.openhft.chronicle.core.util.ObjectUtils;
import net.openhft.chronicle.bytes.*;
import net.openhft.chronicle.wire.*;
import net.openhft.chronicle.wire.BinaryWireCode;

import java.util.Map;
import java.lang.reflect.Method;

public class ServiceMessageHandlerMethodReader extends AbstractGeneratedMethodReader {
    // instances on which parsed calls are invoked
    private final Object instance0;
    private final WireParselet defaultParselet;
    
    // messageReceived
    private de.shellfire.vpn.messaging.Message messageReceivedarg0;
    
    // connectionStateChanged
    private de.shellfire.vpn.client.ConnectionStateChangedEvent connectionStateChangedarg0;
    
    public ServiceMessageHandlerMethodReader(MarshallableIn in, WireParselet defaultParselet, WireParselet debugLoggingParselet, MethodReaderInterceptorReturns interceptor, Object[] metaInstances, Object[] instances) {
        super(in, debugLoggingParselet);
        this.defaultParselet = defaultParselet;
        instance0 = instances[0];
    }
    
    @Override
    protected boolean readOneCall(WireIn wireIn) {
        ValueIn valueIn = wireIn.getValueIn();
        String lastEventName = "";
        if (wireIn.bytes().peekUnsignedByte() == BinaryWireCode.FIELD_NUMBER) {
            int methodId = (int) wireIn.readEventNumber();
            switch (methodId) {
                case -1:
                lastEventName = "history";
                break;
                
                default:
                lastEventName = Integer.toString(methodId);
                break;
            }
        }
        else {
            lastEventName = wireIn.readEvent(String.class);
        }
        try {
            if (Jvm.isDebug())
            debugLoggingParselet.accept(lastEventName, valueIn);
            if (lastEventName == null)
            throw new IllegalStateException("Failed to read method name or ID");
            switch (lastEventName) {
                case MethodReader.HISTORY:
                valueIn.marshallable(messageHistory);
                break;
                
                case "messageReceived":
                messageReceivedarg0 = valueIn.object(checkRecycle(messageReceivedarg0), de.shellfire.vpn.messaging.Message.class);
                try {
                    dataEventProcessed = true;
                    ((de.shellfire.vpn.messaging.MessageListener) instance0).messageReceived(messageReceivedarg0);
                }
                catch (Exception e) {
                    throw new InvocationTargetRuntimeException(e);
                }
                break;
                
                case "connectionStateChanged":
                connectionStateChangedarg0 = valueIn.object(checkRecycle(connectionStateChangedarg0), de.shellfire.vpn.client.ConnectionStateChangedEvent.class);
                try {
                    dataEventProcessed = true;
                    ((de.shellfire.vpn.client.ConnectionStateListener) instance0).connectionStateChanged(connectionStateChangedarg0);
                }
                catch (Exception e) {
                    throw new InvocationTargetRuntimeException(e);
                }
                break;
                
                default:
                defaultParselet.accept(lastEventName, valueIn);
            }
            return true;
        }
        catch (InvocationTargetRuntimeException e) {
            throw e;
        }
    }
    @Override
    protected boolean readOneCallMeta(WireIn wireIn) {
        ValueIn valueIn = wireIn.getValueIn();
        String lastEventName = "";
        if (wireIn.bytes().peekUnsignedByte() == BinaryWireCode.FIELD_NUMBER) {
            int methodId = (int) wireIn.readEventNumber();
            switch (methodId) {
                default:
                valueIn.skipValue();
                return true;
            }
        }
        else {
            lastEventName = wireIn.readEvent(String.class);
        }
        try {
            if (Jvm.isDebug())
            debugLoggingParselet.accept(lastEventName, valueIn);
            if (lastEventName == null)
            throw new IllegalStateException("Failed to read method name or ID");
            switch (lastEventName) {
                case MethodReader.HISTORY:
                valueIn.marshallable(messageHistory);
                break;
                
                default:
                valueIn.skipValue();
                return true;
            }
            return true;
        }
        catch (InvocationTargetRuntimeException e) {
            throw e;
        }
    }
}