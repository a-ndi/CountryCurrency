package com.currency.CountryCurrency.Controller;


import com.currency.CountryCurrency.Model.CountryModel;
import com.currency.CountryCurrency.Service.CountryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping ("/countries")
public class CountryController {

    private final CountryService countryService;
    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshCountries() {
        try {
            countryService.refreshCountries();
            return ResponseEntity.ok().body(
                    Map.of("message", "Countries refreshed successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.status(503).body(
                    Map.of("error", "External data source unavailable", "details", e.getMessage())
            );
        }
    }


    @GetMapping
    public ResponseEntity<?> getAllCountries(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false, name = "sort") String sortOrder
    ) {
        try {
            List<CountryModel> countries = countryService.getAllCountries(region, currency, sortOrder);
            return ResponseEntity.ok(countries);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Internal server error",
                    "details", e.getMessage()
            ));
        }
    }


    @GetMapping("/{name}")
    public ResponseEntity<?> getCountryByName(@PathVariable String name) {
        try {
            CountryModel country = countryService.getCountryByName(name);
            return ResponseEntity.ok(country);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", "Country not found"));
        }
    }


    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteCountryByName(@PathVariable String name) {
        try {
            countryService.deleteCountryByName(name);
            return ResponseEntity.ok(Map.of("message", "Country deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", "Country not found"));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus(){
        long totalCountries = countryService.getTotalCountries();
        LocalDateTime lastRefreshTime = countryService.getLastRefreshTime();

        String formattedTime = lastRefreshTime != null
                ? lastRefreshTime.format(DateTimeFormatter.ISO_INSTANT.withZone(java.time.ZoneOffset.UTC))
                : "Never refreshed";


        return ResponseEntity.ok(Map.of(
                "total_countries", totalCountries,
                "last_refreshed_at", lastRefreshTime != null ? lastRefreshTime : "Never refreshed"
        ));
    }


    @GetMapping("/image")
    public ResponseEntity<?> getSummaryImage() {
        File imageFile = new File("cache/summary.png");

        if (!imageFile.exists()) {
            return ResponseEntity.status(404).body(
                    Map.of("error", "Summary image not found")
            );
        }

        try {
            byte[] imageBytes = java.nio.file.Files.readAllBytes(imageFile.toPath());
            return ResponseEntity.ok()
                    .header("Content-Type", "image/png")
                    .body(imageBytes);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(
                    Map.of("error", "Failed to load image", "details", e.getMessage())
            );
        }

    }

}
