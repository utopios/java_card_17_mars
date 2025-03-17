#!/bin/bash

# Copyright (c) 1998, 2024, Oracle and/or its affiliates. All rights reserved.

set -e

# Get the current directory name as SAMPLE_NAME
SAMPLE_NAME=$(basename "$PWD")

echo "Sample name: [$SAMPLE_NAME]"
echo

# Move to the parent directory
cd ..

# Call the build script with SAMPLE_NAME as argument
./build.sh "$SAMPLE_NAME"
