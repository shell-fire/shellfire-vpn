package de.shellfire.vpn.messaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import com.sun.jna.WString;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinBase.SECURITY_ATTRIBUTES;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.PointerByReference;
public class MessageBroker {

    // Constants for file mapping and events
	private static final int SDDL_REVISION_1 = 0x00000001;
    private static final int FILE_MAP_COPY = 0x00000001;
    private static final int FILE_MAP_WRITE = 0x00000002;
    private static final int FILE_MAP_READ = 0x00000004;
    private static final int FILE_MAP_ALL_ACCESS = 0x001f;

    private static final int SHARED_MEMORY_SIZE = 8192; // Adjust size based on expected message size
    private static final String SHARED_MEMORY_NAME_CLIENT_TO_SERVER = "Global\\SharedMemoryClientToServer";
    private static final String SHARED_MEMORY_NAME_SERVER_TO_CLIENT = "Global\\SharedMemoryServerToClient";

    private static final String EVENT_NAME_CLIENT_TO_SERVER = "Global\\EventClientToServer";
    private static final String EVENT_NAME_SERVER_TO_CLIENT = "Global\\EventServerToClient";

    private static final String EVENT_NAME_CLIENT_TO_SERVER_ACK = "Global\\EventClientToServerAck";
    private static final String EVENT_NAME_SERVER_TO_CLIENT_ACK = "Global\\EventServerToClientAck";

    private SECURITY_ATTRIBUTES securityAttributes;
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(MessageBroker.class);
	private static MessageBroker instance;
	private final Map<UUID, CompletableFuture<Message<?, ?>>> pendingRequests = new ConcurrentHashMap<>();
    private final boolean isClient;

    private HANDLE hMapFileClientToServer;
    private Pointer sharedMemoryClientToServer;

    private HANDLE hMapFileServerToClient;
    private Pointer sharedMemoryServerToClient;

    private HANDLE eventClientToServer;
    private HANDLE eventServerToClient;

    private HANDLE eventClientToServerAck;
    private HANDLE eventServerToClientAck;

    private HANDLE stopEvent;

    private MessageListener<?> messageListener;

    public static MessageBroker getInstance(boolean isClient) throws IOException {
    	if (instance == null) {
    		instance = new MessageBroker(isClient);
    	}
    	
    	return instance;
    }
    
    private MessageBroker(boolean isClient) throws IOException {
        this.isClient = isClient;
        init();
    }

