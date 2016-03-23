SetCompressor lzma

!include "MUI2.nsh"
!include WinVer.nsh
!include "defs.nsi"
!include "macros.nsh"



;--------------------------------
;Configuration

  ;General

  OutFile "..\..\installer\${PRODUCT_NAME}-${VERSION}-install.exe"

  ShowInstDetails show
  ShowUninstDetails show

  ;Folder selection page
  InstallDir "$PROGRAMFILES\${PRODUCT_NAME}"
  
  ;Remember install folder
  InstallDirRegKey HKCU "Software\${PRODUCT_NAME}" ""

;--------------------------------
;Modern UI Configuration

  Name "${PRODUCT_NAME} ${VERSION}"

  !define MUI_WELCOMEPAGE_TEXT $(ML_MUI_WELCOMEPAGE_TEXT)

  !define MUI_COMPONENTSPAGE_TEXT_TOP $(ML_MUI_COMPONENTSPAGE_TEXT_TOP)

  !define MUI_COMPONENTSPAGE_SMALLDESC
  
  !define MUI_FINISHPAGE_RUN "$INSTDIR\${SFVPN_BIN}"
  !define MUI_FINISHPAGE_NOAUTOCLOSE
  !define MUI_ABORTWARNING
  !define MUI_ICON "..\images\${PRODUCT_ICON}"
  !define MUI_UNICON "..\images\${PRODUCT_ICON}"
  !define MUI_HEADERIMAGE
  !define MUI_HEADERIMAGE_BITMAP "..\images\sf-logo2.bmp"
  !define MUI_UNFINISHPAGE_NOAUTOCLOSE
  !define MUI_LICENSEPAGE_CHECKBOX

  !insertmacro MUI_PAGE_WELCOME 
  
  !insertmacro MUI_PAGE_LICENSE "$(myLicense)"
  !insertmacro CUSTOM_PAGE_JREINFO
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  !insertmacro MUI_PAGE_FINISH
  
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES  
  !insertmacro MUI_UNPAGE_FINISH


;--------------------------------
;Languages
 
  !define MUI_LANGDLL_ALLLANGUAGES
  !insertmacro MUI_LANGUAGE "English"
  !insertmacro MUI_LANGUAGE "German"
  !insertmacro MUI_LANGUAGE "French"
  !insertmacro MUI_RESERVEFILE_LANGDLL 
  
  !include "lang_de.nsi"
  !include "lang_fr.nsi"
  !include "lang_en.nsi"

  LicenseLangString myLicense ${LANG_GERMAN} "..\text\license_de.txt"
  LicenseLangString myLicense ${LANG_ENGLISH} "..\text\license_en.txt"
  LicenseLangString myLicense ${LANG_FRENCH} "..\text\license_fr.txt"
  LicenseData $(myLicense)
  
;--------------------------------


;--------------------------------
;Installer Sections
RequestExecutionLevel admin

Function .onInit
  
  ClearErrors
  SetRegView 32
# Verify that user has admin privs
  UserInfo::GetName
  IfErrors ok
  Pop $R0
  UserInfo::GetAccountType
  Pop $R1
  StrCmp $R1 "Admin" ok
    Messagebox MB_OK "$(ML_ADMIN_PRIV_REQUIRED) [$R0/$R1]"
    Abort
  ok:
  
loop:
   ; find first windows of class "SunAwtFrame" and store handle in $2
   FindWindow $2 "SunAwtFrame" "" 0 $2
   ; If nothing is found skip following check
   IntCmp $2 0 nothingFound
      ; try to retrieve the window title of window $2, store in $3
      System::Call /NOUNLOAD 'user32::GetWindowText(i r2, t .r3, i ${NSIS_MAX_STRLEN})'
      ; Copy the first 14 characters of $3 in $4
      StrCpy $4 $3 14
      ; Compare the 14 characters with "ShellfireVPN 2"
      StrCmp $4 "ShellfireVPN 2" foundVpn loop
foundVpn:
   MessageBox MB_OK $(ML_PROGRAMISRUNNING)
   Abort
nothingFound:  
  
  !insertmacro MUI_LANGDLL_DISPLAY

FunctionEnd

!ifndef SF_SELECTED
!define SF_SELECTED 1
!endif

