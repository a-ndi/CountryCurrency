package com.currency.CountryCurrency.Service;


import com.currency.CountryCurrency.Dto.CountryDto;
import com.currency.CountryCurrency.Model.CountryModel;
import com.currency.CountryCurrency.Repository.CountryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service

public class CountryService {

    private final RestTemplate restTemplate;
    private final CountryRepo countryRepo;

    public CountryService(RestTemplate restTemplate, CountryRepo countryRepo) {
        this.restTemplate = restTemplate;
        this.countryRepo = countryRepo;
    }

    private static LocalDateTime lastRefreshTime;

    public void refreshCountries() {
        try {
            // 1️⃣ Fetch all countries
            String countryUrl = "https://restcountries.com/v2/all?fields=name,capital,region,population,flag,currencies";
            ResponseEntity<List<CountryDto>> response = restTemplate.exchange(
                    countryUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<CountryDto>>() {}
            );
            List<CountryDto> countries = response.getBody();

            if (countries == null) {
                System.out.println("CountryService - ❌ API returned null country list!");
            } else {
                System.out.println("CountryService - ✅ API returned " + countries.size() + " countries");
            }


            if (countries == null || countries.isEmpty()) {
                throw new IllegalStateException("No country data fetched from API");
            }

            // 2️⃣ Fetch exchange rates
            String rateUrl = "https://open.er-api.com/v6/latest/USD";
            Map<String, Object> rateData = restTemplate.getForObject(rateUrl, Map.class);
            Map<String, Object> ratesData = (Map<String, Object>) rateData.get("rates");

            //  Loop through all countries
            for (CountryDto c : countries) {
                String name = c.getName();
                String capital = c.getCapital();
                String region = c.getRegion();
                Long population = c.getPopulation();


                String flag = null;
                if (c.getFlag() != null) {
                    flag = c.getFlag();
                } else if (c.getFlags() instanceof Map) {
                    Map<?, ?> flagMap = (Map<?, ?>) c.getFlags();
                    flag = (String) flagMap.get("png");
                }

                String currencyCode = null;
                Double exchangeRate = null;
                Double estimatedGdp = null;

                // --- Handle currency cases properly ---
                if (c.getCurrencies() == null || c.getCurrencies().isEmpty()) {
                    currencyCode = null;
                    exchangeRate = null;
                    estimatedGdp = 0.0;
                } else {
                    currencyCode = c.getCurrencies().get(0).getCode();

                    if (currencyCode != null && ratesData.containsKey(currencyCode)) {
                        Object rateValue = ratesData.get(currencyCode);
                        if (rateValue instanceof Number) {
                            exchangeRate = ((Number) rateValue).doubleValue();
                        }

                        if (exchangeRate != null && exchangeRate > 0) {
                            double random = 1000 + new Random().nextDouble() * 1000;
                            estimatedGdp = (population * random) / exchangeRate;
                        }
                    } else {
                        exchangeRate = null;
                        estimatedGdp = null;
                    }
                }

                // --- Save or update country ---
                CountryModel country = countryRepo.findByNameIgnoreCase(name)
                        .orElse(new CountryModel());
                country.setName(name);
                country.setCapital(capital);
                country.setRegion(region);
                country.setPopulation(population);
                country.setCurrencyCode(currencyCode);
                country.setExchangeRate(exchangeRate);
                country.setEstimatedGdp(estimatedGdp);
                country.setFlagUrl(flag);
                country.setLastRefreshedAt(LocalDateTime.now());

                countryRepo.save(country);
                System.out.println("✅ Saved country: " + country.getName());
            }

            // Update global refresh timestamp
            lastRefreshTime = LocalDateTime.now();
            System.out.println("CountryService - Countries refreshed successfully at: " + lastRefreshTime);
//            try {
//                System.out.println("CountryService - `starting image generation ");
//                generateSummaryImage();
//            } catch (Exception ex) {
//                System.err.println("CountryService - Skipping image generation: " + ex.getMessage());
//            }


        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("External data source unavailable: " + e.getMessage());
        }
    }

