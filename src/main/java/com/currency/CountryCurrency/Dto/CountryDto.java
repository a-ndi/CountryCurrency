package com.currency.CountryCurrency.Dto;

import lombok.*;

import java.util.List;


@Setter
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor

public class CountryDto {
        private String name;
        private String capital;
        private String region;
        private Long population;
        private String flag;
        private List<CurrencyDto> currencies;

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

        public String getFlag() {
                return flag;
        }

        public void setFlag(String flag) {
                this.flag = flag;
        }

        public List<CurrencyDto> getCurrencies() {
                return currencies;
        }

        public void setCurrencies(List<CurrencyDto> currencies) {
                this.currencies = currencies;
        }
}