Section "${PRODUCT_NAME}" SecShellfireVPN

  call DownloadAndInstallJREIfNecessary
  
  SetShellVarContext all
  SetOverwrite on

  IfFileExists "$INSTDIR\ShellfireVPN2.exe" uninstsvc continueafteruninst
  uninstsvc:
  SetOutPath "$INSTDIR"
  ExpandEnvStrings $0 %COMSPEC%
  DetailPrint 'Running Command: "$0" /C "$INSTDIR\ShellfireVPN2.exe" uninstallservice'
  nsExec::ExecToLog '"$0" /C "$INSTDIR\ShellfireVPN2.exe" uninstallservice'
  sleep 4000
  continueafteruninst:
  
  RmDir /r $INSTDIR 
  SetOutPath "$INSTDIR"

  SetOverwrite on
  File /r "${GEN}\*.*" 
  File "..\images\${PRODUCT_ICON}"

  SetOutPath "$INSTDIR\openvpn\"
  
  ; Check if we are running on a 64 bit system.
  System::Call "kernel32::GetCurrentProcess() i .s"
  System::Call "kernel32::IsWow64Process(i s, *i .r0)"
  IntCmp $0 0 openvpn-32bit

; openvpn-64bit:

  DetailPrint "Installing 64-bit openvpn"
  File "..\tools\openvpn\64-bit\"

goto openvpnend

openvpn-32bit:

  DetailPrint "Installing 32-bit openvpn"
  File "..\tools\openvpn\32-bit\"
openvpnend:  

  SetOutPath "$INSTDIR\"

  ; Check if we are running on a 64 bit system.
  System::Call "kernel32::GetCurrentProcess() i .s"
  System::Call "kernel32::IsWow64Process(i s, *i .r0)"
  IntCmp $0 0 procrun-32bit

; procrun-64bit:

  DetailPrint "Installing 64-bit procrun"
  File "..\tools\prunsrv\64-bit\"

goto procrunend

procrun-32bit:

  DetailPrint "Installing 32-bit procrun"
  File "..\tools\prunsrv\32-bit\"
procrunend:  

	SetOutPath "$INSTDIR\nvspbind\"

	${If} ${AtLeastWinVista}
	  DetailPrint "We are running at least win vista"

	  ; Check if we are running on a 64 bit system.
	  System::Call "kernel32::GetCurrentProcess() i .s"
	  System::Call "kernel32::IsWow64Process(i s, *i .r0)"
	  IntCmp $0 0 nvspbind-32bit

	; nvspbind-64bit:

	  DetailPrint "Installing 64-bit nvspbind"
	  File "..\tools\nvspbind\64-bit\"

	goto nvspbindend

	nvspbind-32bit:

	  DetailPrint "Installing 32-bit nvspbind"
	  File "..\tools\nvspbind\32-bit\"
	nvspbindend:  
	  
	${Else}
	  DetailPrint "We are running XP - installing nvspbind for windows xp"
	
	  File "..\tools\nvspbind\xp\"
	${EndIf}

  SetOutPath "$INSTDIR"  
  DetailPrint "Installing Service"
  ExpandEnvStrings $0 %COMSPEC%
  DetailPrint 'Running Command: "$0" /C "$INSTDIR\ShellfireVPN2.exe" installservice"' 
  nsExec::ExecToLog '"$0" /C "$INSTDIR\ShellfireVPN2.exe" installservice'

  Var /GLOBAL langstr 
  
  StrCmp $language "1031" 0 lang_node
  StrCpy $langstr "de"
  Goto langdone
  lang_node:
  StrCmp $language "1033" 0 lang_noen
  StrCpy $langstr "en"
  Goto langdone
  lang_noen:
  StrCpy $langstr "fr"
  langdone:

  SetShellVarContext Current
  CreateDirectory "$APPDATA\ShellfireVpn"
  FileOpen $4 "$APPDATA\ShellfireVpn\ShellfireVpn.properties" w
  FileWrite $4 "# Initial properties file written from nsis$\r$\n"
  FileWrite $4 "InterfaceLanguage=$langstr$\r$\n"
  FileWrite $4 "instdir=$INSTDIR$\r$\n"
  
  FileClose $4
  
  WriteRegStr HKCU "Software\JavaSoft\Prefs\de\shellfire\vpn\gui" "/Interface/Language" "$langstr"
  WriteRegStr HKCU "Software\JavaSoft\Prefs\de\shellfire\vpn\gui" "instdir" "$INSTDIR\"
  
  WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\ShellfireVPN2.exe" "" "$INSTDIR\ShellfireVPN2.exe"
  WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\ShellfireVPN2.exe" "Path" "$INSTDIR\" 
  
SectionEnd

