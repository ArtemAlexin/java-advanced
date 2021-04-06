/**
 * module with solutions for java advanced course.
 */
module java.solutions {
    requires java.compiler;
    requires java.rmi;
    requires junit;

    requires info.kgeorgiy.java.advanced.base;
    requires info.kgeorgiy.java.advanced.walk;
    requires info.kgeorgiy.java.advanced.arrayset;
    requires info.kgeorgiy.java.advanced.student;
    requires info.kgeorgiy.java.advanced.implementor;

    exports info.kgeorgiy.ja.alyokhin.walk;
    opens info.kgeorgiy.ja.alyokhin.walk;
    exports info.kgeorgiy.ja.alyokhin.arrayset;
    opens info.kgeorgiy.ja.alyokhin.arrayset;
    exports info.kgeorgiy.ja.alyokhin.student;
    opens info.kgeorgiy.ja.alyokhin.student;
    exports info.kgeorgiy.ja.alyokhin.implementor;
    opens info.kgeorgiy.ja.alyokhin.implementor;
}