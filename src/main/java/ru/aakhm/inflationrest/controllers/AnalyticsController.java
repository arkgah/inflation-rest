package ru.aakhm.inflationrest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.aakhm.inflationrest.dto.out.CPIOutDTO;
import ru.aakhm.inflationrest.dto.out.ErrorDTO;
import ru.aakhm.inflationrest.models.validation.except.analytics.CPIIncorrectDateInterval;
import ru.aakhm.inflationrest.services.AnalyticsService;
import ru.aakhm.inflationrest.utils.Utils;

import java.util.Date;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {
    private final Utils utils;
    private final AnalyticsService analyticsService;

    @Autowired
    public AnalyticsController(Utils utils, AnalyticsService analyticsService) {
        this.utils = utils;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/cpi")
    // ISO.DATE == yyyy-MM-dd
    // /cpi?beginDate=2022-01-01&endDate=2022-03-31
    public ResponseEntity<CPIOutDTO> getCPI(
            @RequestParam("beginDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date beginDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate
    ) {
        if (beginDate.getTime() > endDate.getTime())
            throw new CPIIncorrectDateInterval(utils.getMessageFromBundle("cpi.incorrect.dates.err"));

        return new ResponseEntity<>(analyticsService.getCPI(beginDate, endDate), HttpStatus.OK);
    }

    @ExceptionHandler
    private ResponseEntity<ErrorDTO> handleException(RuntimeException e) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage(e.getMessage());
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }
}
