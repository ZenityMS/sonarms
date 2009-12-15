@echo off
set CLASSPATH=.;dist\*
java net.sf.odinms.exttools.wzextract.XMLWzExtract wz\Map.wz
pause