package com.studentrisk.backend.dto;

import java.util.List;

public record BatchImportResponse(
        boolean success,
        String message,
        int totalRows,
        int insertedOrUpdated,
        int skipped,
        List<String> errors
) {
    public static BatchImportResponse of(
            boolean success,
            String message,
            int totalRows,
            int insertedOrUpdated,
            int skipped,
            List<String> errors
    ) {
        return new BatchImportResponse(success, message, totalRows, insertedOrUpdated, skipped, errors != null ? errors : List.of());
    }
}
