@echo off

set folder=..\bin
set version=2.7


rem echo update language files
rem c:\cygwin\bin\bash.exe -li /bin/msgfmt.sh

echo create some folders
rem rmdir %folder% /S /Q
rem mkdir %folder%
rem rmdir ..\deploy_%version% /S /Q

echo make fat jars
call ant\bin\ant.bat -buildfile makeUpdater.ant
rem call ant\bin\ant.bat -buildfile makeMainDat.ant
call ant\bin\ant.bat -buildfile makeService.ant
echo make loader .exe
"C:\Program Files (x86)\Launch4j\launch4jc.exe" makeLoaderExe_%version%.xml
echo make main dat .exe
"C:\Program Files (x86)\Launch4j\launch4jc.exe" makeMainDatExe_%version%.xml
echo move .exe to .dat
move %folder%\ShellfireVpn2dat.exe %folder%\ShellfireVpn2.dat
echo move service jar
move ShellfireVPN2Service.jar %folder%\ShellfireVPN2Service.dat
echo copy library folders
pause
xcopy lib %folder%\lib\ /E /EXCLUDE:deploy_exclude.txt
xcopy lib %folder%\libloader\ /E /EXCLUDE:deploy_exclude.txt
echo copy jre
xcopy jre8 %folder%\jre8\ /E /EXCLUDE:deploy_exclude.txt

echo copy openvpn
xcopy openvpn %folder%\openvpn\ /E /EXCLUDE:deploy_exclude.txt

xcopy ICE_JNIRegistry.dll %folder%\
xcopy elevate.exe %folder%\

move /Y %folder% ..\deploy_%version%
echo creating installer...
"c:\Program Files (x86)\NSIS\makensis.exe" /DVERSION=%version% nsis/sfvpn2.nsi 

echo finished
pause