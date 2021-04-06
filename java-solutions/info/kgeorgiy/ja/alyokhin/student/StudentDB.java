package info.kgeorgiy.ja.alyokhin.student;

import info.kgeorgiy.java.advanced.student.*;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements AdvancedQuery {
    // :NOTE: extract other common comparators into constants
    private static final Comparator<Student> FULL_NAME_COMPARATOR = Comparator.
            comparing((Student::getLastName)).reversed().
            thenComparing(Comparator.comparing(Student::getFirstName).reversed())
            .thenComparing(Student::getId);
    // :NOTE: constants should be in capital
    private static final Comparator<GroupName> COMPARATOR_GROUP_NAME = Comparator.comparing(Enum::name);
    private static final Comparator<Group> GROUP_COMPARATOR = Comparator.comparing(Group::getName);

    private static <T extends Collection<?>> Comparator<T> getComparator() {
        return Comparator.comparing(T::size);
    }

    private static final Comparator<Set<GroupName>> SIZE_SET_COMPARATOR = getComparator();
    private static final Comparator<List<Student>> LIST_SIZE_COMPARATOR = getComparator();

    private <T extends Collection<Student>> Stream<Student> studentsStream(T students) {
        return students.stream();
    }

    private String getFullName(Student student) {
        return student.getFirstName() + " " + student.getLastName();
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

    private <T, R> Stream<Map.Entry<R, T>> getEntrySetStream(Stream<Student> students,
                                                             Function<Student, R> function,
                                                             Collector<Student, ?, T> collector) {
        return students.collect(Collectors.groupingBy(function, collector)).entrySet().stream();
    }

    private <T> Stream<Map.Entry<GroupName, T>> getEntrySetStream(Stream<Student> students, Collector<Student, ?, T> collector) {
        return getEntrySetStream(students, Student::getGroup, collector);
    }

    private Stream<Map.Entry<GroupName, List<Student>>> getEntrySetStream(Stream<Student> students) {
        return getEntrySetStream(students, Collectors.toList());
    }

    private List<Group> toGroupList(Collection<Student> students, Comparator<Student> comparator) {
        return getEntrySetStream(studentsStream(students).sorted(comparator))
                .map(x -> new Group(x.getKey(), x.getValue()))
                .sorted(GROUP_COMPARATOR)
                .collect(Collectors.toList());
    }

    private <R, T> R getLargestObjectByComparator(Stream<Map.Entry<R, T>> entryStream,
                                                  Comparator<R> comparatorR, Comparator<T> comparatorT, R zeroVal) {
        return entryStream.max(Map.Entry.<R, T>comparingByValue(comparatorT)
                .thenComparing(Map.Entry::getKey, comparatorR))
                .map(Map.Entry::getKey)
                .orElse(zeroVal);
    }

    private <R, T> R getLargestObjectByComparator(Stream<Map.Entry<R, T>> entryStream,
                                                  Comparator<R> comparatorR, Comparator<T> comparatorT) {
        return getLargestObjectByComparator(entryStream, comparatorR, comparatorT, null);
    }

    private <T> List<T> getIndicesStudent(Collection<Student> students,
                                          int[] indices, Function<Student, T> f) {
        return Arrays.stream(indices).mapToObj(List.copyOf(students)::get)
                .map(f).collect(Collectors.toList());
    }

    @Override
    public String getMostPopularName(Collection<Student> students) {
        return getLargestObjectByComparator(
                getEntrySetStream(
                        studentsStream(students),
                        Student::getFirstName,
                        Collectors.mapping(Student::getGroup, Collectors.toSet())
                ),
                Comparator.naturalOrder(),
                SIZE_SET_COMPARATOR,
                "");
    }

    @Override
    public List<String> getFirstNames(Collection<Student> students, int[] indices) {
        return getIndicesStudent(students, indices, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(Collection<Student> students, int[] indices) {
        return getIndicesStudent(students, indices, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(Collection<Student> students, int[] indices) {
        return getIndicesStudent(students, indices, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(Collection<Student> students, int[] indices) {
        return getIndicesStudent(students, indices, this::getFullName);
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
        return this.getLargestObjectByComparator(getEntrySetStream(studentsStream(students)),
                COMPARATOR_GROUP_NAME,
                LIST_SIZE_COMPARATOR);
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return this.getLargestObjectByComparator(getEntrySetStream(studentsStream(students),
                Collectors.collectingAndThen(Collectors.mapping(Student::getFirstName, Collectors.toSet()), Set::size)),
                COMPARATOR_GROUP_NAME.reversed(),
                Integer::compareTo);
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
        return mapToList(students, this::getFullName);
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
