#!/bin/bash

# Copyright (c) 1998, 2024, Oracle and/or its affiliates. All rights reserved.

set -e

if [ -z "$1" ]; then
    echo "No parameters given"
    echo "Usage: build.sh \"sample name\" (e.g., \"ArrayViews\")"
    exit 1
fi

# Check JAVA_HOME environment variable
if [ -z "$JAVA_HOME" ]; then
    echo "Environment [JAVA_HOME] is not set. Please set it to point to JDK 17."
    exit 1
fi

# Check JC_HOME_SIMULATOR environment variable
if [ -z "$JC_HOME_SIMULATOR" ]; then
    echo "Environment [JC_HOME_SIMULATOR] is not set. Please set it to the latest Java Card Development Kit Simulator."
    exit 1
fi

if [ ! -d "$JC_HOME_SIMULATOR/runtime/bin" ]; then
    echo "Invalid environment [JC_HOME_SIMULATOR]. Please set it to the correct path."
    exit 1
fi

SAMPLE_NAME="$1"
CLIENT_NAME="$2"
ARGS_CLIENT="$3"

if [ ! -d "$SAMPLE_NAME" ]; then
    echo "Unknown sample name given [$SAMPLE_NAME]"
    exit 1
fi

echo "Sample name  : [$SAMPLE_NAME]"
echo "Client name  : [$CLIENT_NAME]"
echo "Argument list:"
for arg in $ARGS_CLIENT; do
    echo "  $arg"
done

cd "$SAMPLE_NAME/client/bin"

EXT_MODULEPATH="$JC_HOME_SIMULATOR/client/AMService:$JC_HOME_SIMULATOR/client/COMService"
COMMAND_JAVA="$JAVA_HOME/bin/java \
-p \"$JC_HOME_SIMULATOR/client/AMService/amservice.jar:$JC_HOME_SIMULATOR/client/COMService/socketprovider.jar\" \
--module-path $EXT_MODULEPATH --add-modules ALL-MODULE-PATH $CLIENT_NAME $ARGS_CLIENT"

echo
"Executing: [$COMMAND_JAVA]"
$COMMAND_JAVA
RESULT=$?

if [ $RESULT -eq 0 ]; then
    echo "Success"
else
    echo "Execution of [$CLIENT_NAME] failed"
    exit $RESULT
fi
