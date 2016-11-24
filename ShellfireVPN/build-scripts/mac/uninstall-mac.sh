# needs to be run as su
launchctl stop wrapper.ShellfireVPN2Service
launchctl unload /Library/LaunchDaemons/wrapper.ShellfireVPN2Service.plist
launchctl stop ShellfireVPNService
launchctl unload /Library/LaunchDaemons/ShellfireVPNService.plist


rm /Library/LaunchDaemons/wrapper.ShellfireVPN2Service.plist
rm /Library/LaunchDaemons/ShellfireVPNService.plist

#killall -9 java
#rm -r ../ShellfireVPN2.app
