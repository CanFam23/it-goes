package com.it_goes.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.it_goes.api.util.exception.AccountRequestException;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Set;

final class TripBody {
    private static final Set<String> TYPES = Set.of("paragraph", "heading", "quote", "list", "listitem", "link", "text", "linebreak");
    private int nodes;
    private final StringBuilder text = new StringBuilder();

    static String validateAndSummarize(JsonNode body) {
        if (body == null || body.toString().getBytes(StandardCharsets.UTF_8).length > 100_000) bad();
        JsonNode root = body.path("root");
        if (!"root".equals(root.path("type").asText()) || !root.path("children").isArray()) bad();
        TripBody validator = new TripBody();
        for (JsonNode child : root.path("children")) validator.visit(child, 1);
        String summary = validator.text.toString().strip();
        if (summary.isEmpty()) bad();
        return summary;
    }

    private void visit(JsonNode node, int depth) {
        if (++nodes > 2000 || depth > 12 || !node.isObject()) bad();
        String type = node.path("type").asText();
        if (!TYPES.contains(type)) bad();
        if (type.equals("text")) {
            if (!node.path("text").isTextual()) bad();
            JsonNode format = node.path("format");
            if (!format.isMissingNode() && (!format.isInt() || (format.intValue() & ~3) != 0)) bad();
            text.append(node.path("text").asText());
            if (text.length() > 20_000) bad();
            return;
        }
        if (type.equals("linebreak")) {
            text.append('\n');
            return;
        }
        if (type.equals("heading") && !Set.of("h2", "h3").contains(node.path("tag").asText())) bad();
        if (type.equals("list") && !Set.of("bullet", "number").contains(node.path("listType").asText())) bad();
        if (type.equals("link")) {
            try {
                URI uri = URI.create(node.path("url").asText());
                if (uri.getScheme() == null || !Set.of("http", "https").contains(uri.getScheme().toLowerCase())
                        || uri.getHost() == null) bad();
            } catch (IllegalArgumentException ex) {
                bad();
            }
        }
        JsonNode children = node.path("children");
        if (!children.isArray()) bad();
        for (JsonNode child : children) visit(child, depth + 1);
        if (Set.of("paragraph", "heading", "quote", "listitem").contains(type)) text.append('\n');
    }

    private static void bad() {
        throw new AccountRequestException(HttpStatus.BAD_REQUEST, "Enter a valid formatted trip body with supported text and links.");
    }
}
