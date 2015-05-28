@echo off

set CORE_PATH=%~DP0\..
set JAVA_PATH="%JAVA_HOME%\bin\java"
set KLASSPATH=%CLASSPATH%;%CORE_PATH%\lib\*;%CORE_PATH%\lib\classes;%CORE_PATH%\classes

"%JAVA_PATH%" ^
-classpath "%KLASSPATH%" ^
-Dlogs.dir="%CORE_PATH%\var\log" ^
app.hongs.cmdlet.CmdletRunner %* ^
--corepath-- "%CORE_PATH%"

@echo on