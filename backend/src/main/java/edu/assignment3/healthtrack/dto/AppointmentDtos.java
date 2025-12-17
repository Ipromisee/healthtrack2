package edu.assignment3.healthtrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public final class AppointmentDtos {
  private AppointmentDtos() {}

  public record AppointmentRow(
      long id,
      long userId,
      long providerId,
      String providerName,
      LocalDateTime scheduledAt,
      String appointmentType,
      String memo,
      String status,
      String cancelReason
  ) {}

  public record CreateAppointmentRequest(
      @NotNull Long userId,
      @NotNull Long providerId,
      @NotNull LocalDateTime scheduledAt,
      @NotBlank String appointmentType,
      String memo
  ) {}

  public record CancelAppointmentRequest(
      @NotNull Long appointmentId,
      @NotBlank String reason
  ) {}

  public record AppointmentSearchRequest(
      String healthId,
      Long providerId,
      String appointmentType,
      LocalDateTime start,
      LocalDateTime end
  ) {}

  public record AppointmentListResponse(List<AppointmentRow> items) {}
}


