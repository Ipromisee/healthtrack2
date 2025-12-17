package edu.assignment3.healthtrack.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;

public final class ProviderDtos {
  private ProviderDtos() {}

  public record ProviderLoginRequest(
      @NotBlank String licenseNo,
      @NotBlank String loginCode
  ) {}

  public record ProviderLoginResponse(
      long providerId,
      String licenseNo,
      String displayName,
      String specialty
  ) {}

  public record ProviderAppointmentRow(
      long appointmentId,
      long userId,
      String userHealthId,
      String userName,
      LocalDateTime scheduledAt,
      String appointmentType,
      String status,
      String memo,
      String cancelReason
  ) {}

  public record ProviderAppointmentListResponse(List<ProviderAppointmentRow> items) {}

  public record ProviderCancelAppointmentRequest(
      long providerId,
      long appointmentId,
      @NotBlank String reason
  ) {}

  public record PatientRow(long userId, String healthId, String fullName) {}

  public record PatientListResponse(List<PatientRow> items) {}

  public record PatientHealthRecordRow(
      long id,
      String metricCode,
      String metricName,
      String unit,
      LocalDateTime recordedAt,
      double value,
      String note
  ) {}

  public record PatientHealthRecordListResponse(List<PatientHealthRecordRow> items) {}
}


