package com.currency.CountryCurrency.Repository;


import com.currency.CountryCurrency.Model.CountryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface CountryRepo extends JpaRepository<CountryModel, Long> {

    Optional<CountryModel> findByNameIgnoreCase(String name);
}