Section $(ML_SecTAP) SecTAP

	SetOverwrite on
	SetOutPath "$INSTDIR"
	
	${If} ${AtLeastWinVista}
	  DetailPrint "We are running at least win vista"

	  File /oname=tap-windows.exe "..\tools\tap\tap-windows-vista-or-later.exe"
	
	${Else}
	  DetailPrint "We are running XP"
	
	  File /oname=tap-windows.exe "..\tools\tap\tap-windows-xp.exe"
	${EndIf}

	DetailPrint "Installing TAP (may need confirmation)..."
	nsExec::ExecToLog '"$INSTDIR\tap-windows.exe" /S /SELECT_UTILITIES=1'
	Pop $R0 # return value/error/timeout

	WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${PACKAGE_NAME}" "tap" "installed"	
SectionEnd


Section $(ML_SecAddShortcuts) SecAddShortcuts
  SetOutPath "$INSTDIR"
  RmDir /r "$SMPROGRAMS\${PRODUCT_NAME}" 
  SetOverwrite on
  CreateDirectory "$SMPROGRAMS\${PRODUCT_NAME}"
  WriteINIStr "$SMPROGRAMS\${PRODUCT_NAME}\Shellfire Web.url" "InternetShortcut" "URL" "http://www.shellfire.de"
  
  CreateShortCut "$SMPROGRAMS\${PRODUCT_NAME}\$(ML_UNINSTALLPROGRAM).lnk" "$INSTDIR\Uninstall.exe"

  CreateShortCut "$SMPROGRAMS\${PRODUCT_NAME}\${PRODUCT_NAME}.lnk" "$INSTDIR\${SFVPN_BIN}" ""
SectionEnd

Section  $(ML_SecAddDesktop) SecAddDesktop
  SetOutPath "$INSTDIR"
  SetOverwrite on
  CreateShortcut "$DESKTOP\${PRODUCT_NAME}.lnk" "$INSTDIR\${SFVPN_BIN}"
SectionEnd


;--------------------
;Post-install section

Section -post

  SetOverwrite on

  ;
  ; install/upgrade TAP driver if selected, using tapinstall.exe
  ;
  SectionGetFlags ${SecTAP} $R0
  IntOp $R0 $R0 & ${SF_SELECTED}
  IntCmp $R0 ${SF_SELECTED} "" notap notap
    ; TAP install/update was selected.
    ; Should we install or update?
    ; If tapinstall error occurred, $5 will
    ; be nonzero.
    IntOp $5 0 & 0
	${If} ${AtLeastWinVista}
	  nsExec::ExecToStack '"$INSTDIR\openvpn\tapinstall.exe" hwids ${TAP}'
	${Else}
		nsExec::ExecToStack '"$INSTDIR\openvpn\devcon.exe" hwids ${TAP}'
	${Endif}
    
    Pop $R0 # return value/error/timeout
    IntOp $5 $5 | $R0
    DetailPrint "tapinstall hwids returned: $R0"

    ; If tapinstall output string contains "${TAP}" we assume
    ; that TAP device has been previously installed,
    ; therefore we will update, not install.
    Push "${TAP}"
    Call StrStr
    Pop $R0

    IntCmp $5 0 "" tapinstall_check_error tapinstall_check_error
    IntCmp $R0 -1 tapinstall

 ;tapupdate:
    DetailPrint "TAP UPDATE"
	${If} ${AtLeastWinVista}
	  nsExec::ExecToLog '"$INSTDIR\openvpn\tapinstall.exe" update "$INSTDIR\openvpn\OemVista.inf" ${TAP}'
	${Else}
	  nsExec::ExecToLog '"$INSTDIR\openvpn\devcon.exe" update "$INSTDIR\openvpn\OemWin2k.inf" ${TAP}'
	${Endif}
	
	
    
    Pop $R0 # return value/error/timeout
    Call CheckReboot
    IntOp $5 $5 | $R0
    DetailPrint "tapinstall update returned: $R0"
    Goto tapinstall_check_error

 tapinstall:
    DetailPrint "TAP REMOVE OLD TAP"

	${If} ${AtLeastWinVista}
	  nsExec::ExecToLog '"$INSTDIR\openvpn\tapinstall.exe" remove TAP0801'
	${Else}
	  nsExec::ExecToLog '"$INSTDIR\openvpn\devcon.exe" remove TAP0801'
	${Endif}
    
    Pop $R0 # return value/error/timeout
    DetailPrint "tapinstall remove TAP0801 returned: $R0"

    DetailPrint "TAP INSTALL (${TAP})"
	${If} ${AtLeastWinVista}
	  nsExec::ExecToLog '"$INSTDIR\openvpn\tapinstall.exe" install "$INSTDIR\openvpn\OemVista.inf" ${TAP}'
	${Else}
	  nsExec::ExecToLog '"$INSTDIR\openvpn\devcon.exe" install "$INSTDIR\openvpn\OemWin2k.inf" ${TAP}'
	${Endif}

    
    Pop $R0 # return value/error/timeout
    Call CheckReboot
    IntOp $5 $5 | $R0
    DetailPrint "tapinstall install returned: $R0"

 tapinstall_check_error:
    DetailPrint "tapinstall cumulative status: $5"
    IntCmp $5 0 notap
    MessageBox MB_OK $(ML_TAPINSTALLERROR)

 notap:

  ; Store install folder in registry
  WriteRegStr HKLM SOFTWARE\${PRODUCT_NAME} "" $INSTDIR


  ; Create start menu folders

 noshortcuts:
  ; Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

  ; Show up in Add/Remove programs
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "DisplayName" "${PRODUCT_NAME} ${VERSION}"
  WriteRegExpandStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "UninstallString" "$INSTDIR\Uninstall.exe"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "DisplayIcon" "$INSTDIR\${PRODUCT_ICON}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" "DisplayVersion" "${VERSION}"

  ; write install log to install directory
  StrCpy $0 "$INSTDIR\install.log"
  Push $0
  Call DumpLog
  
