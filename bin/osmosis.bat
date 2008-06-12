@ECHO OFF
REM This is an equivalent Windows batch file to complement the unix shell script
REM Corresponding lines from the shell script are printed before the matching batch file commands

REM if [ -f /etc/osmosis ] ; then
REM   . /etc/osmosis
REM fi
IF EXIST %ALLUSERSPROFILE%\osmosis.bat CALL %ALLUSERSPROFILE%\osmosis.bat

REM if [ -f "$HOME/.osmosis" ] ; then
REM   . "$HOME/.osmosis"
REM fi
IF EXIST %USERPROFILE%\osmosis.bat CALL %USERPROFILE%\osmosis.bat


REM if [ -z "$JAVACMD" ] ; then
REM   # No JAVACMD provided in osmosis config files, therefore default to java
REM   JAVACMD=java
REM fi
IF "%JAVACMD%"=="" set JAVACMD=java

REM ## resolve links - $0 may be a link to application
REM PRG="$0"
REM
REM # need this for relative symlinks
REM while [ -h "$PRG" ] ; do
REM   ls=`ls -ld "$PRG"`
REM   link=`expr "$ls" : '.*-> \(.*\)$'`
REM   if expr "$link" : '/.*' > /dev/null; then
REM     PRG="$link"
REM   else
REM     PRG="`dirname "$PRG"`/$link"
REM   fi
REM done
REM No symbolic links on Windows, so no equivalent

REM # make it fully qualified
REM saveddir=`pwd`
REM MYAPP_HOME=`dirname "$PRG"`/..
REM MYAPP_HOME=`cd "$MYAPP_HOME" && pwd`
REM cd "$saveddir"
REM
REM #echo "myapp is installed in $MYAPP_HOME"

REM Set "SAVEDIR" to the current directory
set SAVEDIR=%CD%
set MYAPP_HOME=%~dp0..
REM Now make the MYAPP_HOME path absolute
cd %MYAPP_HOME%
set MYAPP_HOME=%CD%
REM Change back to the original directory
cd %SAVEDIR%

REM MAINCLASS=com.bretth.osmosis.core.Osmosis
REM EXEC="$JAVACMD -cp $MYAPP_HOME/osmosis.jar:$MYAPP_HOME/lib/mysql-connector-java-5.0.7-bin.jar:$MYAPP_HOME/lib/postgresql-8.3-603.jdbc4.jar:$MYAPP_HOME/lib/postgis_1.3.2.jar $MAINCLASS $@"
REM 
REM #echo $EXEC
REM exec $EXEC
set MAINCLASS=com.bretth.osmosis.core.Osmosis

SET EXEC=%JAVACMD% -cp %MYAPP_HOME%\osmosis.jar;%MYAPP_HOME%\lib\mysql-connector-java-5.0.7-bin.jar;%MYAPP_HOME%\lib\postgresql-8.3-603.jdbc4.jar;%MYAPP_HOME%\lib\postgis_1.3.2.jar %MAINCLASS% %*

%EXEC%