    private void init() throws IOException {
        log.debug("MessageBroker.init() - start");
        Kernel32 kernel32 = Kernel32.INSTANCE;

        // Create shared memory with custom security attributes
        securityAttributes = createSecurityAttributes();

        // Create or open shared memory for client-to-server communication
        hMapFileClientToServer = kernel32.CreateFileMapping(
                WinBase.INVALID_HANDLE_VALUE,
                securityAttributes,
                WinNT.PAGE_READWRITE,
                0,
                SHARED_MEMORY_SIZE,
                SHARED_MEMORY_NAME_CLIENT_TO_SERVER);

        if (hMapFileClientToServer == null || WinBase.INVALID_HANDLE_VALUE.equals(hMapFileClientToServer)) {
            throw new IOException("Could not create client-to-server file mapping. Error code: " + kernel32.GetLastError());
        }

        sharedMemoryClientToServer = kernel32.MapViewOfFile(
                hMapFileClientToServer,
                FILE_MAP_READ | FILE_MAP_WRITE,
                0,
                0,
                SHARED_MEMORY_SIZE);

        if (sharedMemoryClientToServer == null) {
            kernel32.CloseHandle(hMapFileClientToServer);
            throw new IOException("Could not map view of client-to-server file. Error code: " + kernel32.GetLastError());
        }

        // Create or open shared memory for server-to-client communication
        hMapFileServerToClient = kernel32.CreateFileMapping(
                WinBase.INVALID_HANDLE_VALUE,
                securityAttributes,
                WinNT.PAGE_READWRITE,
                0,
                SHARED_MEMORY_SIZE,
                SHARED_MEMORY_NAME_SERVER_TO_CLIENT);

        if (hMapFileServerToClient == null || WinBase.INVALID_HANDLE_VALUE.equals(hMapFileServerToClient)) {
            throw new IOException("Could not create server-to-client file mapping. Error code: " + kernel32.GetLastError());
        }

        sharedMemoryServerToClient = kernel32.MapViewOfFile(
                hMapFileServerToClient,
                FILE_MAP_READ | FILE_MAP_WRITE,
                0,
                0,
                SHARED_MEMORY_SIZE);

        if (sharedMemoryServerToClient == null) {
            kernel32.CloseHandle(hMapFileServerToClient);
            throw new IOException("Could not map view of server-to-client file. Error code: " + kernel32.GetLastError());
        }

        // Create or open events with custom security attributes
        eventClientToServer = kernel32.CreateEvent(
        		securityAttributes,
                true,
                false,
                EVENT_NAME_CLIENT_TO_SERVER);

        if (eventClientToServer == null || WinBase.INVALID_HANDLE_VALUE.equals(eventClientToServer)) {
            throw new IOException("Could not create eventClientToServer. Error code: " + kernel32.GetLastError());
        }

        eventServerToClient = kernel32.CreateEvent(
        		securityAttributes,
                true,
                false,
                EVENT_NAME_SERVER_TO_CLIENT);

        if (eventServerToClient == null || WinBase.INVALID_HANDLE_VALUE.equals(eventServerToClient)) {
            throw new IOException("Could not create eventServerToClient. Error code: " + kernel32.GetLastError());
        }

        // Create acknowledgment events
        eventClientToServerAck = kernel32.CreateEvent(
        		securityAttributes,
                true,
                false,
                EVENT_NAME_CLIENT_TO_SERVER_ACK);

        if (eventClientToServerAck == null || WinBase.INVALID_HANDLE_VALUE.equals(eventClientToServerAck)) {
            throw new IOException("Could not create eventClientToServerAck. Error code: " + kernel32.GetLastError());
        }

        eventServerToClientAck = kernel32.CreateEvent(
        		securityAttributes,
                true,
                false,
                EVENT_NAME_SERVER_TO_CLIENT_ACK);

        if (eventServerToClientAck == null || WinBase.INVALID_HANDLE_VALUE.equals(eventServerToClientAck)) {
            throw new IOException("Could not create eventServerToClientAck. Error code: " + kernel32.GetLastError());
        }

        // Create stop event
        stopEvent = kernel32.CreateEvent(
        		securityAttributes,
                true,   // Manual reset event
                false,  // Initial state is nonsignaled
                null    // Anonymous event
        );

        if (stopEvent == null || WinBase.INVALID_HANDLE_VALUE.equals(stopEvent)) {
            throw new IOException("Could not create stopEvent. Error code: " + kernel32.GetLastError());
        }

        log.debug("MessageBroker.init() - end");
    }

    public <T extends Serializable, E extends Serializable> void sendMessage(Message<T, E> message) throws IOException {
        log.debug("sendMessage(message={}) - start", message);

        Kernel32 kernel32 = Kernel32.INSTANCE;
        HANDLE eventHandle;
        HANDLE ackEventHandle;
        Pointer sharedMemoryPointer;

        if (isClient) {
            // Client sending message to server
            eventHandle = eventClientToServer;
            ackEventHandle = eventClientToServerAck;
            sharedMemoryPointer = sharedMemoryClientToServer;
            log.debug("Client sending message to server.");
        } else {
            // Server sending message to client
            eventHandle = eventServerToClient;
            ackEventHandle = eventServerToClientAck;
            sharedMemoryPointer = sharedMemoryServerToClient;
            log.debug("Server sending message to client.");
        }

        // Reset the acknowledgment event before sending
        kernel32.ResetEvent(ackEventHandle);

        // Serialize message to byte array
        byte[] serializedMessage = serializeMessage(message);

        // Write to shared memory
        if (serializedMessage.length > SHARED_MEMORY_SIZE) {
            throw new IOException("Message size exceeds shared memory capacity.");
        }

        synchronized (sharedMemoryPointer) {
            // Write the message to shared memory
            sharedMemoryPointer.write(0, serializedMessage, 0, serializedMessage.length);

            // Reset the event in case it was set
            kernel32.ResetEvent(eventHandle);

            // Signal that a message is ready
            if (!kernel32.SetEvent(eventHandle)) {
                throw new IOException("Failed to set event. Error code: " + kernel32.GetLastError());
            }
        }

        // Wait for acknowledgment
        log.debug("Waiting for acknowledgment...");
        int ackResult = kernel32.WaitForSingleObject(ackEventHandle, WinBase.INFINITE);
        if (ackResult != WinBase.WAIT_OBJECT_0) {
            throw new IOException("Failed to receive acknowledgment. Error code: " + kernel32.GetLastError());
        }
        log.debug("Acknowledgment received.");

        log.debug("sendMessage(message={}) - finished", message);
    }

