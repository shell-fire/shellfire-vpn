Florian Gattung / flogat, 18 June 2021:

Because of KB2921916, which is discussed between Wireguard and Tailscale developers here:
https://github.com/tailscale/tailscale/issues/1051

As the piece of code from Tailscale developers already utilizes the go implementation wireguard, which can use the wintun.dll, 
I decided after some research to also build this quick tool in go.

Only needed on Win7, but likely helps a little bit with first time wireguard tunnel startup also on Win10.

For x64, compile on my normal machine
For x86, compile on win7 32 bit VM

I checked some other options to force the wintun driver install and they all appeared more complex (e.g. creation of a JNA wrapper for the dll).

The compiled forcewintun.exe are installed via the build-scripts/tools/wireguard folders and called during installation.


Question: Why not force KB2921916, as per Wireguard's recommendation?
-> Detection of KB2921916 seems to be quite tricky and I agree with Tailscale devs that I don't want to force it.
-> Summary of Wireguard's dev, that this might be a non-issue going forward due to the lower and lower marketshare of win7 makes sense to me.
-> When users contact support, they can always re-install and will be good.

Optimisation idea, in case this is an ongoing problem in the future:
- Detect from wireguard log that specific failure and launch an elevation process to forcewintun.exe from the gui.