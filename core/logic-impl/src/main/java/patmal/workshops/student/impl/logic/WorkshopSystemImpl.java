package patmal.workshops.student.impl.logic;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.student.impl.model.InstructorImpl;
import patmal.workshops.student.impl.model.StudentImpl;
import patmal.workshops.student.impl.model.TeamImpl;
import patmal.workshops.student.impl.model.WorkshopImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;

public class WorkshopSystemImpl implements WorkshopSystem {

    private static final Logger LOGGER = LogManager.getLogger(WorkshopSystemImpl.class);
    private static final EmailValidator EMAIL_VALIDATOR = EmailValidator.getInstance();

    private final Map<String, Instructor> instructors;
    private final Map<String, Student> students;
    private final Map<Integer, Workshop> workshops;
    private final Map<Integer, Team> teams;
    private final JsonExporter jsonExporter;

    private int nextWorkshopId;
    private int nextTeamId;

    public WorkshopSystemImpl() {
        this.instructors = new LinkedHashMap<>();
        this.students = new LinkedHashMap<>();
        this.workshops = new LinkedHashMap<>();
        this.teams = new LinkedHashMap<>();
        this.jsonExporter = new JsonExporter();
        this.nextWorkshopId = 1;
        this.nextTeamId = 1;
    }

    private void validateEmail(String email) {
        if (email == null || !EMAIL_VALIDATOR.isValid(email)) {
            LOGGER.warn("Rejected invalid email address: {}", email);
            throw new IllegalArgumentException("Invalid email address: " + email);
        }
    }

    @Override
    public Instructor createInstructor(String id, String name, String email) {
        validateEmail(email);
        if (this.instructors.containsKey(id) || this.students.containsKey(id)) {
            LOGGER.warn("Rejected instructor creation - ID already exists: {}", id);
            throw new IllegalArgumentException("ID already exists: " + id);
        }
        Instructor instructor = new InstructorImpl(id, name, email);
        this.instructors.put(id, instructor);
        LOGGER.info("Created instructor: id={}, name={}", id, name);
        return instructor;
    }

    @Override
    public Student createStudent(String id, String name, String email) {
        validateEmail(email);
        if (this.students.containsKey(id) || this.instructors.containsKey(id)) {
            LOGGER.warn("Rejected student creation - ID already exists: {}", id);
            throw new IllegalArgumentException("ID already exists: " + id);
        }
        Student student = new StudentImpl(id, name, email);
        this.students.put(id, student);
        LOGGER.info("Created student: id={}, name={}", id, name);
        return student;
    }

    @Override
    public Workshop createWorkshop(String name, String description) {
        int id = this.nextWorkshopId++;
        Workshop workshop = new WorkshopImpl(id, name, description);
        this.workshops.put(id, workshop);
        LOGGER.info("Created workshop: id={}, name={}", id, name);
        return workshop;
    }

    @Override
    public void associateWorkshopWithInstructor(Instructor instructor, Workshop workshop) {
        if (instructor == null || workshop == null) {
            LOGGER.warn("Rejected association - instructor or workshop is null");
            throw new IllegalArgumentException("Instructor and Workshop cannot be null");
        }

        InstructorImpl instructorImpl = (InstructorImpl) this.instructors.get(instructor.getId());
        WorkshopImpl workshopImpl = (WorkshopImpl) this.workshops.get(workshop.getId());

        if (instructorImpl == null || workshopImpl == null) {
            LOGGER.warn("Rejected association - instructor or workshop not found in system");
            throw new IllegalArgumentException("Instructor or Workshop does not exist in the system");
        }

        Instructor previousInstructor = workshopImpl.getInstructor();
        if (previousInstructor instanceof InstructorImpl previousImpl) {
            previousImpl.removeWorkshop(workshopImpl);
        }

        workshopImpl.setInstructor(instructorImpl);
        instructorImpl.addWorkshop(workshopImpl);
        LOGGER.info("Associated workshop {} with instructor {}", workshop.getId(), instructor.getId());
    }