    public <T extends Serializable, E extends Serializable> E sendMessageWithResponse(Message<T, E> message) throws IOException {
        log.debug("sendMessageWithResponse(message={}) - start", message);

        CompletableFuture<Message<?, ?>> future = new CompletableFuture<>();
        pendingRequests.put(message.getMessageId(), future);

        // Send the message
        sendMessage(message);

        try {
            // Wait for the response
            Message<E, ?> responseMessage = (Message<E, ?>) future.get(); // This will block until the response is received
            return responseMessage.getPayload();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Failed to receive response", e);
        } finally {
            pendingRequests.remove(message.getMessageId());
        }
    }


    public void startReaderThread(MessageListener<?> listener) {
        log.debug("startReaderThread() - start");
        this.messageListener = listener;

        new Thread(() -> {
            Kernel32 kernel32 = Kernel32.INSTANCE;

            HANDLE receiveEventHandle;
            HANDLE sendAckEventHandle;
            Pointer receiveSharedMemory;

            if (isClient) {
                receiveEventHandle = eventServerToClient;
                sendAckEventHandle = eventClientToServerAck; // Corrected
                receiveSharedMemory = sharedMemoryServerToClient;
                log.debug("Client listener thread started.");
            } else {
                receiveEventHandle = eventClientToServer;
                sendAckEventHandle = eventClientToServerAck; // Corrected
                receiveSharedMemory = sharedMemoryClientToServer;
                log.debug("Server listener thread started.");
            }

            HANDLE[] handles = new HANDLE[] { receiveEventHandle, stopEvent };

            while (true) {
                // Wait for message or stop event
                int result = kernel32.WaitForMultipleObjects(
                        handles.length,
                        handles,
                        false,              // Wait for any one object
                        WinBase.INFINITE    // Infinite timeout
                );

                if (result == WinBase.WAIT_FAILED) {
                    log.error("WaitForMultipleObjects failed. Error code: " + kernel32.GetLastError());
                    break;
                }

                int eventIndex = result - WinBase.WAIT_OBJECT_0;

                if (eventIndex == 0) {
                    // Message event was signaled
                    try {
                        byte[] messageBytes;
                        synchronized (receiveSharedMemory) {
                            messageBytes = receiveSharedMemory.getByteArray(0, SHARED_MEMORY_SIZE);
                            // Reset the event for next communication
                            kernel32.ResetEvent(receiveEventHandle);
                        }

                        // Signal acknowledgment to sender
                        log.debug("Signal acknowledgment to sender - sendAckEventHandle");
                        if (!kernel32.SetEvent(sendAckEventHandle)) {
                            throw new IOException("Failed to set acknowledgment event. Error code: " + kernel32.GetLastError());
                        }

                        Message<?, ?> message = deserializeMessage(messageBytes);

                        log.debug("Received message: {}", message);

                        if (message.isResponse()) {
                            // Match response to pending request
                            CompletableFuture<Message<?, ?>> future = pendingRequests.get(message.getMessageId());
                            if (future != null) {
                                future.complete(message);
                            } else {
                                log.warn("Received response for unknown request: {}", message);
                            }
                        } else {
                            // Unsolicited message or request, process via listener
                            messageListener.messageReceived(message);
                        }

                    } catch (IOException e) {
                        log.error("Failed to process message", e);
                    }
                } else if (eventIndex == 1) {
                    // Stop event was signaled
                    log.debug("Stop event signaled, exiting reader thread.");
                    break;
                } else {
                    // Unexpected result
                    log.error("Unexpected result from WaitForMultipleObjects: " + result);
                    break;
                }
            }
            log.debug("Reader thread exiting.");
        }, "MessageBroker-ReaderThread").start();

        log.debug("startReaderThread() - end");
    }



