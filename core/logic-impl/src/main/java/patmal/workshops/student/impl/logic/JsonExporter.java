package patmal.workshops.student.impl.logic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Person;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.student.impl.model.StudentImpl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class JsonExporter {

    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    private record PersonDto(String id, String name, String email) {}
    private record TeamWithStudentsDto(Integer id, String name, List<PersonDto> students) {}
    private record WorkshopWithTeamsDto(Integer id, String name, String description, List<TeamWithStudentsDto> teams) {}
    private record InstructorExportDto(String id, String name, String email, List<WorkshopWithTeamsDto> workshops) {}
    private record WorkshopWithInstructorDto(Integer id, String name, String description, PersonDto instructor) {}
    private record TeamForStudentDto(Integer id, String name, List<String> studentIds, WorkshopWithInstructorDto workshop) {}
    private record StudentExportDto(String id, String name, String email, TeamForStudentDto team) {}
    private record TeamExportDto(Integer id, String name, List<PersonDto> students, WorkshopWithInstructorDto workshop) {}


    public void exportInstructors(File targetFile, Collection<Instructor> instructors, Collection<Team> allTeams) {
        List<InstructorExportDto> result = new ArrayList<>();
        for (Instructor instructor : instructors) {
            result.add(toInstructorDto(instructor, allTeams));
        }
        writeJsonToFile(targetFile, result);
    }

    public void exportStudents(File targetFile, Collection<Student> students) {
        List<StudentExportDto> result = new ArrayList<>();
        for (Student student : students) {
            result.add(toStudentDto(student));
        }
        writeJsonToFile(targetFile, result);
    }

    public void exportTeams(File targetFile, Collection<Team> teams) {
        List<TeamExportDto> result = new ArrayList<>();
        for (Team team : teams) {
            result.add(toTeamDto(team));
        }
        writeJsonToFile(targetFile, result);
    }

    private InstructorExportDto toInstructorDto(Instructor instructor, Collection<Team> allTeams) {
        List<WorkshopWithTeamsDto> workshopDtos = new ArrayList<>();
        for (Workshop workshop : instructor.getWorkshops()) {
            workshopDtos.add(toWorkshopWithTeamsDto(workshop, allTeams));
        }
        return new InstructorExportDto(
                instructor.getId(),
                instructor.getName(),
                instructor.getEmail(),
                workshopDtos
        );
    }

    private WorkshopWithTeamsDto toWorkshopWithTeamsDto(Workshop workshop, Collection<Team> allTeams) {
        List<TeamWithStudentsDto> teamDtos = new ArrayList<>();
        for (Team team : allTeams) {
            if (isTeamInWorkshop(team, workshop)) {
                teamDtos.add(new TeamWithStudentsDto(
                        team.getId(),
                        team.getName(),
                        toPersonDtos(team.getStudents())
                ));
            }
        }
        return new WorkshopWithTeamsDto(
                workshop.getId(),
                workshop.getName(),
                workshop.getDescription(),
                teamDtos
        );
    }

    private boolean isTeamInWorkshop(Team team, Workshop workshop) {
        return team.getWorkshop() != null && team.getWorkshop().getId().equals(workshop.getId());
    }

    private StudentExportDto toStudentDto(Student student) {
        Team team = null;
        if (student instanceof StudentImpl) {
            team = ((StudentImpl) student).getTeam();
        }

        TeamForStudentDto teamDto = null;
        if (team != null) {
            teamDto = new TeamForStudentDto(
                    team.getId(),
                    team.getName(),
                    extractStudentIds(team.getStudents()),
                    toWorkshopWithInstructorDto(team.getWorkshop())
            );
        }

        return new StudentExportDto(
                student.getId(),
                student.getName(),
                student.getEmail(),
                teamDto
        );
    }

    private TeamExportDto toTeamDto(Team team) {
        return new TeamExportDto(
                team.getId(),
                team.getName(),
                toPersonDtos(team.getStudents()),
                toWorkshopWithInstructorDto(team.getWorkshop())
        );
    }

    private WorkshopWithInstructorDto toWorkshopWithInstructorDto(Workshop workshop) {
        if (workshop == null) {
            return null;
        }

        PersonDto instructorDto = null;
        if (workshop.getInstructor() != null) {
            instructorDto = toPersonDto(workshop.getInstructor());
        }

        return new WorkshopWithInstructorDto(
                workshop.getId(),
                workshop.getName(),
                workshop.getDescription(),
                instructorDto
        );
    }

    private List<PersonDto> toPersonDtos(List<Student> students) {
        List<PersonDto> dtos = new ArrayList<>();
        for (Student student : students) {
            dtos.add(toPersonDto(student));
        }
        return dtos;
    }

    private List<String> extractStudentIds(List<Student> students) {
        List<String> ids = new ArrayList<>();
        for (Student student : students) {
            ids.add(student.getId());
        }
        return ids;
    }

    private PersonDto toPersonDto(Person person) {
        return new PersonDto(person.getId(), person.getName(), person.getEmail());
    }

    private void writeJsonToFile(File targetFile, Object data) {
        try (BufferedWriter writer = Files.newBufferedWriter(targetFile.toPath(), StandardCharsets.UTF_8)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write JSON to " + targetFile, e);
        }
    }
}