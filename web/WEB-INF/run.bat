@echo off

set BASE_PATH=%~DP0\
set JAVA_HOME=%JAVA_HOME%
set KLASSPATH=%CLASSPATH%;%BASE_PATH%lib\*;%BASE_PATH%classes

"%JAVA_HOME%\bin\java" ^
-classpath "%KLASSPATH%" ^
app.hongs.cmdlet.CmdletRunner %* ^
--basepath "%BASE_PATH%"

@echo on