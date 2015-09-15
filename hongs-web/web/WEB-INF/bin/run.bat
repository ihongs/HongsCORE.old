@echo off

set CORE_PATH=%~DP0\..
set JAVA_PATH="%JAVA_HOME%\bin\java"
set KLASSPATH=%CLASSPATH%;%CORE_PATH%\lib\*;%CORE_PATH%\classes;%CORE_PATH%\lib\classes

"%JAVA_PATH%" %JAVA_OPTS% ^
  -classpath "%KLASSPATH%"^
  -Dlog4j.configurationFile="\\%CORE_PATH%\lib\classes\log4j2.xml"^
  -Dlogs.dir="\\%CORE_PATH%\var\log"^
  -Dtmps.dir="\\%CORE_PATH%\tmp\log"^
  app.hongs.cmdlet.CmdletRunner %*^
  --corepath "%CORE_PATH%"

@echo on