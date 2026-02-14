package com.investments.tracker.infrastructure.web.dto;

public record ValidationError(String field, String message, Object rejectedValue) {}
