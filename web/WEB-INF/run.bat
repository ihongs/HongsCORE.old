@echo off

set BASE_PATH=.
set JAVA_HOME=%JAVA_HOME%
set KLASSPATH=%CLASSPATH%
set CLASSPATH=%BASE_PATH%\classes;^
%BASE_PATH%\lib\hongs-core-framework-0.1.1.jar;^
%BASE_PATH%\lib\mysql-connector-java-3.1.14-bin.jar;^
%BASE_PATH%\lib\commons-fileupload-1.2.2.jar;^
D:\Applications\apache-tomcat-7.0.39\lib\servlet-api.jar;^
%CLASSPATH%

"%JAVA_HOME%\bin\java" ^
-classpath "%CLASSPATH%" ^
app.hongs.shell %* ^
-base.path "%BASE_PATH%"

set CLASSPATH=%KLASSPATH%

@echo on