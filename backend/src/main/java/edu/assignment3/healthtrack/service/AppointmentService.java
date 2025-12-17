package edu.assignment3.healthtrack.service;

import edu.assignment3.healthtrack.dto.AppointmentDtos;
import edu.assignment3.healthtrack.repo.AccountRepository;
import edu.assignment3.healthtrack.repo.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AppointmentService {
  private final AppointmentRepository repo;
  private final AccountRepository accountRepo;

  public AppointmentService(AppointmentRepository repo, AccountRepository accountRepo) {
    this.repo = repo;
    this.accountRepo = accountRepo;
  }

  public long create(AppointmentDtos.CreateAppointmentRequest req) {
    String type = req.appointmentType().trim().toUpperCase();
    if (!type.equals("IN_PERSON") && !type.equals("VIRTUAL")) {
      throw new IllegalArgumentException("appointmentType must be IN_PERSON or VIRTUAL");
    }
    if (!accountRepo.isProviderLinked(req.userId(), req.providerId())) {
      throw new IllegalArgumentException("You can only book appointments with providers linked to your account.");
    }
    return repo.createAppointment(req.userId(), req.providerId(), req.scheduledAt(), type, req.memo());
  }

  @Transactional
  public void cancel(AppointmentDtos.CancelAppointmentRequest req) {
    AppointmentRepository.AppointmentRowForCancel row = repo.getForCancel(req.appointmentId());
    if (row == null) throw new IllegalArgumentException("Appointment not found: " + req.appointmentId());
    if (!"SCHEDULED".equalsIgnoreCase(row.status())) {
      throw new IllegalArgumentException("Only SCHEDULED appointments can be cancelled.");
    }
    LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    // Allowed up to 24h before scheduled time
    if (!now.isBefore(row.scheduledAt().minusHours(24))) {
      throw new IllegalArgumentException("Cannot cancel within 24 hours of scheduled time.");
    }
    repo.markCancelled(req.appointmentId(), now, req.reason());
  }

  public List<AppointmentDtos.AppointmentRow> search(AppointmentDtos.AppointmentSearchRequest req) {
    return repo.search(req);
  }
}


