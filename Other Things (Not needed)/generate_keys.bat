@echo off
set CLASSPATH=.;dist\*
java -Dnet.sf.odinms.wzpath=wz\ net.sf.odinms.tools.ext.wz.KeyGenerator
pause