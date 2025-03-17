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

# Check JC_HOME_TOOLS environment variable
if [ -z "$JC_HOME_TOOLS" ]; then
    echo "Environment [JC_HOME_TOOLS] is not set. Please set it to the latest Java Card Development Kit Tools."
    exit 1
fi

if [ ! -f "$JC_HOME_TOOLS/lib/tools.jar" ]; then
    echo "Invalid environment [JC_HOME_TOOLS]. Please set it to the correct path."
    exit 1
fi

SAMPLE_NAME="$1"
JAVA_FILES=java_files.lst

if [ ! -d "$SAMPLE_NAME" ]; then
    echo "Unknown sample name given [$SAMPLE_NAME]"
    exit 1
fi

# Java compilation for applet
cd "$SAMPLE_NAME/applet/src"
find . -name "*.java" > "$JAVA_FILES"
echo "Java file(s) to be compiled:"
cat "$JAVA_FILES"

COMMAND_JAVAC="$JAVA_HOME/bin/javac -g -d ../bin -source 7 -target 7 \
-cp \"$JC_HOME_TOOLS/lib/api_classic-3.2.0.jar:$JC_HOME_TOOLS/lib/api_classic_annotations-3.2.0.jar\" \
-Xlint:-options @$JAVA_FILES"

echo
"Executing: [$COMMAND_JAVAC]"
$COMMAND_JAVAC
RESULT=$?
rm "$JAVA_FILES"
cd -

if [ $RESULT -ne 0 ]; then
    echo "Stopping due to error(s) in Java compilation"
    exit $RESULT
fi

PACKAGE_LIST="$PWD/$SAMPLE_NAME/applet/PackageList.lst"
> "$PACKAGE_LIST"

if [ -n "$2" ]; then
    for pkg in $2; do
        echo "$pkg" >> "$PACKAGE_LIST"
    done
else
    for conf in "$SAMPLE_NAME/applet/configurations"/*.conf; do
        echo "$(basename "$conf" .conf)" >> "$PACKAGE_LIST"
    done
fi

# Converter stage
cd "$SAMPLE_NAME/applet/bin"
echo "Running Java Card converter and verifier..."

while read -r pkg; do
    "$JC_HOME_TOOLS/bin/converter" -config "../configurations/$pkg.conf"
    if [ $? -ne 0 ]; then
        exit 1
    fi
    find ../deliverables -name "$pkg*.exp" | while read -r exp_file; do
        "$JC_HOME_TOOLS/bin/verifyexp" "$exp_file"
        if [ $? -ne 0 ]; then
            exit 1
        fi
    done
done < "$PACKAGE_LIST"

rm "$PACKAGE_LIST"
cd -

# Java compilation for client
cd "$SAMPLE_NAME/client/src"
find . -name "*.java" > "$JAVA_FILES"
echo "Java file(s) to be compiled:"
cat "