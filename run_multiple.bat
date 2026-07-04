@echo off
javac -cp "lib\jsoup-1.17.2.jar" -d out src\ScrapeMasterMultiple.java
java  -cp "lib\jsoup-1.17.2.jar;out" ScrapeMasterMultiple
pause
