@echo off
title ScrapeMaster - Launching...
echo Compiling ScraperGUI...
javac -cp "lib\jsoup-1.17.2.jar" -d out src\ScraperGUI.java
if %errorlevel% neq 0 (
    echo.
    echo Compile failed. Make sure Java JDK is installed.
    pause
    exit /b 1
)
echo Launching ScrapeMaster...
java -cp "lib\jsoup-1.17.2.jar;out" ScraperGUI
