cd ../
mkdir -Force java.solutions
$EXTERN_MODULE = "../java-advanced-2021"
$TO_COMPILE = "${EXTERN_MODULE}/modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor"
$MY_MODULE = "java.solutions/info/kgeorgiy/ja/alyokhin/implementor"
$MODULE_NAME = "java.solutions"

cp -Force -Recurse java-solutions\* "$MODULE_NAME"

javac -d tmp `
--module-path "$EXTERN_MODULE\lib;$EXTERN_MODULE\artifacts" `
--module-source-path "$EXTERN_MODULE\modules;." `
--module "$MODULE_NAME"

rm -Recurse "$MODULE_NAME"

New-Item -ItemType Directory -Force -Path artifacts

cd "tmp\$MODULE_NAME"

jar -c -f ..\..\artifacts\info.kgeorgiy.ja.alyokhin.jar `
-m ..\..\scripts\MANIFEST.MF `
info\kgeorgiy\ja\alyokhin\implementor

cd ..\..\

rm -Recurse tmp