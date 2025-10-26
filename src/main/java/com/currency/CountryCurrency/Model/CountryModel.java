package com.currency.CountryCurrency.Model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
@JsonPropertyOrder ({
        "id",
        "name",
        "capital",
        "region",
        "population",
        "currency_code",
        "exchange_rate",
        "estimated_gdp",
        "flag_url",
        "last_refreshed_at"
})
@Entity
public class CountryModel {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)

    private Long Id;
    private String name;
    private String capital;
    private String region;
    private Long population;
    @JsonProperty("currency_code")
    private String currencyCode;

    @JsonProperty("exchange_rate")
    private Double exchangeRate;

    @JsonProperty("estimated_gdp")
    private Double estimatedGdp;

    @JsonProperty("flag_url")
    private String flagUrl;

    @JsonProperty("last_refreshed_at")
    private LocalDateTime lastRefreshedAt;



    public CountryModel() {
    }

    public CountryModel(Long id, String name, String capital, String region, Long population, String currencyCode, Double exchangeRate, Double estimatedGdp, String flagUrl, LocalDateTime lastRefreshedAt) {
        Id = id;
        this.name = name;
        this.capital = capital;
        this.region = region;
        this.population = population;
        this.currencyCode = currencyCode;
        this.exchangeRate = exchangeRate;
        this.estimatedGdp = estimatedGdp;
        this.flagUrl = flagUrl;
        this.lastRefreshedAt = lastRefreshedAt;
    }


    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Long getPopulation() {
        return population;
    }

    public void setPopulation(Long population) {
        this.population = population;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(Double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public Double getEstimatedGdp() {
        return estimatedGdp;
    }

    public void setEstimatedGdp(Double estimatedGdp) {
        this.estimatedGdp = estimatedGdp;
    }

    public String getFlagUrl() {
        return flagUrl;
    }

    public void setFlagUrl(String flagUrl) {
        this.flagUrl = flagUrl;
    }

    public LocalDateTime getLastRefreshedAt() {
        return lastRefreshedAt;
    }

    public void setLastRefreshedAt(LocalDateTime lastRefreshedAt) {
        this.lastRefreshedAt = lastRefreshedAt;
    }

    @Override
    public String toString() {
        return "CountryModel{" +
                "Id=" + Id +
                ", name='" + name + '\'' +
                ", capital='" + capital + '\'' +
                ", region='" + region + '\'' +
                ", population=" + population +
                ", currencyCode='" + currencyCode + '\'' +
                ", exchangeRate=" + exchangeRate +
                ", estimatedGdp=" + estimatedGdp +
                ", flagUrl='" + flagUrl + '\'' +
                ", lastRefreshedAt=" + lastRefreshedAt +
                '}';
    }
}
