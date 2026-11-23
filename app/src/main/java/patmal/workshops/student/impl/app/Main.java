package patmal.workshops.student.impl.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.student.impl.logic.WorkshopSystemImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        LOGGER.info("Starting Workshop Registration CLI demo...");

        WorkshopSystem system = new WorkshopSystemImpl();

        Instructor dana = system.createInstructor("i1", "Dana Levi", "dana.levi@univ.edu");

        Workshop javaWorkshop = system.createWorkshop("Java Workshop", "Maven and multi-module architecture");
        Workshop springWorkshop = system.createWorkshop("Spring Workshop", "Introduction to Spring Boot");

        system.associateWorkshopWithInstructor(dana, javaWorkshop);
        system.associateWorkshopWithInstructor(dana, springWorkshop);

        File studentsCsv = resolveStudentsCsv(args);
        system.importStudentDataFromFile(studentsCsv);

        system.createTeam("Alpha", javaWorkshop, List.of("s1", "s2"));

        File instructorsFile = new File("instructors.json");
        File studentsFile = new File("students.json");
        File teamsFile = new File("teams.json");

        system.exportInstructorsDataToJson(instructorsFile);
        system.exportStudentsDataToJson(studentsFile);
        system.exportTeamsDataToJson(teamsFile);

        LOGGER.info("Exported files to current directory: {}, {}, {}",
                instructorsFile.getAbsolutePath(),
                studentsFile.getAbsolutePath(),
                teamsFile.getAbsolutePath());
    }

    private static File resolveStudentsCsv(String[] args) throws IOException {
        if (args.length > 0) {
            return new File(args[0]);
        }

        try (InputStream resourceStream = Main.class.getResourceAsStream("/students.csv")) {
            if (resourceStream == null) {
                throw new IllegalStateException("Default /students.csv not found in resources");
            }
            File tempFile = File.createTempFile("students-", ".csv");
            tempFile.deleteOnExit();
            Files.copy(resourceStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return tempFile;
        }
    }
}