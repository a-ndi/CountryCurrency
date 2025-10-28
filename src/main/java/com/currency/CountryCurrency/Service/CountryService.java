package com.currency.CountryCurrency.Service;


import com.currency.CountryCurrency.Dto.CountryDto;
import com.currency.CountryCurrency.Model.CountryModel;
import com.currency.CountryCurrency.Repository.CountryRepo;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
            try {
                System.out.println("CountryService - `starting image generation ");
                generateSummaryImage();
            } catch (Exception ex) {
                System.err.println("CountryService - Skipping image generation: " + ex.getMessage());
            }


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

    public void generateSummaryImage() throws Exception {
        List<CountryModel> countries = countryRepo.findAll();
        List<CountryModel> top5 = countries.stream()
                .filter(c -> c.getEstimatedGdp() != null)
                .sorted(Comparator.comparingDouble(CountryModel::getEstimatedGdp).reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<String> names = top5.stream().map(CountryModel::getName).toList();
        List<Double> gdps = top5.stream().map(CountryModel::getEstimatedGdp).toList();

        CategoryChart chart = new CategoryChartBuilder()
                .width(600).height(400)
                .title("Top 5 Countries by Estimated GDP")
                .xAxisTitle("Country").yAxisTitle("GDP").build();

        chart.addSeries("GDP", names, gdps);

        BitmapEncoder.saveBitmap(chart, "cache/summary", BitmapEncoder.BitmapFormat.PNG);
        System.out.println("✅ Summary image generated (font-free with XChart)");
    }
}