    public void stopReaderThread() {
        log.debug("stopReaderThread() called.");
        if (stopEvent != null) {
            Kernel32.INSTANCE.SetEvent(stopEvent);
        }
    }

    private byte[] serializeMessage(Serializable message) throws IOException {
        log.debug("serializeMessage(message={}) - start", message);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(message);
            byte[] bytes = bos.toByteArray();
            log.debug("serializeMessage(message={}) - finished, size={} bytes", message, bytes.length);
            return bytes;
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Serializable> Message<T, ?> deserializeMessage(byte[] data) throws IOException {
        log.debug("deserializeMessage() - start");
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bis)) {
            Message<T, ?> message = (Message<T, ?>) ois.readObject();
            log.debug("deserializeMessage() - finished, message={}", message);
            return message;
        } catch (ClassNotFoundException e) {
            throw new IOException("Failed to deserialize message", e);
        }
    }

    public void close() {
        log.debug("close() - start");
        stopReaderThread();
        Kernel32 kernel32 = Kernel32.INSTANCE;

        if (sharedMemoryClientToServer != null) {
            kernel32.UnmapViewOfFile(sharedMemoryClientToServer);
            sharedMemoryClientToServer = null;
        }
        if (hMapFileClientToServer != null) {
            kernel32.CloseHandle(hMapFileClientToServer);
            hMapFileClientToServer = null;
        }
        if (sharedMemoryServerToClient != null) {
            kernel32.UnmapViewOfFile(sharedMemoryServerToClient);
            sharedMemoryServerToClient = null;
        }
        if (hMapFileServerToClient != null) {
            kernel32.CloseHandle(hMapFileServerToClient);
            hMapFileServerToClient = null;
        }
        if (eventClientToServer != null) {
            kernel32.CloseHandle(eventClientToServer);
            eventClientToServer = null;
        }
        if (eventServerToClient != null) {
            kernel32.CloseHandle(eventServerToClient);
            eventServerToClient = null;
        }
        if (eventClientToServerAck != null) {
            kernel32.CloseHandle(eventClientToServerAck);
            eventClientToServerAck = null;
        }
        if (eventServerToClientAck != null) {
            kernel32.CloseHandle(eventServerToClientAck);
            eventServerToClientAck = null;
        }
        if (stopEvent != null) {
            kernel32.CloseHandle(stopEvent);
            stopEvent = null;
        }
        
        if (securityAttributes != null && securityAttributes.lpSecurityDescriptor != null) {
            Kernel32.INSTANCE.LocalFree(securityAttributes.lpSecurityDescriptor);
            securityAttributes.lpSecurityDescriptor = null;
        }
        securityAttributes = null;
        log.debug("close() - end");
    }

    private SECURITY_ATTRIBUTES createSecurityAttributes() {
        log.debug("createSecurityAttributes() - start");
        SECURITY_ATTRIBUTES sa = new SECURITY_ATTRIBUTES();
        sa.bInheritHandle = true;
       
        
        // Allocate and initialize the SECURITY_DESCRIPTOR
        int sdSize = 64; // Adjust size as necessary
        WinNT.SECURITY_DESCRIPTOR sd = new WinNT.SECURITY_DESCRIPTOR(sdSize);

        // Write the structure to memory
        sd.write();

        if (!Advapi32.INSTANCE.InitializeSecurityDescriptor(sd, WinNT.SECURITY_DESCRIPTOR_REVISION)) {
            throw new RuntimeException("Failed to initialize security descriptor. Error code: "
                    + Kernel32.INSTANCE.GetLastError());
        }

        // Define security descriptor string to allow access to everyone
        WString sddl = new WString("D:(A;;GA;;;WD)");

        // Convert SDDL string to security descriptor
        PointerByReference pSD = new PointerByReference();
        if (!Advapi32Extended.INSTANCE.ConvertStringSecurityDescriptorToSecurityDescriptorW(
                sddl, SDDL_REVISION_1, pSD, null)) {
            throw new RuntimeException("Failed to create security descriptor. Error code: "
                    + Kernel32.INSTANCE.GetLastError());
        }

        sa.lpSecurityDescriptor = pSD.getValue();
        sa.dwLength = new DWORD(sa.size());
        log.debug("createSecurityAttributes() - end");
        return sa;
    }
    


}
