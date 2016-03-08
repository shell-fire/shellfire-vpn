#!/bin/bash
export JAVAC=/bin/javac.sh
/bin/msgfmt.exe --verbose --java2 -d /cygdrive/a/Flocloud/workspace/ShellfireVPN2/bin -r de.shellfire.vpn.Messages -l de /cygdrive/a/Flocloud/workspace/ShellfireVPN2/deploy-pre/po/trans_de.po
/bin/msgfmt.exe --java2 -d /cygdrive/a/Flocloud/workspace/ShellfireVPN2/bin -r de.shellfire.vpn.Messages -l en /cygdrive/a/Flocloud/workspace/ShellfireVPN2/deploy-pre/po/trans_en.po
/bin/msgfmt.exe --java2 -d /cygdrive/a/Flocloud/workspace/ShellfireVPN2/bin -r de.shellfire.vpn.Messages -l fr /cygdrive/a/Flocloud/workspace/ShellfireVPN2/deploy-pre/po/trans_fr.po
/bin/cp /cygdrive/a/Flocloud/workspace/ShellfireVPN2/bin/de/shellfire/vpn/Messages* /cygdrive/a/Flocloud/workspace/ShellfireVPN2/deploy-pre/po/bin/