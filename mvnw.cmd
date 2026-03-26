@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM Begin all REM://
@echo off

@REM Set the current directory to the location of this script
set WRAPPER_DIR=%~dp0

@REM Check JAVA_HOME or java on PATH
if not "%JAVA_HOME%"=="" goto javaHomeSet
set JAVACMD=java
goto checkJava

:javaHomeSet
set "JAVACMD=%JAVA_HOME%\bin\java.exe"

:checkJava
"%JAVACMD%" -version >nul 2>&1
if errorlevel 1 (
    echo Error: JAVA_HOME is not set and java is not in PATH.
    exit /b 1
)

set WRAPPER_JAR="%WRAPPER_DIR%.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_PROPERTIES="%WRAPPER_DIR%.mvn\wrapper\maven-wrapper.properties"

@REM Download maven-wrapper.jar if it doesn't exist
if exist %WRAPPER_JAR% goto runWrapper

@REM Use PowerShell to download the wrapper jar
echo Downloading Maven Wrapper...
for /f "tokens=2 delims==" %%a in ('findstr "wrapperUrl" %WRAPPER_PROPERTIES%') do set WRAPPER_URL=%%a
powershell -Command "(New-Object System.Net.WebClient).DownloadFile('%WRAPPER_URL%', '%WRAPPER_DIR%.mvn\wrapper\maven-wrapper.jar')"

@REM If download fails, try downloading maven directly
if not exist %WRAPPER_JAR% (
    echo Maven Wrapper JAR download failed. Attempting direct Maven download...
    for /f "tokens=2 delims==" %%a in ('findstr "distributionUrl" %WRAPPER_PROPERTIES%') do set MVN_URL=%%a
    
    set "MVN_ZIP=%TEMP%\apache-maven.zip"
    set "MVN_DIR=%WRAPPER_DIR%.mvn\maven"
    
    powershell -Command "(New-Object System.Net.WebClient).DownloadFile('%MVN_URL%', '%MVN_ZIP%')"
    powershell -Command "Expand-Archive -Path '%MVN_ZIP%' -DestinationPath '%MVN_DIR%' -Force"

    @REM Find the extracted directory and run mvn
    for /d %%d in ("%MVN_DIR%\apache-maven-*") do (
        "%%d\bin\mvn.cmd" %*
        exit /b %ERRORLEVEL%
    )
    exit /b 1
)

:runWrapper
"%JAVACMD%" -jar %WRAPPER_JAR% %*
