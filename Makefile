APP_MAIN   = com.example.maryshell/com.example.maryshell.Shell
TEST_MAIN  = com.example.maryshell.tests.Test

# Параметры
VFS    ?=
SCRIPT ?=
TARGET := $(filter-out app test build clean help all, $(MAKECMDGOALS))

TEST_ARGS := $(if $(strip $(VFS)),VFS="$(VFS)") \
             $(if $(strip $(SCRIPT)),SCRIPT="$(SCRIPT)")

TEST_CMD_ARGS := $(TEST_ARGS) $(TARGET)
APP_ARGS      := $(TEST_ARGS)

.PHONY: all app test build clean help

all: help

app:
	@echo Application $(APP_ARGS)
	mvn -q clean javafx:run -Djavafx.args="$(APP_ARGS)"

test:
	@echo Test $(TEST_CMD_ARGS)
	mvn -q clean compile
	mvn -q exec:java \
		-Dexec.mainClass="$(TEST_MAIN)" \
		-Dexec.args="$(TEST_CMD_ARGS)"


build:
	mvn -q clean package

clean:
	mvn -q clean


help:
	@echo Application:
	@echo   make app                                          - without parameters
	@echo   make app VFS=C:/vfs                               - with VFS
	@echo   make app SCRIPT=scripts/startup.txt               - with script
	@echo   make app VFS=C:/vfs SCRIPT=scripts/startup.txt    - with both parameters
	@echo ------
	@echo Tests:
	@echo   make test                                         - default script
	@echo   make test VFS=C:/vfs                              - with VFS
	@echo   make test SCRIPT=scripts/startup.txt              - with script
	@echo   make test VFS=C:/vfs SCRIPT=scripts/startup.txt   - with both parameters
	@echo   make test TEST_FILES=/com/.../test2.txt           - another test
	@echo ------
	@echo Build:
	@echo   make build                                        - build jar
	@echo   make clean                                        - clean