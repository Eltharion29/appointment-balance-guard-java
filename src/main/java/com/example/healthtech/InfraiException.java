package com.example.healthtech;

public final class InfraiException extends RuntimeException {
    private final String code;
    private final int status;

    public InfraiException(String code, int status) {
        super(code + " (HTTP " + status + ")");
        this.code = code;
        this.status = status;
    }

    public String code() { return code; }
    public int status() { return status; }
}
