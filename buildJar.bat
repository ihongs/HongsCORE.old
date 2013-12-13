@echo off

set VERSION=0.6.0
set WARNAME=hongs-core-%VERSION%
set JARNAME=hongs-core-framework-%VERSION%
set DSTPATH=%~DP0\dist
set WEBPATH=%~DP0\build\web
set CLSPATH=%~DP0\build\web\WEB-INF\classes

cd %CLSPATH%
jar cvf %DSTPATH%\%JARNAME%.jar app\hongs org\json\simple
cd %~DP0

rmdir %DSTPATH%\%WARNAME%.tmp
mkdir %DSTPATH%\%WARNAME%.tmp
xcopy %WEBPATH%\* %DSTPATH%\%WARNAME%.tmp\ /E /Y
rmdir %DSTPATH%\%WARNAME%\WEB-INF\classes\app\hongs /S /Q
rmdir %DSTPATH%\%WARNAME%\WEB-FIN\classes\org\json\simple /S /Q
xcopy %DSTPATH%\%JARNAME%.jar %DSTPATH%\%WARNAME%.tmp\WEB-INF\lib\

cd %DSTPATH%\%WARNAME%.tmp
jar cvf %DSTPATH%\%WARNAME%.jar *
cd %~DP0

rmdir %DSTPATH%\%WARNAME%.tmp
mkdir %DSTPATH%\%WARNAME%.tmp
xcopy web %DSTPATH%\%WARNAME%\ /E /Y
xcopy src %DSTPATH%\%WARNAME%\ /E /Y
rmdir %DSTPATH%\%WARNAME%\src\java\app\hongs /S /Q
rmdir %DSTPATH%\%WARNAME%\src\java\org\json\simple /S /Q
xcopy %DSTPATH%\%JARNAME%.jar %DSTPATH%\%WARNAME%.tmp\web\WEB-INF\lib\
xcopy %DSTPATH%\%JARNAME%.jar %DSTPATH%\%WARNAME%.tmp\
xcopy %DSTPATH%\%WARNAME%.war %DSTPATH%\%WARNAME%.tmp\

cd %DSTPATH%\%WARNAME%.tmp
jar cvf %DSTPATH%\%WARNAME%.zip *
cd %~DP0

del %DSTPATH%\%JARNAME%.jar
del %DSTPATH%\%WARNAME%.war
rmdir %DSTPATH%\%WARNAME%.tmp

@echo on
