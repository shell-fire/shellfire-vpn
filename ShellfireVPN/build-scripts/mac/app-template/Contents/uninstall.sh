# Uninstall the ShellfireVPNService
# needs to be run as su
#
# Note: To delete all configuration files, go to your Documents folder and delete the ShellfireVPN folder there
# Then you are good to go and can simply remove ShellfireVPN from your Applications folder
#
launchctl stop wrapper.ShellfireVPN2Service
launchctl unload /Library/LaunchDaemons/wrapper.ShellfireVPN2Service.plist
launchctl stop ShellfireVPNService
launchctl unload /Library/LaunchDaemons/ShellfireVPNService.plist
launchctl stop ShellfireVPN2Service
launchctl unload /Library/LaunchDaemons/ShellfireVPN2Service.plist

rm /Library/LaunchDaemons/wrapper.ShellfireVPN2Service.plist
rm /Library/LaunchDaemons/ShellfireVPNService.plist
rm /Library/LaunchDaemons/ShellfireVPN2Service.plist
rm /etc/shellfirevpn.conf

rm -rf /tmp/shellfire-vpn