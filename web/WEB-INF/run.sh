#!/bin/bash

BASE_PATH=`dirname $0`
JAVA_HOME="$JAVA_HOME"
CLASSPATH="$BASE_PATH/classes":\
"$BASE_PATH/lib/hongs-core-framework-0.1.1.jar":\
"$BASE_PATH/lib/mysql-connector-java-3.1.14-bin.jar":\
"$BASE_PATH/lib/commons-fileupload-1.2.2.jar":\
"$CLASSPATH"

"$JAVA_HOME/bin/java" \
-classpath "$CLASSPATH" \
app.hongs.shell $@ \
-base-path "$BASE_PATH"