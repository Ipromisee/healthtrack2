package edu.assignment3.healthtrack.service;

import edu.assignment3.healthtrack.dto.ProviderDtos;
import edu.assignment3.healthtrack.repo.AppointmentRepository;
import edu.assignment3.healthtrack.repo.HealthRecordRepository;
import edu.assignment3.healthtrack.repo.ProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProviderService {
  private final ProviderRepository providerRepo;
  private final AppointmentRepository appointmentRepo;
  private final HealthRecordRepository healthRecordRepo;

  public ProviderService(ProviderRepository providerRepo, AppointmentRepository appointmentRepo, HealthRecordRepository healthRecordRepo) {
    this.providerRepo = providerRepo;
    this.appointmentRepo = appointmentRepo;
    this.healthRecordRepo = healthRecordRepo;
  }

  public ProviderDtos.ProviderLoginResponse login(ProviderDtos.ProviderLoginRequest req) {
    ProviderDtos.ProviderLoginResponse r = providerRepo.login(req.licenseNo().trim(), req.loginCode().trim());
    if (r == null) throw new IllegalArgumentException("Provider 登录失败：license_no 或登录码错误。");
    return r;
  }

  public ProviderDtos.ProviderAppointmentListResponse appointments(long providerId) {
    List<AppointmentRepository.ProviderAppointmentRow> rows = appointmentRepo.listForProvider(providerId);
    List<ProviderDtos.ProviderAppointmentRow> out = rows.stream().map(r ->
        new ProviderDtos.ProviderAppointmentRow(
            r.appointmentId(),
            r.userId(),
            r.userHealthId(),
            r.userName(),
            r.scheduledAt(),
            r.appointmentType(),
            r.status(),
            r.memo(),
            r.cancelReason()
        )
    ).toList();
    return new ProviderDtos.ProviderAppointmentListResponse(out);
  }

  public ProviderDtos.PatientListResponse patients(long providerId) {
    List<AppointmentRepository.PatientRow> rows = appointmentRepo.listPatientsForProvider(providerId);
    List<ProviderDtos.PatientRow> out = rows.stream()
        .map(r -> new ProviderDtos.PatientRow(r.userId(), r.healthId(), r.fullName()))
        .toList();
    return new ProviderDtos.PatientListResponse(out);
  }

  public ProviderDtos.PatientHealthRecordListResponse patientHealthRecords(long providerId, long userId, int limit) {
    if (!appointmentRepo.providerHasPatient(providerId, userId)) {
      throw new IllegalArgumentException("无权限查看该用户状况：该用户不属于当前 Provider 的就诊用户。");
    }
    int safeLimit = Math.min(Math.max(limit, 1), 200);
    List<HealthRecordRepository.PatientHealthRecordRow> rows = healthRecordRepo.listRecentForUser(userId, safeLimit);
    List<ProviderDtos.PatientHealthRecordRow> out = rows.stream()
        .map(r -> new ProviderDtos.PatientHealthRecordRow(
            r.id(), r.metricCode(), r.metricName(), r.unit(), r.recordedAt(), r.value(), r.note()
        ))
        .toList();
    return new ProviderDtos.PatientHealthRecordListResponse(out);
  }

  @Transactional
  public void cancelAppointment(ProviderDtos.ProviderCancelAppointmentRequest req) {
    var row = appointmentRepo.getForProviderCancel(req.appointmentId());
    if (row == null) throw new IllegalArgumentException("预约不存在。");
    if (row.providerId() != req.providerId()) {
      throw new IllegalArgumentException("无权限取消该预约（该预约不属于当前 Provider）。");
    }
    if (!"SCHEDULED".equalsIgnoreCase(row.status())) {
      throw new IllegalArgumentException("仅可取消状态为 SCHEDULED 的预约。");
    }

    // Assumption: provider can cancel anytime with a reason (e.g., Provider Unavailable)
    appointmentRepo.markCancelledByProvider(req.appointmentId(), LocalDateTime.now(), req.reason());
  }
}


