package edu.assignment3.healthtrack.controller;

import edu.assignment3.healthtrack.dto.ProviderDtos;
import edu.assignment3.healthtrack.service.ProviderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/provider")
public class ProviderController {
  private final ProviderService service;

  public ProviderController(ProviderService service) {
    this.service = service;
  }

  @PostMapping("/login")
  public ProviderDtos.ProviderLoginResponse login(@Valid @RequestBody ProviderDtos.ProviderLoginRequest req) {
    return service.login(req);
  }

  @GetMapping("/{providerId}/appointments")
  public ProviderDtos.ProviderAppointmentListResponse appointments(@PathVariable("providerId") long providerId) {
    return service.appointments(providerId);
  }

  @PostMapping("/appointment/cancel")
  public void cancel(@Valid @RequestBody ProviderDtos.ProviderCancelAppointmentRequest req) {
    service.cancelAppointment(req);
  }

  @GetMapping("/{providerId}/patients")
  public ProviderDtos.PatientListResponse patients(@PathVariable("providerId") long providerId) {
    return service.patients(providerId);
  }

  @GetMapping("/{providerId}/patients/{userId}/health-records")
  public ProviderDtos.PatientHealthRecordListResponse patientHealthRecords(
      @PathVariable("providerId") long providerId,
      @PathVariable("userId") long userId,
      @RequestParam(defaultValue = "50") int limit
  ) {
    return service.patientHealthRecords(providerId, userId, limit);
  }
}


