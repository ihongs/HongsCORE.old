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

rmdir %DSTPATH%\%WARNAME% /S /Q
mkdir %DSTPATH%\%WARNAME%
xcopy %WEBPATH% %DSTPATH%\%WARNAME% /E /Y
rmdir %DSTPATH%\%WARNAME%\WEB-FIN\classes\org /S /Q
rmdir %DSTPATH%\%WARNAME%\WEB-INF\classes\app\hongs /S /Q
xcopy %DSTPATH%\%JARNAME%.jar %DSTPATH%\%WARNAME%\WEB-INF\lib

cd %DSTPATH%\%WARNAME%
jar cvf %DSTPATH%\%WARNAME%.war *
cd %~DP0

rmdir %DSTPATH%\%WARNAME% /S /Q
mkdir %DSTPATH%\%WARNAME%
xcopy web %DSTPATH%\%WARNAME%\web /E /Y /I
xcopy src %DSTPATH%\%WARNAME%\src /E /Y /I
rmdir %DSTPATH%\%WARNAME%\src\java\org /S /Q
rmdir %DSTPATH%\%WARNAME%\src\java\app\hongs /S /Q
xcopy %DSTPATH%\%JARNAME%.jar %DSTPATH%\%WARNAME%\web\WEB-INF\lib
xcopy %DSTPATH%\%JARNAME%.jar %DSTPATH%\%WARNAME%
xcopy %DSTPATH%\%WARNAME%.war %DSTPATH%\%WARNAME%

cd %DSTPATH%\%WARNAME%
jar cvf %DSTPATH%\%WARNAME%.zip *
cd %~DP0

copy %DSTPATH%\%WARNAME%.zip %~DP0\hongs-core.zip /Y
del %DSTPATH%\%JARNAME%.jar
del %DSTPATH%\%WARNAME%.war
del %DSTPATH%\%WARNAME%.zip
rmdir %DSTPATH%\%WARNAME% /S /Q

@echo on
