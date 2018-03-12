@echo OFF

SET TAPE_DIRECTORY=H:\Finance\OptionData\NSignalData

SET /P DATE=Tape file event date:
SET PREFIX=%TAPE_DIRECTORY%\%DATE%
SET SUFFIX=.DO.nxc
SET /A DAY=0

:start
SET /A R=%DATE% %% 100
ECHO %DATE% %DAY% %R%
SET /A DAY=%DAY%+1
IF %DAY% LEQ 31 (
	IF %DAY% LSS 10 (
		ECHO java -jar NSignalReceiver.jar %TAPE_DIRECTORY%\%DATE%0%DAY%%SUFFIX%.trade
		java -jar NSignalReceiver.jar %TAPE_DIRECTORY%\%DATE%0%DAY%%SUFFIX%.trade
::		ECHO NxCoreReader %PREFIX%0%DAY%%SUFFIX% -r
::		CALL NxCoreReader %PREFIX%0%DAY%%SUFFIX% -r
	) ELSE (
		ECHO NxCoreReader %TAPE_DIRECTORY%\%DATE%%DAY%%SUFFIX%.trade
		java -jar NSignalReceiver.jar %TAPE_DIRECTORY%\%DATE%%DAY%%SUFFIX%.trade
::		ECHO NxCoreReader %PREFIX%%DAY%%SUFFIX% -r
::		CALL NxCoreReader %PREFIX%%DAY%%SUFFIX% -r
	)
	GOTO start
)
SET /A R=%DATE% %% 100
IF %R% LSS 12 (
	SET /A DATE=%DATE%+1
	SET /A DAY=0
	GOTO start
)
pause