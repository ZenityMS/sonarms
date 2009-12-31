#!/bin/sh
CLASSPATH=".:dist/arberms.jar:dist/mina-core.jar:dist/slf4j-api.jar:dist/slf4j-jdk14.jar:dist/mysql-connector-java-bin.jar"
export CLASSPATH
java -Darberms.recvops=config\recvops.properties \
-Dnet.sf.odinms.sendops=config\sendops.properties \
-Dnet.sf.odinms.wzpath=wz \
-Djavax.net.ssl.keyStore=filename.keystore \
-Djavax.net.ssl.keyStorePassword=passwd \
-Djavax.net.ssl.trustStore=filename.keystore \
-Djavax.net.ssl.trustStorePassword=passwd \
-Xmx150M \
net.sf.odinms.net.world.WorldServer