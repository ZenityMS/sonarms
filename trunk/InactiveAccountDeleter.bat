@echo off
TITLE Inactive Account Deleter - Written by Emilyx3
set CLASSPATH=.;dist\arberms.jar;dist\lib\mina-core.jar;dist\lib\slf4j-api.jar;dist\lib\slf4j-jdk14.jar;dist\lib\mysql-connector-java-bin.jar
java net.sf.odinms.server.InactiveAccountDeleterWindow
