#!/bin/bash
FOLDER="../deploy-mac"
VERSION="2.0"

echo "folder: $FOLDER"


echo "update language files"
cd po
./msgfmt-mac.sh 
cd ..

echo create some folders
rm -r $FOLDER
mkdir $FOLDER
rm -r ..\deploy-mac_$VERSION


echo make fat jars
#ant -buildfile makeUpdater-mac.ant
ant -buildfile makeMainDat-mac.ant
ant -buildfile makeService-mac.ant

echo "copy fat jars"
cp ShellfireVPN2.jar $FOLDER
cp ShellfireVPN2Service.jar $FOLDER/ShellfireVPN2Service.dat
#cp ShellfireVPN2dat.jar $FOLDER/ShellfireVPN2.dat



echo copy library folders
rsync -av --progress lib/* $FOLDER/lib --exclude CVS
#rsync -av --progress lib/* $FOLDER/libloader --exclude CVS


echo copy openvpn and kext
rsync -av --progress openvpn-mac/* $FOLDER/openvpn --exclude CVS

rsync -av --progress text/license* $FOLDER/openvpn --exclude CVS


echo creating / copying .app bundle
rm -r "../Shellfire VPN.app/"
rsync -av --progress macos-app-template/ "../Shellfire VPN.app" --exclude CVS
rsync -av --progress ../deploy-mac/* "../Shellfire VPN.app/Contents/Java/" --exclude CVS

cp -r "../Shellfire VPN.app" "../Shellfire VPN.app.orig"



echo finished

