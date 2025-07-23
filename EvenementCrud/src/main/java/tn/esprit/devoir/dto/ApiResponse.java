package tn.esprit.devoir.dto;

public class ApiResponse {
    private boolean success;
    private String message;

    public ApiResponse() {
        // Constructeur vide requis par Jackson
    }

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    // Setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
