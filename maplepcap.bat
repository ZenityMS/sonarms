@echo off
set CLASSPATH=.;dist\odinteh.jar;dist\exttools.jar;dist\mina-core.jar;dist\slf4j-api.jar;dist\slf4j-jdk14.jar;dist\mysql-connector-java-bin.jar;dist\jpcap.jar
java -Dnet.sf.odinms.recvops=recvops.properties -Dnet.sf.odinms.sendops=sendops.properties net.sf.odinms.exttools.maplepcap.MaplePcap karmascissors.pcap > karmascissors.txt
pause