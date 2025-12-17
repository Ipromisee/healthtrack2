package edu.assignment3.healthtrack.controller;

import edu.assignment3.healthtrack.dto.HealthRecordDtos;
import edu.assignment3.healthtrack.service.HealthRecordService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health-record")
public class HealthRecordController {
  private final HealthRecordService service;

  public HealthRecordController(HealthRecordService service) {
    this.service = service;
  }

  @GetMapping("/metric-types")
  public HealthRecordDtos.MetricTypeListResponse metricTypes() {
    return service.metricTypes();
  }

  @PostMapping("/create")
  public HealthRecordDtos.CreateHealthRecordResponse create(@Valid @RequestBody HealthRecordDtos.CreateHealthRecordRequest req) {
    return service.create(req);
  }
}


