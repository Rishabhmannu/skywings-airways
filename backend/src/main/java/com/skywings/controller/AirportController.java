package com.skywings.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/airports")
@RequiredArgsConstructor
@Slf4j
public class AirportController {

    private final ObjectMapper objectMapper;
    private List<Map<String, String>> airports = new ArrayList<>();

    @PostConstruct
    public void loadAirports() {
        try {
            var resource = new ClassPathResource("airports.json");
            airports = objectMapper.readValue(resource.getInputStream(),
                new TypeReference<List<Map<String, String>>>() {});
            log.info("Loaded {} airports for search", airports.size());
        } catch (Exception e) {
            log.error("Failed to load airports.json: {}", e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, String>>> searchAirports(
            @RequestParam String q,
            @RequestParam(defaultValue = "15") int limit) {
        if (q == null || q.trim().length() < 2 || q.trim().length() > 50) {
            return ResponseEntity.ok(List.of());
        }
        if (limit < 1 || limit > 50) limit = 15;

        // Strip any non-alphanumeric/space characters to prevent injection
        String query = q.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase().trim();

        // Score-based: exact code match first, then city starts-with, then contains
        List<Map<String, String>> results = airports.stream()
            .filter(a -> {
                String code = a.getOrDefault("code", "").toLowerCase();
                String name = a.getOrDefault("name", "").toLowerCase();
                String city = a.getOrDefault("city", "").toLowerCase();
                return code.equals(query) || code.startsWith(query)
                    || city.startsWith(query) || city.contains(query)
                    || name.contains(query);
            })
            .sorted((a, b) -> {
                int scoreA = matchScore(a, query);
                int scoreB = matchScore(b, query);
                return scoreB - scoreA;
            })
            .limit(limit)
            .toList();

        return ResponseEntity.ok(results);
    }

    private int matchScore(Map<String, String> airport, String query) {
        String code = airport.getOrDefault("code", "").toLowerCase();
        String city = airport.getOrDefault("city", "").toLowerCase();
        if (code.equals(query)) return 100;
        if (code.startsWith(query)) return 80;
        if (city.equals(query)) return 70;
        if (city.startsWith(query)) return 60;
        if (city.contains(query)) return 40;
        return 10;
    }
}
