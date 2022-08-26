@echo off

rem CD into directory where our script is located to get classpath right

SET SCRIPT_PATH=%~dp0
cd %SCRIPT_PATH%

rem Add 3rd party tools in path
SET "PATH=%SCRIPT_PATH%\tools;%PATH%"

.\jre\bin\java --add-exports java.base/java.lang=ALL-UNNAMED --add-exports java.desktop/sun.awt=ALL-UNNAMED --add-exports java.desktop/sun.java2d=ALL-UNNAMED -Xmx4096m -Xms4096m -cp "*;deps\*" asaintsever.tinyworld.ui.UI