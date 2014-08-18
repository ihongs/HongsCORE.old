@echo off

set VERSION=0.6.0
set TMPNAME=hongs-core.tmp
set JARNAME=hongs-core-framework-%VERSION%
set DSTPATH=%~DP0\dist
set WEBPATH=%~DP0\build\web
set CLSPATH=%~DP0\build\web\WEB-INF\classes

cd %CLSPATH%
jar cvf %DSTPATH%\%JARNAME%.jar app\hongs org\json\simple
cd %~DP0

rmdir %DSTPATH%\%TMPNAME% /S /Q
mkdir %DSTPATH%\%TMPNAME%
xcopy %WEBPATH% %DSTPATH%\%TMPNAME% /E /Y
rmdir %DSTPATH%\%TMPNAME%\WEB-FIN\classes\org /S /Q
rmdir %DSTPATH%\%TMPNAME%\WEB-INF\classes\app\hongs /S /Q
xcopy %DSTPATH%\%JARNAME%.jar %DSTPATH%\%TMPNAME%\WEB-INF\lib

cd %DSTPATH%\%TMPNAME%
jar cvf %~DP0\HongsCORE.war *
cd %~DP0

rmdir %DSTPATH%\%TMPNAME% /S /Q
mkdir %DSTPATH%\%TMPNAME%
xcopy web %DSTPATH%\%TMPNAME%\web /E /Y /I
xcopy src %DSTPATH%\%TMPNAME%\src /E /Y /I
rmdir %DSTPATH%\%TMPNAME%\src\java\org /S /Q
rmdir %DSTPATH%\%TMPNAME%\src\java\app\hongs /S /Q
xcopy %DSTPATH%\%JARNAME%.jar %DSTPATH%\%TMPNAME%\web\WEB-INF\lib
xcopy %DSTPATH%\%JARNAME%.jar %DSTPATH%\%TMPNAME%
xcopy %DSTPATH%\%TMPNAME%.war %DSTPATH%\%TMPNAME%

cd %DSTPATH%\%TMPNAME%
jar cvf %~DP0\HongsCORE.zip *
cd %~DP0

del %DSTPATH%\%JARNAME%.jar
rmdir %DSTPATH%\%TMPNAME% /S /Q

@echo on
