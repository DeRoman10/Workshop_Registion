package patmal.workshops.logic;

import patmal.workshops.data.InstructorImpl;
import patmal.workshops.data.StudentImpl;
import patmal.workshops.data.TeamImpl;
import patmal.workshops.data.WorkshopImpl;
import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WorkshopSystemImpl implements WorkshopSystem {

    private final Map<String, Instructor> instructors;
    private final Map<String, Student> students;
    private final Map<Integer, Workshop> workshops;
    private final Map<Integer, Team> teams;

    private int nextWorkshopId;
    private int nextTeamId;

    public WorkshopSystemImpl() {
        this.instructors = new HashMap<>();
        this.students = new HashMap<>();
        this.workshops = new HashMap<>();
        this.teams = new HashMap<>();
        this.nextWorkshopId = 1;
        this.nextTeamId = 1;
    }

    @Override
    public Instructor createInstructor(String id, String name, String email) {
        if (this.instructors.containsKey(id) || this.students.containsKey(id)) {
            throw new IllegalArgumentException("ID already exists: " + id);
        }
        Instructor instructor = new InstructorImpl(id, name, email);
        this.instructors.put(id, instructor);
        return instructor;
    }

    @Override
    public Student createStudent(String id, String name, String email) {
        if (this.students.containsKey(id) || this.instructors.containsKey(id)) {
            throw new IllegalArgumentException("ID already exists: " + id);
        }
        Student student = new StudentImpl(id, name, email);
        this.students.put(id, student);
        return student;
    }

    @Override
    public Workshop createWorkshop(String name, String description) {
        int id = this.nextWorkshopId++;
        Workshop workshop = new WorkshopImpl(id, name, description);
        this.workshops.put(id, workshop);
        return workshop;
    }

    @Override
    public void associateWorkshopWithInstructor(Instructor instructor, Workshop workshop) {
        if (instructor == null || workshop == null) {
            throw new IllegalArgumentException("Instructor and Workshop cannot be null");
        }

        InstructorImpl instructorImpl = (InstructorImpl) this.instructors.get(instructor.getId());
        WorkshopImpl workshopImpl = (WorkshopImpl) this.workshops.get(workshop.getId());

        if (instructorImpl != null && workshopImpl != null) {
            workshopImpl.setInstructor(instructorImpl);
            instructorImpl.addWorkshop(workshopImpl);
        }
    }

    @Override
    public Team createTeam(String name, Workshop workshop, List<String> members) {
        Set<String> uniqueMembers = new HashSet<>();
        List<StudentImpl> validStudents = new ArrayList<>();

        for (String studentId : members) {
            if (!this.students.containsKey(studentId)) {
                throw new IllegalArgumentException("Student ID does not exist: " + studentId);
            }
            if (!uniqueMembers.add(studentId)) {
                throw new IllegalArgumentException("Duplicate student in team request: " + studentId);
            }

            StudentImpl studentImpl = (StudentImpl) this.students.get(studentId);
            if (studentImpl.getTeamName() != null) {
                throw new IllegalArgumentException("Student is already in a team: " + studentId);
            }

            validStudents.add(studentImpl);
        }

        int id = this.nextTeamId++;
        TeamImpl team = new TeamImpl(id, name, workshop);

        for (StudentImpl student : validStudents) {
            team.addStudent(student);
            student.setTeam(team);
        }

        this.teams.put(id, team);
        return team;
    }

    @Override
    public List<Instructor> getInstructors() {
        return new ArrayList<>(this.instructors.values());
    }

    @Override
    public Instructor getInstructor(String id) {
        return this.instructors.get(id);
    }

    @Override
    public List<Student> getStudents() {
        return new ArrayList<>(this.students.values());
    }

    @Override
    public Student getStudent(String id) {
        return this.students.get(id);
    }

    @Override
    public List<Workshop> getWorkshops() {
        return new ArrayList<>(this.workshops.values());
    }

    @Override
    public Workshop getWorkshop(Integer id) {
        return this.workshops.get(id);
    }

    @Override
    public List<Team> getTeams() {
        return new ArrayList<>(this.teams.values());
    }

    @Override
    public Team getTeam(Integer id) {
        return this.teams.get(id);
    }

    @Override
    public List<Student> getFreeStudents() {
        return this.students.values().stream()
                .filter(student -> student.getTeamName() == null)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> getStudentsOfInstructor(String instructorId) {
        if (!this.instructors.containsKey(instructorId)) {
            return new ArrayList<>();
        }

        return this.students.values().stream()
                .filter(student -> {
                    Instructor instructor = student.getInstructor();
                    return instructor != null && instructor.getId().equals(instructorId);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> getStudentsOfWorkshop(Integer workshopId) {
        if (!this.workshops.containsKey(workshopId)) {
            return new ArrayList<>();
        }

        List<Student> result = new ArrayList<>();
        for (Team team : this.teams.values()) {
            if (team.getWorkshop().getId().equals(workshopId)) {
                result.addAll(team.getStudents());
            }
        }
        return result;
    }
}
