# define the TAP version
!define PRODUCT_TAP_ID "tap0901"
!define PRODUCT_TAP_WIN32_MIN_MAJOR "9"
!define PRODUCT_TAP_WIN32_MIN_MINOR "9"

# Branding
!define PRODUCT_NAME "ShellfireVPN"
!define SFVPN_BIN "ShellfireVPN2.exe"

# tapinstall.exe source code.
# Not needed if DRVBINSRC is defined
# (or if using pre-built mode).
!define TISRC "../tapinstall"



# TAP adapter icon -- visible=0x81 or hidden=0x89
!define PRODUCT_TAP_CHARACTERISTICS 0x81

# DDK Version.
# DDK distribution is assumed to be in C:\WINDDK\${DDKVER}
!define DDKVER 6001.18002
!define DDKVER_MAJOR 6001


# remember installer language settings
!define MUI_LANGDLL_REGISTRY_ROOT "HKCU" 
!define MUI_LANGDLL_REGISTRY_KEY "SOFTWARE\${PRODUCT_NAME}" 
!define MUI_LANGDLL_REGISTRY_VALUENAME "Installer Language"



; assert makesnsis.exe is called with /DVERSION=2.7
!define GEN "..\..\bin"

!define PRODUCT_ICON "icon-big.ico"

!define TAP "${PRODUCT_TAP_ID}"
!define TAPDRV "${TAP}.sys"

 !define JRE_VERSION "1.17"
 



 
 
 
 !include "JREDyna_inetc.nsh"
 
 