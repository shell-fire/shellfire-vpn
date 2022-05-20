@echo off

set folder=..\bin
set version=3.2


rem echo update language files
call lang\msgfmt.bat
pause

rem echo create some folders
rmdir %folder% /S /Q
mkdir %folder%
mkdir %folder%\servers

echo make fat jars
call ant\bin\ant.bat -buildfile makeService.ant
call ant\bin\ant.bat -buildfile makeMainDat.ant
call ant\bin\ant.bat -buildfile makeUpdater.ant
pause

echo make updater to ShellfireVPN.exe
"C:\Program Files (x86)\Launch4j\launch4jc.exe" makeLoaderExe_%version%.xml

echo make mainDat - exe file (named .dat)
"C:\Program Files (x86)\Launch4j\launch4jc.exe" makeMainDatExe_%version%.xml

move %folder%\ShellfireVPN2Dat.exe %folder%\ShellfireVPN2.dat

echo "deleting jars"
del %folder%\ShellfireVPN2.jar
del %folder%\ShellfireVPN2Dat.jar

echo "copying tools"
rem NOT: xcopy tools %folder%\tools\ /S /E
xcopy InstallServiceTemplate.txt %folder%\
xcopy UninstallServiceTemplate.txt %folder%\
xcopy shellfire.keystore %folder%\
xcopy servers %folder%\servers\

echo creating installer...
"c:\Program Files (x86)\NSIS\makensis.exe" /DVERSION=%version% nsis/sfvpn2.nsi 

echo finished
pause