package de.shellfire.vpn.messaging;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.Function;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public interface Advapi32Extended extends Library {
    Advapi32Extended INSTANCE = Native.load("Advapi32", Advapi32Extended.class);

    
    boolean ConvertStringSecurityDescriptorToSecurityDescriptorW(
            WString StringSecurityDescriptor,
            int StringSDRevision,
            PointerByReference pSecurityDescriptor,
            IntByReference SecurityDescriptorSize
    );
}
