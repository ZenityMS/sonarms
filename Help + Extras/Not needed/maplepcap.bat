@echo off
set CLASSPATH=.;dist\arberms.jar;dist\exttools.jar;mina-core.jar;slf4j-api.jar;slf4j-jdk14.jar;mysql-connector-java-bin.jar;jpcap.jar
java -Darberms.recvops=recvops.properties -Darberms.sendops=sendops.properties arberms.exttools.maplepcap.MaplePcap mob_skill_status.pcap > mob_skill_status.txt
pause