    public static LocalDateTime getLastRefreshTime() {
        return lastRefreshTime;
    }

    public long getTotalCountries() {
        return countryRepo.count();
    }





    public List<CountryModel> getAllCountries(String region, String currency, String sort) {
        List<CountryModel> countries = countryRepo.findAll();

        //Filter by region
        if (region != null && !region.isEmpty()) {
            countries = countries.stream()
                    .filter(c -> c.getRegion() != null && c.getRegion().equalsIgnoreCase(region))
                    .collect(Collectors.toList());
        }

        //Filter by currency
        if (currency != null && !currency.isEmpty()) {
            countries = countries.stream()
                    .filter(c -> c.getCurrencyCode() != null && c.getCurrencyCode().equalsIgnoreCase(currency))
                    .collect(Collectors.toList());
        }

        // Sorting
        if (sort != null && !sort.isEmpty()) {
            if (sort.equalsIgnoreCase("gdp_desc")) {
                countries = countries.stream()
                        .sorted((a, b) -> Double.compare(
                                b.getEstimatedGdp() == null ? 0 : b.getEstimatedGdp(),
                                a.getEstimatedGdp() == null ? 0 : a.getEstimatedGdp()))
                        .collect(Collectors.toList());
            } else if (sort.equalsIgnoreCase("gdp_asc")) {
                countries = countries.stream()
                        .sorted((a, b) -> Double.compare(
                                a.getEstimatedGdp() == null ? 0 : a.getEstimatedGdp(),
                                b.getEstimatedGdp() == null ? 0 : b.getEstimatedGdp()))
                        .collect(Collectors.toList());
            }
        }

        return countries;
    }



    public CountryModel getCountryByName(String name) {
        return countryRepo.findByNameIgnoreCase(name)
                .orElseThrow(() -> new IllegalArgumentException("Country not found"));
    }


    public void deleteCountryByName(String name) {
        CountryModel country = countryRepo.findByNameIgnoreCase(name)
                .orElseThrow(() -> new IllegalArgumentException("Country not found"));

        countryRepo.delete(country);
    }





    public void generateSummaryImage() {
        try {
            // Get data
            List<CountryModel> countries = countryRepo.findAll();
            System.out.println("CountryService - got all countries ");
            int totalCountries = countries.size();
            System.out.println("CountryService - got image size");


            // Sort by GDP and pick top 5
            List<CountryModel> top5 = countries.stream()
                    .filter(c -> c.getEstimatedGdp() != null)
                    .sorted(Comparator.comparingDouble(CountryModel::getEstimatedGdp).reversed())
                    .limit(5)
                    .collect(Collectors.toList());

            System.out.println("CountryService - sorted");

            // Create image canvas
            int width = 600;
            int height = 300;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            System.out.println("CountryService - create graphics");

            // Background
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            System.out.println("CountryService - set background");

            // Header
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Country Summary", 20, 40);

            System.out.println("CountryService - set Header");

            // Total countries
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("Total Countries: " + totalCountries, 20, 80);

            System.out.println("CountryService - Draw string");

            // Top 5 by GDP
            g.drawString("Top 5 Countries by Estimated GDP:", 20, 110);
            int y = 140;
            for (CountryModel c : top5) {
                g.drawString(c.getName() + " → " + String.format("%.2f", c.getEstimatedGdp()), 40, y);
                y += 25;
            }

            // Timestamp
            g.setFont(new Font("Arial", Font.ITALIC, 14));
            g.drawString("Last Refreshed: " + lastRefreshTime, 20, height - 30);

            g.dispose();
            System.out.println("CountryService - Did some font stuff ");


            // Ensure folder exists
            File folder = new File("cache");
            if (!folder.exists()) folder.mkdirs();

            // Save image
            ImageIO.write(image, "png", new File("cache/summary.png"));

            System.out.println(" Summary image generated successfully!");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to generate summary image: " + e.getMessage());
        }
    }
}




