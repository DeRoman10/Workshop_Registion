package patmal.workshops.student.impl.model;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Workshop;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InstructorImpl implements Instructor {

    private final String id;
    private final String name;
    private final String email;
    private final List<Workshop> workshops;

    public InstructorImpl(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.workshops = new ArrayList<>();
    }

    public void addWorkshop(Workshop workshop) {
        if (!this.workshops.contains(workshop)) {
            this.workshops.add(workshop);
        }
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
    public List<Workshop> getWorkshops() {
        return new ArrayList<>(this.workshops);
    }

    public void removeWorkshop(Workshop workshop) {
        this.workshops.remove(workshop);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof InstructorImpl)) {
            return false;
        }

        InstructorImpl otherInstructor = (InstructorImpl) other;
        return Objects.equals(this.id, otherInstructor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }
}