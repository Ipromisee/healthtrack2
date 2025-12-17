package edu.assignment3.healthtrack.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public final class FamilyDtos {
  private FamilyDtos() {}

  public record FamilyMemberRow(
      long familyGroupId,
      String groupName,
      long userId,
      String healthId,
      String fullName,
      String memberRole,
      boolean canManage
  ) {}

  public record FamilyMemberListResponse(List<FamilyMemberRow> items) {}

  public record MemberHealthRecordRow(
      long id,
      String metricCode,
      String metricName,
      String unit,
      LocalDateTime recordedAt,
      double value,
      String note
  ) {}

  public record MemberHealthRecordListResponse(List<MemberHealthRecordRow> items) {}

  public record MemberChallengeRow(
      long challengeId,
      String goalText,
      java.time.LocalDate startDate,
      java.time.LocalDate endDate,
      double progressValue,
      boolean isCompleted
  ) {}

  public record MemberChallengeListResponse(List<MemberChallengeRow> items) {}

  public record MemberDataRequest(
      @NotNull Long viewerUserId,
      @NotNull Long targetUserId
  ) {}
}


