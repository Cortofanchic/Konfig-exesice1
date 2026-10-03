@echo off
cd /d "%~dp0"

echo TEST 1: without parameters
make app

echo TEST 2: with VFS
make app VFS="src/main/resources/com/example/maryshell/vfs/vfs-test-1.json"

echo TEST 3: with script
make app SCRIPT="src/main/resources/com/example/maryshell/tests/test2-1.txt"

echo TEST 4: with VFS and with script
make app VFS="src/main/resources/com/example/maryshell/vfs/vfs-test-2.json" SCRIPT="src/main/resources/com/example/maryshell/tests/test1.txt"

echo TEST 5: test
make test

echo TEST 6: test with VFS
make test VFS="src/main/resources/com/example/maryshell/vfs/vfs-test-1.json"

echo TEST 7: test with VFS and with script
make тест VFS="src/main/resources/com/example/maryshell/vfs/vfs-test-3.json" SCRIPT="src/main/resources/com/example/maryshell/tests/test1.txt"

echo TEST 8: test with test script
make test "src/main/resources/com/example/maryshell/tests/test1.txt"

echo TEST 8: test with script
make test SCRIPT="src/main/resources/com/example/maryshell/tests/test1.txt"