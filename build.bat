@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo   K-Map Solver and GUI - Build Script
echo ===================================================

:: Prioritize JDK 24 / modern JDK
if exist "C:\Users\aayus\.jdks\openjdk-24.0.1\bin\javac.exe" (
    set "JAVAC_CMD=C:\Users\aayus\.jdks\openjdk-24.0.1\bin\javac.exe"
    set "JAR_CMD=C:\Users\aayus\.jdks\openjdk-24.0.1\bin\jar.exe"
) else if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javac.exe" (
        set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
        set "JAR_CMD=%JAVA_HOME%\bin\jar.exe"
    )
)

if not defined JAVAC_CMD (
    where javac >nul 2>&1
    if !ERRORLEVEL! EQU 0 (
        set "JAVAC_CMD=javac"
        set "JAR_CMD=jar"
    ) else (
        echo [ERROR] javac could not be found. Please install a modern JDK or set JAVA_HOME.
        pause
        exit /b 1
    )
)

echo Using compiler: "!JAVAC_CMD!"

:: Create out directory
if not exist "out" mkdir out

echo [1/3] Compiling Java sources (targeting Java 8 for maximum runtime compatibility)...
"!JAVAC_CMD!" --release 8 -d out src\*.java
if !ERRORLEVEL! NEQ 0 (
    echo [INFO] Falling back to standard compilation...
    "!JAVAC_CMD!" -d out src\*.java
    if !ERRORLEVEL! NEQ 0 (
        echo [ERROR] Compilation failed!
        pause
        exit /b 1
    )
)
echo [OK] Compilation successful.

echo [2/3] Creating executable JAR package (KMap.jar)...
"!JAR_CMD!" --create --file KMap.jar --manifest META-INF\MANIFEST.MF -C out src 2>nul
if !ERRORLEVEL! NEQ 0 (
    "!JAR_CMD!" cfm KMap.jar META-INF\MANIFEST.MF -C out src
    if !ERRORLEVEL! NEQ 0 (
        echo [ERROR] JAR packaging failed!
        pause
        exit /b 1
    )
)
echo [OK] KMap.jar created successfully.

echo [3/3] Build complete!
echo.
echo You can run the application using:
echo   .\run.bat
echo   or: java -jar KMap.jar
echo ===================================================
