package com.se2026.ims.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User implements Identifiable {
    private String id;
    private String name;
    private String password;
    private Role role;

    public User() {}

    @JsonCreator
    public User(@JsonProperty("id") String id, 
                @JsonProperty("name") String name, 
                @JsonProperty("password") String password, 
                @JsonProperty("role") Role role) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    @Override
    public String toString() {
        return "User{id='" + id + "', name='" + name + "', role=" + role + "}";
    }
}
