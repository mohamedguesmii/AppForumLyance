package tn.esprit.devoir.dto;

public class TaskDto {
    private String id;
    private String name;
    private String assignee;
    private String created;
    private String processInstanceId;

    public TaskDto() {
    }

    public TaskDto(String id, String name, String assignee, String created, String processInstanceId) {
        this.id = id;
        this.name = name;
        this.assignee = assignee;
        this.created = created;
        this.processInstanceId = processInstanceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
}
