powershell ./build-jar.ps1
cd ../
java -jar artifacts\info.kgeorgiy.ja.alyokhin.jar `
-jar info.kgeorgiy.java.advanced.implementor.ImplerException implementation.jar

rm -Recurse artifacts