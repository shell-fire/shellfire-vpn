package main

import (
	"fmt"
	"sync"
	"golang.org/x/sys/windows"
	"golang.zx2c4.com/wireguard/tun/wintun"
)


type NativeTun struct {
	wt        *wintun.Adapter
	handle    windows.Handle
	session   wintun.Session
	readWait  windows.Handle
	running   sync.WaitGroup
	closeOnce sync.Once
	close     int32
	forcedMTU int
}



// InstallWintunDriver loads wintun.dll to force it to install its
// kernel driver, which can take a long time (especially on Windows
// 7). We want to do it early (at install time), so the service starts
// up quicker later, to avoid the GUI timing out trying to connect to
// it.
func InstallWintunDriver() error {

	pool, err := wintun.MakePool("WireGuard")
	if err != nil {
		return fmt.Errorf("MakePool: %w", err)
	}
	adapter, _, err := pool.CreateAdapter("Shellfire VPN Installer", nil)
	if err != nil {
		return fmt.Errorf("CreateAdapter: %w", err)
	}
	_, err = adapter.Delete(false)
	if err != nil {
		return fmt.Errorf("Adapter.Delete: %w", err)
	}
	return nil
}


// Main function
func main() {
  InstallWintunDriver()
  
}