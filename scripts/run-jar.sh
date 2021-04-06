#!/bin/bash
sh build-jar.sh
cd ../
java -jar artifacts/info.kgeorgiy.ja.alyokhin.implementor.jar \
    -jar info.kgeorgiy.java.advanced.implementor.Impler implementation.jar
rm -rf artifacts