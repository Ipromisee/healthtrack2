package edu.assignment3.healthtrack.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public final class SummaryDtos {
  private SummaryDtos() {}

  public record AppointmentCountRequest(
      @NotNull Long userId,
      @NotNull LocalDate startDate,
      @NotNull LocalDate endDate
  ) {}

  public record AppointmentCountResponse(long userId, long count) {}

  public record MetricMonthlyStatsRequest(
      @NotNull Long userId,
      @NotNull String metricCode,
      @NotNull LocalDate monthStart,  // first day of the month
      @NotNull LocalDate monthEnd     // last day of the month
  ) {}

  public record MetricMonthlyStatsResponse(
      long userId,
      String metricCode,
      Double avgValue,
      Double minValue,
      Double maxValue
  ) {}

  public record ActiveUserRow(long userId, String fullName, long recordCount, long completedChallenges) {}

  public record TopActiveUsersResponse(List<ActiveUserRow> items) {}
}


