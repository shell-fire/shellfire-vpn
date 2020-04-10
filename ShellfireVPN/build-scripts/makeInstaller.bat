@echo off

set folder=..\bin
set version=2.7


rem echo update language files
call c:\cygwin\bin\bash.exe -li /bin/msgfmt.sh

rem echo create some folders
rmdir %folder% /S /Q
mkdir %folder%

echo make fat jars
call ant\bin\ant.bat -buildfile makeService.ant
call ant\bin\ant.bat -buildfile makeMainDat.ant
call ant\bin\ant.bat -buildfile makeUpdater.ant


echo make updater to ShellfireVPN.exe
"C:\Program Files (x86)\Launch4j\launch4jc.exe" makeLoaderExe_%version%.xml

echo make mainDat - exe file (named .dat)
"C:\Program Files (x86)\Launch4j\launch4jc.exe" makeMainDatExe_%version%.xml

move %folder%\ShellfireVPN2Dat.exe %folder%\ShellfireVPN2.dat

echo "deleting jars"
rm %folder%\ShellfireVPN2.jar
rm %folder%\ShellfireVPN2Dat.jar

echo "copying tools"
xcopy tools %folder%\tools\ /S /E
xcopy InstallServiceTemplate.txt %folder%\
xcopy UninstallServiceTemplate.txt %folder%\
xcopy shellfire.keystore %folder%\

echo creating installer...
"c:\Program Files (x86)\NSIS\makensis.exe" /DVERSION=%version% nsis/sfvpn2.nsi 

echo finished
pause