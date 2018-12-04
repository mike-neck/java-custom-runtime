#!/usr/bin/env bash

JAVA11=${JAVA_HOME}

MODULES=$(${JAVA11}/bin/jdeps --list-deps build/mod/java-custom-runtime.jar  | tr "\n" "," | tr -d [:space:])

${JAVA11}/bin/jlink --compress=2 --module-path ${JAVA11}/jmods --add-modules ${MODULES} --output build/mod/lambda-custom-java-runtime
