package com.example.ldap;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private String name;
    private List<String> uniqueMembers = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getUniqueMembers() {
        return uniqueMembers;
    }

    public void addUniqueMember(String uniqueMember) {
        this.uniqueMembers.add(uniqueMember);
    }
}
