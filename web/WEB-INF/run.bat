@echo off

set BASE_PATH=%~DP0\
set JAVA_HOME=%JAVA_HOME%
set KLASSPATH=%CLASSPATH%
set CLASSPATH=%CLASSPATH%;%BASE_PATH%lib\*;%BASE_PATH%classes

"%JAVA_HOME%\bin\java" ^
-classpath "%CLASSPATH%" ^
app.hongs.cmdlet.Cmdlet %* ^
--basepath "%BASE_PATH%"

set CLASSPATH=%KLASSPATH%

@echo on