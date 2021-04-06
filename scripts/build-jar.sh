#!/bin/bash
cd ../

EXTERN_MODULE=../java-advanced-2021
TO_COMPILE=${EXTERN_MODULE}/modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor
MY_MODULE=java.solutions/info/kgeorgiy/ja/alyokhin/implementor
MODULE_NAME=java.solutions

cp -a java-solutions ${MODULE_NAME}

javac -d tmp \
--module-path ${EXTERN_MODULE}/lib:${EXTERN_MODULE}/artifacts \
--module-source-path ${EXTERN_MODULE}/modules:. \
--module ${MODULE_NAME}
rm -rf ${MODULE_NAME}

mkdir -p artifacts

cd tmp/${MODULE_NAME} || exit

jar -c -f ../../artifacts/info.kgeorgiy.ja.alyokhin.implementor.jar \
-m ../../scripts/Manifest.MF \
info/kgeorgiy/ja/alyokhin/implementor

cd ../../
rm -rf tmp
