#echo "creating .zip for auto updater"
#cd "../Shellfire VPN.app/"
#rm ../ShellfireVPN-2.4.osx.zip
#zip -v -r -q ../ShellfireVPN-2.4.osx.zip *
#cd ../deploy-pre

echo "creating .dmg"
rm ../ShellfireVPN.tmp.dmg
rm ../ShellfireVPN2.dmg

hdiutil create -srcfolder "../Shellfire VPN.app" -volname "ShellfireVPN" -fs HFS+ -fsargs "-c c=64,a=16,e=16" -format UDRW -size 512m ../ShellfireVPN.tmp.dmg
device=$(hdiutil attach -readwrite -noverify -noautoopen "../ShellfireVPN.tmp.dmg" | egrep '^/dev/' | sed 1q | awk '{print $1}')

mkdir /Volumes/ShellfireVPN/.background
cp shellfire-vpn-mac-dmg-background.png /Volumes/ShellfireVPN/.background


echo '
   tell application "Finder"
     tell disk "ShellfireVPN"
           open
           set current view of container window to icon view
           set toolbar visible of container window to false
           set statusbar visible of container window to false
           set the bounds of container window to {400, 100, 968, 406}
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
hdiutil convert "../ShellfireVPN.tmp.dmg" -format UDZO -imagekey zlib-level=9 -o "../ShellfireVPN.dmg"
rm -f ../ShellfireVPN.tmp.dmg 





