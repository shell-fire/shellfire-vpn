set SERVICE_NAME=ShellfireVPN2Service
set PR_DESCRIPTION=ShellfireVPN2Service
set PR_INSTALL=§§PROCRUNPATH§§
set PR_SERVICEUSER=LocalSystem

REM Service log configuration
set PR_LOGPREFIX=%SERVICE_NAME%
set PR_LOGPATH=§§TEMP§§
set PR_STDOUTPUT=§§LOGFILE_STD§§
set PR_STDERROR=§§LOGFILE_ERR§§
set PR_LOGLEVEL=Trace
set PR_LOGJNIMESSAGES=1
 
REM Path to java installation
set PR_JVM=§§JVM_DLL§§
set PR_CLASSPATH=§§SHELLFIREVPNSERVICEDAT§§
 
REM Startup configuration
set PR_STARTUP=auto
set PR_STARTMODE=jvm
set PR_STARTCLASS=de.shellfire.vpn.service.Service
set PR_STARTMETHOD=start
set PR_STARTIMAGE=§§INSTALLDIR§§\icon-big.ico

REM Shutdown configuration
set PR_STOPMODE=jvm
set PR_STOPCLASS=de.shellfire.vpn.service.Service
set PR_STOPMETHOD=stop
set PR_STOPIMAGE=§§INSTALLDIR§§\icon-big.ico

 
REM JVM configuration
set PR_JVMMS=256
set PR_JVMMX=1024
set PR_JVMSS=4000
set PR_JVMOPTIONS=
set PR_JVMOPTIONS9=--add-opens=java.base/java.lang=ALL-UNNAMED;--add-opens=java.base/sun.nio.ch=ALL-UNNAMED;--module-path=.\\lib\\javafx\\lib;--add-modules=javafx.swing,javafx.graphics,javafx.fxml,javafx.media,javafx.web;--add-reads=javafx.graphics=ALL-UNNAMED;--add-opens=javafx.controls/com.sun.javafx.charts=ALL-UNNAMED;--add-opens=javafx.graphics/com.sun.javafx.iio=ALL-UNNAMED;--add-opens=javafx.graphics/com.sun.javafx.iio.common=ALL-UNNAMED;--add-opens=javafx.graphics/com.sun.javafx.css=ALL-UNNAMED;--add-opens=javafx.base/com.sun.javafx.runtime=ALL-UNNAMED;--add-exports=java.base/jdk.internal.util=chronicle.bytes;--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED;--add-exports=java.base/sun.nio.ch=ALL-UNNAMED;--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED;--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED;--add-opens=jdk.compiler/com.sun.tools.javac=ALL-UNNAMED;--add-opens=java.base/java.lang.reflect=ALL-UNNAMED;--add-opens=java.base/java.io=ALL-UNNAMED;--add-opens=java.base/java.util=ALL-UNNAMED;-Dde.shellfire.vpn.runtype=Service
set PR_STARTPARAMS=

REM Uninstall service (might fail if service not installed before)
"%PR_INSTALL%" //DS//%SERVICE_NAME%
REM Install service
"%PR_INSTALL%" //IS//%SERVICE_NAME%
timeout 2
REM start service
"%PR_INSTALL%" //ES//%SERVICE_NAME%