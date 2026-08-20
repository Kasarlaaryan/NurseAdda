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
        // 8HR shift: client pays 1500 per day (187.50/hr x 8hrs)
        if (rateConfigRepository.findByShiftType("8HR").isEmpty()) {
            RateConfig eightHour = new RateConfig();
            eightHour.setShiftType("8HR");
            eightHour.setStaffHourlyRate(new BigDecimal("125.00"));
            eightHour.setClientHourlyRate(new BigDecimal("187.50"));
            eightHour.setOvertimeMultiplier(new BigDecimal("1.50"));
            rateConfigRepository.save(eightHour);
            log.info("Seeded default 8HR rate config: client=187.50/hr (1500/day), staff=125/hr, ot=1.5x");
        }

        // 12HR shift: client pays 2250 per day (187.50/hr x 12hrs)
        if (rateConfigRepository.findByShiftType("12HR").isEmpty()) {
            RateConfig twelveHour = new RateConfig();
            twelveHour.setShiftType("12HR");
            twelveHour.setStaffHourlyRate(new BigDecimal("125.00"));
            twelveHour.setClientHourlyRate(new BigDecimal("187.50"));
            twelveHour.setOvertimeMultiplier(new BigDecimal("1.50"));
            rateConfigRepository.save(twelveHour);
            log.info("Seeded default 12HR rate config: client=187.50/hr (2250/day), staff=125/hr, ot=1.5x");
        }
    }
}
