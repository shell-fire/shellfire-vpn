set SERVICE_NAME=ShellfireVPN2Service
set PR_INSTALL=§§PROCRUNPATH§§
 
REM delete service
"%PR_INSTALL%" //DS//%SERVICE_NAME%

REM delete temp files
rmdir §§TEMP§§sfvpn-chronicle-client-to-service /s /q
rmdir  §§TEMP§§sfvpn-chronicle-service-to-client /s /q
