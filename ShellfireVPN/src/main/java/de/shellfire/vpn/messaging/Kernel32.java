package de.shellfire.vpn.messaging;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.WinNT.*;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface Kernel32 extends StdCallLibrary {
    Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class, W32APIOptions.DEFAULT_OPTIONS);

    HANDLE CreateFileMapping(HANDLE hFile, WinBase.SECURITY_ATTRIBUTES lpFileMappingAttributes,
                             int flProtect, int dwMaximumSizeHigh, int dwMaximumSizeLow, String lpName);

    Pointer MapViewOfFile(HANDLE hFileMappingObject, int dwDesiredAccess,
                          int dwFileOffsetHigh, int dwFileOffsetLow, int dwNumberOfBytesToMap);

    boolean UnmapViewOfFile(Pointer lpBaseAddress);

    boolean CloseHandle(HANDLE hObject);

    HANDLE CreateEvent(WinBase.SECURITY_ATTRIBUTES lpEventAttributes, boolean bManualReset,
                       boolean bInitialState, String lpName);

    boolean SetEvent(HANDLE hEvent);

    int WaitForSingleObject(HANDLE hHandle, int dwMilliseconds);

    int GetLastError();
}
