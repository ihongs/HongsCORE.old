@echo off
set JARNAME=hongs-core-framework-0.2.0
set PKGPATH=app\hongs org\json\simple
set CLSPATH=%~DP0\build\web\WEB-INF\classes
set DSTPATH=%~DP0\dist
cd      %CLSPATH%
jar cvf %JARNAME%.jar %PKGPATH%
copy    %JARNAME%.jar %DSTPATH%
cd      %~DP0
@echo on