#!/bin/bash

BASE_PATH=`dirname $0`
JAVA_HOME="$JAVA_HOME"
CLASSPATH="$BASE_PATH/classes":\
"$BASE_PATH/lib/hongs-core-framework-0.6.0.jar":\
"$BASE_PATH/lib/mysql-connector-java-5.1.24-bin.jar":\
"$BASE_PATH/lib/commons-fileupload-1.2.2.jar":\
"/opt/apache-tomcat-7.0.39/lib/servlet-api.jar":\
"$CLASSPATH"

"$JAVA_HOME/bin/java" \
-classpath "$CLASSPATH" \
app.hongs.cmdlet.Cmdlet $@ \
-base-path "$BASE_PATH"