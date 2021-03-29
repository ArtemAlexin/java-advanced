#!/usr/bin/env bash

cd ../../../../../../../../

ROOT=${PWD}

BASIC_PATH=${ROOT}/java-advanced-2021
SOLUTION_PATH=${ROOT}/java-advanced/java-solutions
SOLUTION_PATH_TO_GENERATE_DOC = ${ROOT}/javadoc
MODULE_NAME=info.kgeorgiy.ja.alyokhin.jarimplementor

SRC_NAME=info.kgeorgiy.java.advanced.implementor
SRC_PATH=info/kgeorgiy/java/advanced/implementor
MODULE_PATH=info/kgeorgiy/ja/alyokhin/jarimplementor
OUT_PATH=${SOLUTION_PATH}/_build/production/${MODULE_NAME}
REQ_PATH=${BASIC_PATH}/lib:${BASIC_PATH}/artifacts
AUX_PATH=${BASIC_PATH}/modules/${SRC_NAME}/${SRC_PATH}
cd java-advanced
javadoc -d docs -private --module-path=${REQ_PATH}\
--module-source-path=${BASIC_PATH}/modules \
${SOLUTION_PATH}/${MODULE_PATH}/*.java \
${AUX_PATH}/Impler.java ${AUX_PATH}/JarImpler.java ${AUX_PATH}/ImplerException.java

