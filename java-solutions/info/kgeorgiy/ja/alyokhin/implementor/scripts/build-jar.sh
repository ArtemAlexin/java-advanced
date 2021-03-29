#!/usr/bin/env bash

cd ../../../../../../../../

ROOT=${PWD}

BASIC_PATH=${ROOT}/java-advanced-2021
SOLUTION_PATH=${ROOT}/java-advanced/java-solutions

MODULE_NAME=info.kgeorgiy.ja.alyokhin.jarimplementor
MODULE_PATH=info/kgeorgiy/ja/alyokhin/jarimplementor

OUT_PATH=${SOLUTION_PATH}/_build/production/${MODULE_NAME}
OUT_PATH_TO_B=${SOLUTION_PATH}/_build
REQ_PATH=${BASIC_PATH}/lib:${BASIC_PATH}/artifacts
SRC_PATH=${SOLUTION_PATH}
JAR_PATH=${SOLUTION_PATH}

rm -rf ${OUT_PATH_TO_B}

javac --module-path ${REQ_PATH} ${SRC_PATH}/module-info.java ${SRC_PATH}/${MODULE_PATH}/*.java -d ${OUT_PATH}

cd ${OUT_PATH}

mkdir ${JAR_PATH} 2> /dev/null

jar -c --file=${JAR_PATH}/_implementor.jar --main-class=${MODULE_NAME}.JarImplementor --module-path=${REQ_PATH} \
    ${MODULE_PATH}
rm -rf ${OUT_PATH_TO_B}