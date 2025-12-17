package edu.assignment3.healthtrack.controller;

import edu.assignment3.healthtrack.dto.SummaryDtos;
import edu.assignment3.healthtrack.service.SummaryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/summary")
public class SummaryController {
  private final SummaryService service;

  public SummaryController(SummaryService service) {
    this.service = service;
  }

  @PostMapping("/appointment-count")
  public SummaryDtos.AppointmentCountResponse appointmentCount(@Valid @RequestBody SummaryDtos.AppointmentCountRequest req) {
    return service.appointmentCount(req);
  }

  @PostMapping("/metric-stats")
  public SummaryDtos.MetricMonthlyStatsResponse metricStats(@Valid @RequestBody SummaryDtos.MetricMonthlyStatsRequest req) {
    return service.metricStats(req);
  }

  @GetMapping("/top-active-users")
  public SummaryDtos.TopActiveUsersResponse topActiveUsers(@RequestParam(defaultValue = "5") int limit) {
    return service.topActiveUsers(limit);
  }
}