    @Override
    public Team createTeam(String name, Workshop workshop, List<String> members) {
        if (workshop == null || !this.workshops.containsKey(workshop.getId())) {
            LOGGER.warn("Rejected team creation - workshop is null or does not exist");
            throw new IllegalArgumentException("Workshop must exist in the system");
        }
        if (members == null) {
            LOGGER.warn("Rejected team creation - members list is null");
            throw new IllegalArgumentException("Members list cannot be null");
        }

        Set<String> uniqueMembers = new HashSet<>();
        List<StudentImpl> validStudents = new ArrayList<>();

        for (String studentId : members) {
            if (!this.students.containsKey(studentId)) {
                LOGGER.warn("Rejected team creation - student ID does not exist: {}", studentId);
                throw new IllegalArgumentException("Student ID does not exist: " + studentId);
            }
            if (!uniqueMembers.add(studentId)) {
                LOGGER.warn("Rejected team creation - duplicate student ID: {}", studentId);
                throw new IllegalArgumentException("Duplicate student in team request: " + studentId);
            }

            StudentImpl studentImpl = (StudentImpl) this.students.get(studentId);
            if (studentImpl.getTeamName() != null) {
                LOGGER.warn("Rejected team creation - student already in a team: {}", studentId);
                throw new IllegalArgumentException("Student is already in a team: " + studentId);
            }

            validStudents.add(studentImpl);
        }

        int id = this.nextTeamId++;
        Workshop storedWorkshop = this.workshops.get(workshop.getId());
        TeamImpl team = new TeamImpl(id, name, storedWorkshop);

        for (StudentImpl student : validStudents) {
            team.addStudent(student);
            student.setTeam(team);
        }

        this.teams.put(id, team);
        LOGGER.info("Created team: id={}, name={}, members={}", id, name, members.size());
        return team;
    }

    @Override
    public void exportInstructorsDataToJson(File targetFileName) {
        LOGGER.info("Exporting instructors data to {}", targetFileName);
        this.jsonExporter.exportInstructors(targetFileName, this.instructors.values(), this.teams.values());
    }

    @Override
    public void exportStudentsDataToJson(File targetFileName) {
        LOGGER.info("Exporting students data to {}", targetFileName);
        this.jsonExporter.exportStudents(targetFileName, this.students.values());
    }

    @Override
    public void exportTeamsDataToJson(File targetFileName) {
        LOGGER.info("Exporting teams data to {}", targetFileName);
        this.jsonExporter.exportTeams(targetFileName, this.teams.values());
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

    @Override
    public List<Student> importStudentDataFromFile(File source) {
        if (source == null) {
            LOGGER.warn("Rejected CSV import - source file is null");
            throw new IllegalArgumentException("Source file cannot be null");
        }

        LOGGER.info("Importing students from CSV file: {}", source);
        List<Student> importedStudents = new ArrayList<>();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setIgnoreEmptyLines(true)
                .build();

        try (BufferedReader reader = Files.newBufferedReader(source.toPath(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, format)) {

            Iterator<CSVRecord> iterator = parser.iterator();

            if (!iterator.hasNext()) {
                LOGGER.warn("Rejected CSV import - file is empty (missing header)");
                throw new IllegalArgumentException("CSV file is empty or missing header");
            }

            CSVRecord header = iterator.next();
            if (header.size() != 3
                    || !"id".equals(header.get(0))
                    || !"name".equals(header.get(1))
                    || !"email".equals(header.get(2))) {
                LOGGER.warn("Rejected CSV import - invalid header: {}", header);
                throw new IllegalArgumentException("Invalid CSV header. Expected: id,name,email");
            }

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();
                if (record.size() != 3) {
                    LOGGER.warn("Rejected CSV import - wrong number of columns at line {}: {}",
                            record.getRecordNumber(), record.size());
                    throw new IllegalArgumentException("Invalid column count at line " + record.getRecordNumber());
                }

                Student student = createStudent(record.get(0), record.get(1), record.get(2));
                importedStudents.add(student);
            }

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read CSV file: " + source, e);
        }

        LOGGER.info("Successfully imported {} students from {}", importedStudents.size(), source);
        return importedStudents;
    }
}