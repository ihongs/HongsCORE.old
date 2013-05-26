#!/bin/bash

BASE_PATH=`dirname $0`
JAVA_HOME="$JAVA_HOME"
CLASSPATH="$BASE_PATH/classes":\
"$PREFIX/lib/hongs-core-framework-0.1.1.jar":\
"$PREFIX/lib/mysql-connector-java-3.1.14-bin.jar":\
"$PREFIX/lib/commons-fileupload-1.2.2.jar":\
"$CLASSPATH"

"$JAVA_HOME/bin/java" \
-classpath "$CLASSPATH" \
app.hongs.shell $@ \
-base-path "$BASE_PATH"