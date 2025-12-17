package edu.assignment3.healthtrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public final class HealthRecordDtos {
  private HealthRecordDtos() {}

  public record MetricTypeRow(long id, String code, String displayName, String unit) {}

  public record MetricTypeListResponse(List<MetricTypeRow> items) {}

  public record CreateHealthRecordRequest(
      @NotNull Long userId,
      @NotBlank String metricCode,
      @NotNull LocalDateTime recordedAt,
      @NotNull Double metricValue,
      String note
  ) {}

  public record CreateHealthRecordResponse(long id) {}
}


