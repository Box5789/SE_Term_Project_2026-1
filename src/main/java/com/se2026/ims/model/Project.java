package com.se2026.ims.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Project implements Identifiable {
    private String id;
    private String name;
    private String description;

    public Project() {}

    @JsonCreator
    public Project(@JsonProperty("id") String id, 
                   @JsonProperty("name") String name, 
                   @JsonProperty("description") String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "Project{id='" + id + "', name='" + name + "'}";
    }
}
