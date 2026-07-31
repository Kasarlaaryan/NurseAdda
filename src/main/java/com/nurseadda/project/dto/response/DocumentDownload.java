package com.nurseadda.project.dto.response;

import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;

public record DocumentDownload(
        byte[] data,
        String contentType,
        String fileName
) {

    public MediaType mediaType() {
        if (contentType == null) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (InvalidMediaTypeException ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    /**
     * Returns a header-safe version of the file name, stripping characters
     * that could be used for HTTP header injection (quotes, CR/LF, etc.).
     */
    public String safeFileName() {
        if (fileName == null) {
            return "document";
        }
        String sanitized = fileName
                .replaceAll("[\r\n\"]", "")
                .replaceAll("[\\/:*?<>|]", "_")
                .trim();
        return sanitized.isBlank() ? "document" : sanitized;
    }
}
