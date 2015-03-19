@echo off

set BASE_PATH=%~DP0\

"%BASE_PATH%\WEB-INF\run.bat" system:setup

@echo on
