@echo off
setlocal

set "MVN_REPO=%USERPROFILE%\.m2\repository"
set "CP=target/classes"

set "CP=%CP%;%MVN_REPO%\com\formdev\flatlaf\3.4\flatlaf-3.4.jar"
set "CP=%CP%;%MVN_REPO%\com\zaxxer\HikariCP\5.1.0\HikariCP-5.1.0.jar"
set "CP=%CP%;%MVN_REPO%\org\slf4j\slf4j-api\2.0.9\slf4j-api-2.0.9.jar"
set "CP=%CP%;%MVN_REPO%\ch\qos\logback\logback-classic\1.4.14\logback-classic-1.4.14.jar"
set "CP=%CP%;%MVN_REPO%\ch\qos\logback\logback-core\1.4.14\logback-core-1.4.14.jar"
set "CP=%CP%;%MVN_REPO%\com\h2database\h2\2.2.224\h2-2.2.224.jar"

echo ============================================
echo   Library Management System
echo   Running in GUI mode...
echo ============================================

java -cp "%CP%" com.library.Main

if %ERRORLEVEL% neq 0 (
    echo.
    echo Build first with: mvn clean compile
    echo Or use: .\mvnw.cmd compile
    pause
)
