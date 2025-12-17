package edu.assignment3.healthtrack.controller;

import edu.assignment3.healthtrack.repo.SearchRepository;
import edu.assignment3.healthtrack.service.SearchService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {
  private final SearchService service;

  public SearchController(SearchService service) {
    this.service = service;
  }

  @GetMapping("/health-records")
  public List<SearchRepository.HealthRecordRow> healthRecords(
      @RequestParam(required = false) String healthId,
      @RequestParam(required = false) String metricCode,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
  ) {
    return service.healthRecords(healthId, metricCode, start, end);
  }
}


