package patmal.workshops.student.impl.model;

import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Workshop;
import java.util.Objects;

public class WorkshopImpl implements Workshop {

    private final Integer id;
    private final String name;
    private final String description;
    private Instructor instructor;

    public WorkshopImpl(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.instructor = null;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
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
    public String getDescription() {
        return this.description;
    }

    @Override
    public Instructor getInstructor() {
        return this.instructor;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WorkshopImpl)) {
            return false;
        }

        WorkshopImpl otherWorkshop = (WorkshopImpl) other;
        return Objects.equals(this.id, otherWorkshop.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

}
