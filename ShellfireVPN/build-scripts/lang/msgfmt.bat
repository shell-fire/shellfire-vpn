c:\gettext\bin\msgfmt.exe --verbose "C:\nextcloud\MYLIBR~1\workspace\shellfire-vpn4\ShellfireVPN\build-scripts\lang\trans_en-de.po" --java2 --source -d "c:\nextcloud\MYLIBR~1\workspace\shellfire-vpn4\ShellfireVPN\src\main\java\de\shellfire\vpn" -r de.shellfire.vpn.Messages -l de
copy /Y c:\nextcloud\MYLIBR~1\workspace\shellfire-vpn4\ShellfireVPN\src\main\java\de\shellfire\vpn\de\shellfire\vpn\*.java c:\nextcloud\MYLIBR~1\workspace\shellfire-vpn4\ShellfireVPN\src\main\java\de\shellfire\vpn
rmdir /S /Q c:\nextcloud\MYLIBR~1\workspace\shellfire-vpn4\ShellfireVPN\src\main\java\de\shellfire\vpn\de

c:\gettext\bin\msgfmt.exe --verbose "C:\nextcloud\MYLIBR~1\workspace\shellfire-vpn4\ShellfireVPN\build-scripts\lang\trans_en-fr.po" --java2 --source -d "c:\nextcloud\MYLIBR~1\workspace\shellfire-vpn4\ShellfireVPN\src\main\java\de\shellfire\vpn" -r de.shellfire.vpn.Messages -l fr
copy /Y c:\nextcloud\MYLIBR~1\workspace\shellfire-vpn4\ShellfireVPN\src\main\java\de\shellfire\vpn\de\shellfire\vpn\*.java c:\nextcloud\MYLIBR~1\workspace\shellfire-vpn4\ShellfireVPN\src\main\java\de\shellfire\vpn
rmdir /S /Q c:\nextcloud\MYLIBR~1\workspace\shellfire-vpn4\ShellfireVPN\src\main\java\de\shellfire\vpn\de

c:\gettext\bin\msgfmt.exe --verbose "C:\nextcloud\MYLIBR~1\workspace\shellfire-vpn4\ShellfireVPN\build-scripts\lang\trans_en-en.po" --java2 --source -d "c:\nextcloud\MYLIBR~1\workspace\shellfire-vpn4\ShellfireVPN\src\main\java\de\shellfire\vpn" -r de.shellfire.vpn.Messages -l en
copy /Y c:\nextcloud\MYLIBR~1\workspace\shellfire-vpn4\ShellfireVPN\src\main\java\de\shellfire\vpn\de\shellfire\vpn\*.java c:\nextcloud\MYLIBR~1\workspace\shellfire-vpn4\ShellfireVPN\src\main\java\de\shellfire\vpn
rmdir /S /Q c:\nextcloud\MYLIBR~1\workspace\shellfire-vpn4\ShellfireVPN\src\main\java\de\shellfire\vpn\de


rem pause
