@echo off
set CLSPATH=\Workspace\HongsCORE\build\web\WEB-INF\classes
set JARNAME=hongs-core-framework
set VERSION=0.1.2
e:
cd \Workspace\HongsCORE\build\web\WEB-INF\classes
jar cvf hongs-core-0.1.2.jar app\hongs org\json\simple
copy %JARNAME%-%VERSION%.jar \Workspace\HongsADMS\web\WEB-INF\lib\ /Y /B
cd \Workspace\HongsCORE
@echo on