package com.releasescribe.service;

import java.util.List;

public class AiResult {

    private List<Group> groups;
    private String summary;

    public List<Group> getGroups() { return groups; }
    public void setGroups(List<Group> groups) { this.groups = groups; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public static class Group {
        private String type;
        private List<String> items;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public List<String> getItems() { return items; }
        public void setItems(List<String> items) { this.items = items; }
    }
}
