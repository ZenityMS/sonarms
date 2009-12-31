@echo off
set CLASSPATH=.;dist\arberms.jar;dist\exttools.jar;mina-core.jar;slf4j-api.jar;slf4j-jdk14.jar;mysql-connector-java-bin.jar;jpcap.jar
java arberms.exttools.wzextract.XMLWzExtract wz\Map.wz
pause