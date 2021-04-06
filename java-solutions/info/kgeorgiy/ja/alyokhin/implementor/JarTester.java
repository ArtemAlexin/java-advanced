package info.kgeorgiy.ja.alyokhin.implementor;

import info.kgeorgiy.java.advanced.implementor.*;

public class JarTester extends Tester {
    public static void main(final String... args) {
        new Tester()
                .add("interface", InterfaceImplementorTest.class)
                .add("class", ClassImplementorTest.class)
                .add("advanced", AdvancedImplementorTest.class)
                .add("covariant", CovariantImplementorTest.class)
                .add("jar-interface", InterfaceJarImplementorTest.class)
                .add("jar-class", ClassJarImplementorTest.class)
                .add("jar-advanced", AdvancedJarImplementorTest.class)
                .run(args);
    }
}
