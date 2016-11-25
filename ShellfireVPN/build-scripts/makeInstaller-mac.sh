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
rsync -av --progress mac/openvpn/* $FOLDER/openvpn --exclude CVS

rsync -av --progress text/license* $FOLDER/openvpn --exclude CVS


echo creating / copying .app bundle
rm -r "../installer/Shellfire VPN.app/"
rsync -av --progress mac/app-template/ "../installer/Shellfire VPN.app" --exclude CVS
rsync -av --progress ../bin/* "../installer/Shellfire VPN.app/Contents/Java/" --exclude CVS

cp -r "../installer/Shellfire VPN.app" "../installer/Shellfire VPN.app.orig"



echo finished

