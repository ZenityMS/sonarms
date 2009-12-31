@echo off
set CLASSPATH=.;dist\arberms.jar;dist\mina-core.jar;dist\slf4j-api.jar;dist\slf4j-jdk14.jar;dist\mysql-connector-java-bin.jar
java -Darberms.wzpath=wz\ arberms.tools.ext.wz.KeyGenerator
pause