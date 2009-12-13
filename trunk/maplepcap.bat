@echo off
set CLASSPATH=.;dist\*
java -Dnet.sf.odinms.recvops=recvops.properties -Dnet.sf.odinms.sendops=sendops.properties net.sf.odinms.exttools.maplepcap.MaplePcap mob_skill_status.pcap > mob_skill_status.txt
pause