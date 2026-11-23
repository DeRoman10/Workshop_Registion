package patmal.workshops.student.impl.model;

import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TeamImpl implements Team {

    private final Integer id;
    private final String name;
    private final Workshop workshop;
    private final List<Student> students;

    public TeamImpl(Integer id, String name, Workshop workshop) {
        this.id = id;
        this.name = name;
        this.workshop = workshop;
        this.students = new ArrayList<>();
    }

    public void addStudent(Student student) {
        if (!this.students.contains(student)) {
            this.students.add(student);
        }
    }

    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<Student> getStudents() {
        return new ArrayList<>(this.students);
    }

    @Override
    public Workshop getWorkshop() {
        return this.workshop;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TeamImpl)) {
            return false;
        }

        TeamImpl otherTeam = (TeamImpl) other;
        return Objects.equals(this.id, otherTeam.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }
}
