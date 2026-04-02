package com.skywings.util;

/**
 * Sanitizes user input to prevent XSS attacks.
 * Strips HTML tags and encodes dangerous characters.
 */
public final class InputSanitizer {

    private InputSanitizer() {}

    public static String sanitize(String input) {
        if (input == null) return null;
        return input
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&#x27;")
            .replaceAll("&(?!\\w+;)", "&amp;")
            .replaceAll("<script[^>]*>.*?</script>", "")
            .trim();
    }

    public static String stripHtml(String input) {
        if (input == null) return null;
        return input.replaceAll("<[^>]*>", "").trim();
    }
}
