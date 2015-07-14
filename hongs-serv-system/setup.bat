@echo off

set CORE_PATH=%~DP0\

"%CORE_PATH%\bin\run.bat" system:setup $@

@echo on
