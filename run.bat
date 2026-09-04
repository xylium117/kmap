@echo off
setlocal

:: Prioritize modern JDK in user .jdks or JAVA_HOME if present
if exist "C:\Users\aayus\.jdks\openjdk-24.0.1\bin\java.exe" (
    set "JAVA_CMD=C:\Users\aayus\.jdks\openjdk-24.0.1\bin\java.exe"
) else if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    )
)

if not defined JAVA_CMD (
    set "JAVA_CMD=java"
)

echo Starting K-Map GUI using: "%JAVA_CMD%"

if exist "KMap.jar" (
    "%JAVA_CMD%" -jar KMap.jar
) else (
    "%JAVA_CMD%" -cp out src.KMapInput
)
