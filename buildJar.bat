@echo off

set JARNAME=hongs-core-framework-0.3.0
set PKGLIST=app\hongs org\json\simple
set CLSPATH=%~DP0\build\web\WEB-INF\classes
set DSTPATH=%~DP0\dist

cd %CLSPATH%
jar cvf %DSTPATH%\%JARNAME%.jar %PKGLIST%
cd %~DP0

@echo on
