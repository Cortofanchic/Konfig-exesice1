APP_MAIN   = com.example.maryshell/com.example.maryshell.Shell
TEST_MAIN  = com.example.maryshell.tests.Test
TEST_FILES = /com/example/maryshell/tests/test1.txt

.PHONY: all app test test-all build clean help

all: help

## Запустить приложение (интерактивный режим)
app:
	@echo ">>> Запуск приложения"
	mvn -q clean javafx:run

## Запустить тест с прогоном сценария
test:
	@echo ">>> Запуск теста: $(TEST_FILES)"
	mvn -q clean compile
	mvn -q exec:java \
		-Dexec.mainClass="$(TEST_MAIN)" \
		-Dexec.args="$(TEST_FILES)"

## Запустить тест со всеми сценариями
test-all:
	@echo ">>> Запуск теста со всеми сценариями"
	mvn -q clean compile
	mvn -q exec:java \
		-Dexec.mainClass="$(TEST_MAIN)" \
		-Dexec.args="$(TEST_FILES)"

## Собрать проект
build:
	@echo ">>> Сборка"
	mvn -q clean package

## Очистить сборку
clean:
	@echo ">>> Очистка"
	mvn -q clean

## Показать справку
help:
	@echo "Доступные команды:"
	@echo "  make app        — запустить приложение"
	@echo "  make test       — запустить тест"
	@echo "  make test-all   — запустить тест со всеми сценариями"
	@echo "  make build      — собрать проект"
	@echo "  make clean      — очистить сборку"