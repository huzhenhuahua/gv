@echo off

set CLASSPATH=..\dist\HPBExportTool.jar;..\lib\ojdbc6.jar

java -Xms2048m -Xmx2048m massexport.MassExport itg %1 yes no
