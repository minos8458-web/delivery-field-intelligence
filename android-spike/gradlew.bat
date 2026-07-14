@rem Gradle start script (Windows)
@rem Generated for DFI C4 spike — Gradle 8.9

@if "%DEBUG%"=="" @echo off

set APP_HOME=%~dp0

if defined JAVA_HOME goto findJavaFromJavaHome
set JAVA_EXE=java.exe
goto execute

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

:execute
set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
"%JAVA_EXE%" -Xmx64m -Xms64m -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
