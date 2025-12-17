package edu.assignment3.healthtrack.controller;

import edu.assignment3.healthtrack.dto.AppointmentDtos;
import edu.assignment3.healthtrack.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {
  private final AppointmentService service;

  public AppointmentController(AppointmentService service) {
    this.service = service;
  }

  @PostMapping("/create")
  public long create(@Valid @RequestBody AppointmentDtos.CreateAppointmentRequest req) {
    return service.create(req);
  }

  @PostMapping("/cancel")
  public void cancel(@Valid @RequestBody AppointmentDtos.CancelAppointmentRequest req) {
    service.cancel(req);
  }

  @PostMapping("/search")
  public List<AppointmentDtos.AppointmentRow> search(@RequestBody AppointmentDtos.AppointmentSearchRequest req) {
    return service.search(req);
  }
}


