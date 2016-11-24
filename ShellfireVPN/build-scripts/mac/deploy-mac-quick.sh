#!/bin/bash

FOLDER="../deploy-mac"
VERSION="2.0"

echo "folder: $FOLDER"


echo "update language files"
cd po
./msgfmt-mac.sh 
cd ..

#echo create some folders
#rm -r $FOLDER
#mkdir $FOLDER
#rm -r ..\deploy-mac_$VERSION


echo make fat jars
#ant -buildfile makeUpdater-mac.ant
ant -buildfile makeMainDat-mac.ant
ant -buildfile makeService-mac.ant

echo "copy fat jars"
cp ShellfireVPN2.jar $FOLDER
cp ShellfireVPN2Service.jar $FOLDER/ShellfireVPN2Service.dat
#cp ShellfireVPN2dat.jar $FOLDER/ShellfireVPN2.dat

# echo make yajsw
#cd yajsw/build/gradle/
#./gradlew.sh
#cd ../../..
#cp yajsw/build/gradle/wrapper/build/libs/wrapper.jar libs


#echo copy library folders
#rsync -av --progress lib/* $FOLDER/lib --exclude CVS
#rsync -av --progress lib/* $FOLDER/libloader --exclude CVS


#echo copy openvpn and kext
#rsync -av --progress openvpn-mac/* $FOLDER/openvpn --exclude CVS
rsync -av --progress text/license* $FOLDER/openvpn --exclude CVS



rm -r "../Shellfire VPN.app/"
rsync -av --progress macos-app-template/ "../Shellfire VPN.app" --exclude CVS
rsync -av --progress ../deploy-mac/* "../Shellfire VPN.app/Contents/Java/" --exclude CVS


echo "creating .zip for auto updater"
cd "../Shellfire VPN.app/"
#zip -v -r -q ../ShellfireVPN-2.4.osx.zip *
cd ../deploy-pre

echo "creating .dmg"
rm ../ShellfireVPN.tmp.dmg
rm ../ShellfireVPN2.dmg
#hdiutil create -size 256m -fs HFS+ -volname "ShellfireVPN" ../ShellfireVPN.tmp.dmg
#hdiutil attach ../ShellfireVPN.tmp.dmg
#DEVS=$(hdiutil attach ../ShellfireVPN.tmp.dmg | cut -f 1)
#DEV=$(echo $DEVS | cut -f 1 -d ' ')
#echo "Mounted on $DEV"
#cp -R "../Shellfire VPN.app" /Volumes/ShellfireVPN
#hdiutil detach $DEV
#hdiutil convert ../ShellfireVPN.tmp.dmg -format UDZO -o ../ShellfireVPN2.dmg
#rm ../ShellfireVPN.tmp.dmg





echo finished

