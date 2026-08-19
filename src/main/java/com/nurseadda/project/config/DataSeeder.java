package com.nurseadda.project.config;

import com.nurseadda.project.entity.RateConfig;
import com.nurseadda.project.repository.RateConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RateConfigRepository rateConfigRepository;

    @Override
    public void run(String... args) {
        seedRateConfigs();
    }

    private void seedRateConfigs() {
        if (rateConfigRepository.findByShiftType("8HR").isEmpty()) {
            RateConfig eightHour = new RateConfig();
            eightHour.setShiftType("8HR");
            eightHour.setStaffHourlyRate(new BigDecimal("180.00"));
            eightHour.setClientHourlyRate(new BigDecimal("450.00"));
            eightHour.setOvertimeMultiplier(new BigDecimal("1.50"));
            rateConfigRepository.save(eightHour);
            log.info("Seeded default 8HR rate config: staff=180, client=450, ot=1.5x");
        }

        if (rateConfigRepository.findByShiftType("12HR").isEmpty()) {
            RateConfig twelveHour = new RateConfig();
            twelveHour.setShiftType("12HR");
            twelveHour.setStaffHourlyRate(new BigDecimal("200.00"));
            twelveHour.setClientHourlyRate(new BigDecimal("500.00"));
            twelveHour.setOvertimeMultiplier(new BigDecimal("1.50"));
            rateConfigRepository.save(twelveHour);
            log.info("Seeded default 12HR rate config: staff=200, client=500, ot=1.5x");
        }
    }
}
