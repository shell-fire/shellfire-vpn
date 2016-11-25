#!/bin/bash
FOLDER="../bin"
VERSION="2.7"

echo "folder: $FOLDER"



echo create some folders
rm -r $FOLDER
mkdir $FOLDER


echo "update language files"
ditto lang/bin/Mess* ../target/classes/de/shellfire/vpn/

export PATH=/usr/local/apache-ant/bin:"$PATH"
echo make fat jars
#ant -buildfile makeUpdater-mac.ant
ant -buildfile makeService-mac.ant
ant -buildfile makeMainDat-mac.ant

echo copy openvpn and kext
rsync -av --progress mac/openvpn/* $FOLDER/openvpn --exclude .git

rsync -av --progress text/license* $FOLDER/openvpn --exclude .git


echo creating / copying .app bundle
rm -rf "../installer/Shellfire VPN.app/"
rsync -av --progress mac/app-template/ "../installer/Shellfire VPN.app" --exclude .git
rsync -av --progress ../bin/* "../installer/Shellfire VPN.app/Contents/Java/" --exclude .git

ditto "../installer/Shellfire VPN.app" "../installer/Shellfire VPN.app.orig"


echo "creating .zip for auto updater"
#cd "../installer/Shellfire VPN.app/"
#rm ../ShellfireVPN-2.7.osx.zip
#zip -v -r -q ../ShellfireVPN-2.7.osx.zip *
#cd ../../build-scripts

echo "creating .dmg"
rm ../installer/ShellfireVPN.tmp.dmg
rm ../installer/ShellfireVPN.dmg

hdiutil create -srcfolder "../installer/Shellfire VPN.app" -volname "ShellfireVPN" -fs HFS+ -fsargs "-c c=64,a=16,e=16" -format UDRW -size 512m ../installer/ShellfireVPN.tmp.dmg
device=$(hdiutil attach -readwrite -noverify -noautoopen "../installer/ShellfireVPN.tmp.dmg" | egrep '^/dev/' | sed 1q | awk '{print $1}')

mkdir /Volumes/ShellfireVPN/.background
cp mac/shellfire-vpn-mac-dmg-background.png /Volumes/ShellfireVPN/.background


echo '
   tell application "Finder"
     tell disk "ShellfireVPN"
           open
           set current view of container window to icon view
           set toolbar visible of container window to false
           set statusbar visible of container window to false
           set the bounds of container window to {400, 100, 968, 445}
           set theViewOptions to the icon view options of container window
           set arrangement of theViewOptions to not arranged
           set icon size of theViewOptions to 112
           set background picture of theViewOptions to file ".background:shellfire-vpn-mac-dmg-background.png"
           make new alias file at container window to POSIX file "/Applications" with properties {name:"Applications"}
           set position of item "Shellfire VPN.app" of container window to {120, 215}
           set position of item "Applications" of container window to {430, 215}
           close
           open
           update without registering applications
           delay 5
           close
     end tell
   end tell
' | osascript


chmod -Rf go-w /Volumes/ShellfireVPN
sync
sync
hdiutil detach ${device}
hdiutil convert "../installer/ShellfireVPN.tmp.dmg" -format UDZO -imagekey zlib-level=9 -o "../installer/ShellfireVPN.dmg"
rm -f ../installer/ShellfireVPN.tmp.dmg 





