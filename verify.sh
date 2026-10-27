#!/usr/bin/env sh
set -eu
mkdir -p out
javac --release 17 -Xlint:all -Werror -d out src/main/java/dev/petrocelli/*.java src/test/java/dev/petrocelli/*.java
java -ea -cp out dev.petrocelli.TaskQueueTest
