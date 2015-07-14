@ECHO OFF

REM This is an equivalent Windows batch file to complement the unix shell script
REM Corresponding lines from the shell script are printed before the matching batch file commands

REM # Config files can define several variables used throughout this script.
REM # JAVACMD - The java command to launch osmosis.
REM # JAVACMD_OPTIONS - The options to append to the java command, typically used to modify jvm settings such as max memory.
REM # OSMOSIS_OPTIONS - The options to apply to all osmosis invocations, typically used to add plugins or make quiet operation the default.

REM if [ -f /etc/osmosis ] ; then
REM   . /etc/osmosis
REM fi
IF EXIST "%ALLUSERSPROFILE%\osmosis.bat" CALL "%ALLUSERSPROFILE%\osmosis.bat"

REM if [ -f "$HOME/.osmosis" ] ; then
REM   . "$HOME/.osmosis"
REM fi
IF EXIST "%USERPROFILE%\osmosis.bat" CALL "%USERPROFILE%\osmosis.bat"


REM if [ -z "$JAVACMD" ] ; then
REM   # No JAVACMD provided in osmosis config files, therefore default to java
REM   JAVACMD=java
REM fi
IF "%JAVACMD%"=="" set JAVACMD=java

REM Set "SAVEDIR" to the current directory
set SAVEDIR=%CD%
set MYAPP_HOME=%~dp0..
REM Now make the MYAPP_HOME path absolute
cd /D %MYAPP_HOME%
set MYAPP_HOME=%CD%
REM Change back to the original directory
cd /D %SAVEDIR%

set MAINCLASS=org.codehaus.classworlds.Launcher
set PLEXUS_CP=%MYAPP_HOME%\lib\default\plexus-classworlds-2.5.2.jar
SET EXEC="%JAVACMD%" %JAVACMD_OPTIONS% -cp "%PLEXUS_CP%" -Dapp.home="%MYAPP_HOME%" -Dclassworlds.conf="%MYAPP_HOME%\config\plexus.conf" %MAINCLASS%  %OSMOSIS_OPTIONS% %*

%EXEC%
