package patmal.workshops.student.impl.model;

import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StudentImpl implements Student {

    private final String id;
    private final String name;
    private final String email;

    private Team team;

    public StudentImpl(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.team = null;
    }

    public Team getTeam() {return this.team; }

    public void setTeam(Team team) {
        this.team = team;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getEmail() {
        return this.email;
    }

    @Override
    public String getTeamName() {
        if (this.team == null) {
            return null;
        }
        return this.team.getName();
    }

    @Override
    public Instructor getInstructor() {
        if (this.team == null || this.team.getWorkshop() == null) {
            return null;
        }
        return this.team.getWorkshop().getInstructor();
    }

    @Override
    public List<Student> getTeamMates() {
        if (this.team == null) {
            return new ArrayList<>();
        }

        List<Student> teamMates = new ArrayList<>(this.team.getStudents());
        teamMates.remove(this);

        return teamMates;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof StudentImpl)) {
            return false;
        }

        StudentImpl otherStudent = (StudentImpl) other;
        return Objects.equals(this.id, otherStudent.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }
}