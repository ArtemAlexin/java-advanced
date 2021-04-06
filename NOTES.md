Updating repository __repos/Alekhin_Artem
From https://www.kgeorgiy.info/git-students/year2019//Alekhin_Artem/java-advanced
   4913de5..f1bf08c  master     -> origin/master
Updating 4913de5..f1bf08c
Fast-forward
 NOTES.md                                           | 863 ---------------------
 .../kgeorgiy/ja/alyokhin/arrayset/ArraySet.java    | 378 ++++-----
 .../ja/alyokhin/arrayset/ListWithOrder.java        |  96 +--
 .../implementor/AbstractExecutableGenerator.java   |  15 +-
 .../ja/alyokhin/implementor/Formatter.java         |   9 +
 .../implementor/GenericTypeGeneratorUtils.java     | 156 ++++
 .../ja/alyokhin/implementor/Implementor.java       | 100 ++-
 .../implementor/InvalidClassNameException.java     |  16 +
 .../ja/alyokhin/implementor/MethodGenerator.java   |  11 +-
 .../implementor/UnicodeBufferedWriter.java         |  64 ++
 .../kgeorgiy/ja/alyokhin/implementor/Utils.java    |   2 +-
 .../ja/alyokhin/implementor/VisitorCleaner.java    |   1 -
 .../generic/GenericTypeGeneratorUtils.java         | 156 ++++
 .../generic/InvalidClassNameException.java         |  16 +
 .../ja/alyokhin/implementor/package-info.java      |   2 +-
 .../ja/alyokhin/implementor/scripts/build-jar.sh   |  29 -
 .../ja/alyokhin/implementor/scripts/compile-doc.sh |  23 -
 .../kgeorgiy/ja/alyokhin/student/StudentDB.java    | 451 ++++++-----
 java-solutions/module-info.java                    |  23 +
 scripts/Manifest.MF                                |   3 +
 scripts/build-jar.ps1                              |  27 +
 scripts/build-jar.sh                               |  26 +
 scripts/generate-doc.ps1                           |  17 +
 scripts/generate-doc.sh                            |  15 +
 scripts/run-jar.ps1                                |   6 +
 scripts/run-jar.sh                                 |   6 +
 26 files changed, 1091 insertions(+), 1420 deletions(-)
 delete mode 100644 NOTES.md
 create mode 100644 java-solutions/info/kgeorgiy/ja/alyokhin/implementor/GenericTypeGeneratorUtils.java
 create mode 100644 java-solutions/info/kgeorgiy/ja/alyokhin/implementor/InvalidClassNameException.java
 create mode 100644 java-solutions/info/kgeorgiy/ja/alyokhin/implementor/UnicodeBufferedWriter.java
 create mode 100644 java-solutions/info/kgeorgiy/ja/alyokhin/implementor/generic/GenericTypeGeneratorUtils.java
 create mode 100644 java-solutions/info/kgeorgiy/ja/alyokhin/implementor/generic/InvalidClassNameException.java
 delete mode 100644 java-solutions/info/kgeorgiy/ja/alyokhin/implementor/scripts/build-jar.sh
 delete mode 100644 java-solutions/info/kgeorgiy/ja/alyokhin/implementor/scripts/compile-doc.sh
 create mode 100644 java-solutions/module-info.java
 create mode 100644 scripts/Manifest.MF
 create mode 100644 scripts/build-jar.ps1
 create mode 100644 scripts/build-jar.sh
 create mode 100644 scripts/generate-doc.ps1
 create mode 100644 scripts/generate-doc.sh
 create mode 100644 scripts/run-jar.ps1
 create mode 100644 scripts/run-jar.sh
Compiling 22 files
__current-repo\java-solutions\info\kgeorgiy\ja\alyokhin\implementor\GenericTypeGeneratorUtils.java:16: error: duplicate class: info.kgeorgiy.ja.alyokhin.implementor.generic.GenericTypeGeneratorUtils
public class GenericTypeGeneratorUtils {
       ^
__current-repo\java-solutions\info\kgeorgiy\ja\alyokhin\implementor\InvalidClassNameException.java:6: error: duplicate class: info.kgeorgiy.ja.alyokhin.implementor.generic.InvalidClassNameException
public class InvalidClassNameException extends RuntimeException {
       ^
2 errors
ERROR: Compilation failed
