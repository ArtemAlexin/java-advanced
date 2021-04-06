cd ../
mkdir -Force java.solutions
cp -Force -Recurse java-solutions\*  java.solutions
$EXTERN_MODULE = "../java-advanced-2021"
$TO_COMPILE = "${EXTERN_MODULE}/modules//info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor"
$MY_MODULE = "java.solutions/info/kgeorgiy/ja/alyokhin/implementor"
javadoc -d doc -private `
-link https://docs.oracle.com/en/java/javase/11/docs/api/ `
--module-path "$EXTERN_MODULE\lib;$EXTERN_MODULE\artifacts" `
--module-source-path "$EXTERN_MODULE\modules;." `
"$MY_MODULE\generic\*.java" `
"$MY_MODULE\*.java" `
"$TO_COMPILE\Impler.java" `
"$TO_COMPILE\JarImpler.java" `
"$TO_COMPILE\ImplerException.java"

rm -Recurse java.solutions