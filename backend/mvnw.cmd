@echo off
set "WRAPPER_JAR=.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_CLASS=org.apache.maven.wrapper.MavenWrapperMain"

if not exist "%WRAPPER_JAR%" (
    echo Error: Could not find %WRAPPER_JAR%
    exit /b 1
)

"%JAVA_HOME%\bin\java.exe" -Dmaven.multiModuleProjectDirectory="%CD%" -classpath "%WRAPPER_JAR%" %WRAPPER_CLASS% %*