SectionEnd

;--------------------------------
;Descriptions

!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SecShellfireVPN} $(ML_DESC_SecShellfireVPN)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecTAP} $(ML_DESC_SecTAP)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecAddShortcuts} $(ML_DESC_SecAddShortcuts)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecAddDesktop} $(ML_DESC_SecAddDesktop)
!insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Function un.onInit
  !insertmacro MUI_UNGETLANGUAGE
  ClearErrors
  SetRegView 32
  UserInfo::GetName
  IfErrors ok
  Pop $R0
  UserInfo::GetAccountType
  Pop $R1
  StrCmp $R1 "Admin" ok
    Messagebox MB_OK "$(ML_ADMIN_PRIV_REQUIRED_UNINST) [$R0/$R1]"
    Abort
  ok:
  
loop:
   ; find first windows of class "SunAwtFrame" and store handle in $2
   FindWindow $2 "SunAwtFrame" "" 0 $2
   ; If nothing is found skip following check
   IntCmp $2 0 nothingFound
      ; try to retrieve the window title of window $2, store in $3
      System::Call /NOUNLOAD 'user32::GetWindowText(i r2, t .r3, i ${NSIS_MAX_STRLEN})'
      ; Copy the first 6 characters of $3 in $4
      StrCpy $4 $3 14
      ; Compare the 14 characters with "ShellfireVPN 2"
      StrCmp $4 "ShellfireVPN 2" foundVpn loop
foundVpn:
   MessageBox MB_OK $(ML_PROGRAMISRUNNING)
   Abort
nothingFound:  
  
  
FunctionEnd

Section "Uninstall"

  DetailPrint "TAP REMOVE"
  
	ReadRegStr $R0 HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${PACKAGE_NAME}" "tap"
	${If} $R0 == "installed"
		ReadRegStr $R0 HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\TAP-Windows" "UninstallString"
		${If} $R0 != ""
			DetailPrint "Uninstalling TAP..."
			nsExec::ExecToLog '"$R0" /S'
			Pop $R0 # return value/error/timeout
		${EndIf}
	${EndIf}  
  

  SetOutPath "$INSTDIR"

  ExpandEnvStrings $0 %COMSPEC%
  DetailPrint 'Running Command: "$0" /C "$INSTDIR\ShellfireVPN2.exe" uninstallservice'
  nsExec::ExecToLog '"$0" /C "$INSTDIR\ShellfireVPN2.exe" uninstallservice'
  sleep 4000
    
  ExpandEnvStrings $0 %APPDATA%
  RMDir /r "$0\ShellfireVPN"


  SetShellVarContext all
  Delete "$DESKTOP\${PRODUCT_NAME}.lnk"
  RMDir /r "$SMPROGRAMS\${PRODUCT_NAME}"
  RMDir /r "$INSTDIR"

  DeleteRegKey HKCR "${PRODUCT_NAME}File"
  DeleteRegKey HKLM "SOFTWARE\${PRODUCT_NAME}"
  DeleteRegKey HKCU "SOFTWARE\${PRODUCT_NAME}"

  DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
  DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\ShellfireVPN2.exe"


  DeleteRegValue HKCU "Software\Microsoft\Windows\CurrentVersion\Run" ${PRODUCT_NAME}
  
  DeleteRegKey HKCU "Software\JavaSoft\Prefs\de\shellfire\vpn"
  DeleteRegKey /ifempty HKCU "Software\JavaSoft\Prefs\de\shellfire"
  DeleteRegKey /ifempty HKCU "Software\JavaSoft\Prefs\de"
  DeleteRegKey /ifempty HKCU "Software\JavaSoft\Prefs\"
  
    
SectionEnd
