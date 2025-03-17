#!/bin/bash

# Copyright (c) 1998, 2024, Oracle and/or its affiliates. All rights reserved.

set -e

# Evaluate samples directory and sample name from the current directory
SAMPLES_DIR=$(dirname "$PWD")
SAMPLE_NAME=$(basename "$PWD")

echo "Sample name: [$SAMPLE_NAME]"
echo

CLIENT_NAME="com.utopios.client.ScaffoldingClient"

CAP="$SAMPLES_DIR/$SAMPLE_NAME/applet/deliverables/Scaffolding/com/utopios/scaffolding/javacard/Scaffolding.cap"
PROPS="$JC_HOME_SIMULATOR/samples/client.config.properties"

ARGS_CLIENT="-cap=$CAP -props=$PROPS"

# Move to the parent directory
cd ..

# Execute the run script with parameters
./run.sh "$SAMPLE_NAME" "$CLIENT_NAME" "$ARGS_CLIENT"
