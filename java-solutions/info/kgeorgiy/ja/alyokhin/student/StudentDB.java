package info.kgeorgiy.java.advanced.alyokhin.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.GroupQuery;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements GroupQuery {
    private static final Comparator<Student> FULL_NAME_COMPARATOR = Comparator.
            comparing((Student::getLastName)).reversed().
            thenComparing(Comparator.comparing(Student::getFirstName).reversed())
            .thenComparing(Student::getId);

    private <T extends Collection<Student>> Stream<Student> studentsStream(T students) {
        return students.stream();
    }

    private <T> List<T> mapToList(Collection<Student> students, Function<Student, T> mapper) {
        return mappedCollection(students, mapper).collect(Collectors.toList());
    }

    private <T> Set<T> mapToSet(Collection<Student> students, Function<Student, T> mapper) {
        return mappedCollection(students, mapper).collect(Collectors.toCollection(TreeSet::new));
    }

    private <T> Stream<T> mappedCollection(Collection<Student> students, Function<Student, T> mapper) {
        return studentsStream(students).map(mapper);
    }

    private List<Student> getSortedStudentList(Collection<Student> students, Comparator<Student> comparator) {
        return studentsStream(students).sorted(comparator).collect(Collectors.toList());
    }

    private List<Student> getFilteredStudentList(Collection<Student> students, Predicate<Student> predicate) {
        return studentsStream(students).filter(predicate).
                sorted(FULL_NAME_COMPARATOR).collect(Collectors.toList());
    }

    private <T> Predicate<Student> getPredicateByFunction(Function<Student, T> function, T object) {
        return student -> Objects.equals(function.apply(student), object);
    }

    private <T> Stream<Map.Entry<GroupName, T>> getEntrySetStream(Stream<Student> students, Collector<Student, ?, T> collector) {
        return students.collect(Collectors.groupingBy(Student::getGroup, collector)).entrySet().stream();
    }

    private Stream<Map.Entry<GroupName, List<Student>>> getEntrySetStream(Stream<Student> students) {
        return getEntrySetStream(students, Collectors.toList());
    }

    private List<Group> toGroupList(Collection<Student> students, Comparator<Student> comparator) {
        return getEntrySetStream(studentsStream(students).sorted(comparator))
                .map(x -> new Group(x.getKey(), x.getValue()))
                .sorted(Comparator.comparing(Group::getName))
                .collect(Collectors.toList());
    }

    private <T> GroupName getLargestGroupByComparator(Stream<Map.Entry<GroupName, T>> entryStream,
                                                      Comparator<T> comparator, Comparator<GroupName> comparator2) {
        return entryStream.max(Map.Entry.<GroupName, T>comparingByValue(comparator)
                .thenComparing(Map.Entry::getKey, comparator2))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return toGroupList(students, FULL_NAME_COMPARATOR);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return toGroupList(students, Comparator.naturalOrder());
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return getLargestGroupByComparator(getEntrySetStream(studentsStream(students)),
                Comparator.comparing(List::size),
                Comparator.naturalOrder());
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroupByComparator(getEntrySetStream(studentsStream(students),
                Collectors.collectingAndThen(Collectors.mapping(Student::getFirstName, Collectors.toSet()), Set::size)),
                Integer::compareTo,
                Comparator.reverseOrder());
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return mapToList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return mapToList(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return mapToList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return mapToList(students, x -> x.getFirstName() + " " + x.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return mapToSet(students, Student::getFirstName);
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return studentsStream(students).max(Comparator.naturalOrder()).
                map(Student::getFirstName).orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return getSortedStudentList(students, Comparator.naturalOrder());
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return getSortedStudentList(students, FULL_NAME_COMPARATOR);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return getFilteredStudentList(students, getPredicateByFunction(Student::getFirstName, name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return getFilteredStudentList(students, getPredicateByFunction(Student::getLastName, name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return getFilteredStudentList(students, getPredicateByFunction(Student::getGroup, group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return studentsStream(students).filter(x -> x.getGroup().equals(group)).collect(Collectors.toMap(
                Student::getLastName,
                Student::getFirstName,
                BinaryOperator.minBy(String::compareTo)
        ));
    }
}
