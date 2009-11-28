@echo off
title SonarMS: inactive
color 1b
echo SonarMS Launcher
echo
echo.
echo Ready to execute, press any key
pause >nul
cls
color 4c
title SonarMS: resetting logged in
start resetLoggedIn.bat
title SonarMS: activating 0/3
start /b launch_world.bat
title SonarMS: activating 1/3
ping localhost -w 10000 >nul
start /b launch_login.bat >nul
title SonarMS: activating 2/3
ping localhost -w 10000 >nul
start /b launch_channel.bat >nul
title SonarMS: activating 3/3
ping localhost -w 10000 >nul
color 2a
title SonarMS: Fully Active
