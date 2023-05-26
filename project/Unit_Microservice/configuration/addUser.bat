set ADDUSERPATH=%1
call %ADDUSERPATH%\target\server\bin\add-user.bat -u userTest -p %BELLINI_PROJECT_PASSWORD%! -a 
echo.
ping -n 121 127.0.0.1 > nul