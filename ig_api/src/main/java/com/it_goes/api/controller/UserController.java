package com.it_goes.api.controller;

import com.it_goes.api.jpa.projection.FirstNameDaysLocationYear;
import com.it_goes.api.jpa.projection.FirstNameDaysYear;
import com.it_goes.api.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("getDaysSkied")
    public ResponseEntity<List<FirstNameDaysYear>> getDaysSkied(
            @RequestParam(name="year", required = false) Integer year
    ){
        if (year == null) {
            logger.info("UserController.getDaysSkied: No year provided, getting all year count data");
        }

        final List<FirstNameDaysYear> firstNameDays = userService.getDaysSkied(year);

        logger.info("UserController.getDaysSkied: found days data for {} users/years", firstNameDays.size());

        return ResponseEntity.ok(firstNameDays);
    }

    @GetMapping("getDaysSkiedLocation")
    public ResponseEntity<List<FirstNameDaysLocationYear>> getDaysSkiedLocation(
            @RequestParam(name="year", required = false) Integer year
    ){
        if (year == null) {
            logger.info("UserController.getDaysSkiedLocation: No year provided, getting all year count data");
        }

        final List<FirstNameDaysLocationYear> firstNameDays = userService.getDaysSkiedEachLocation(year);

        logger.info("UserController.getDaysSkiedLocation: found days data for {} users/location/years", firstNameDays.size());

        return ResponseEntity.ok(firstNameDays);
    }
}
