@ECHO OFF
ECHO Press any key to begin.
pause >nul

ECHO Starting launch_world.bat
start /b launch_world.bat
ping localhost -w 10>nul

ECHO Starting launch_login.bat
start /b launch_login.bat
ping localhost -w 10>nul

ECHO Starting launch_channel.bat
start /b launch_channel.bat