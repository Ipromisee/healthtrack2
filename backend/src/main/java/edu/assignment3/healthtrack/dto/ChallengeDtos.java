package edu.assignment3.healthtrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ChallengeDtos {
  private ChallengeDtos() {}

  public record ChallengeRow(
      long id,
      long creatorUserId,
      String goalText,
      LocalDate startDate,
      LocalDate endDate
  ) {}

  public record CreateChallengeRequest(
      @NotNull Long creatorUserId,
      @NotBlank String goalText,
      @NotNull LocalDate startDate,
      @NotNull LocalDate endDate
  ) {}

  public record ParticipantRow(
      long userId,
      String fullName,
      double progressValue,
      boolean isCompleted
  ) {}

  public record UpdateProgressRequest(
      @NotNull Long challengeId,
      @NotNull Long userId,
      @NotNull Double progressValue,
      @NotNull Boolean isCompleted
  ) {}

  public record InviteRequest(
      @NotNull Long challengeId,
      @NotNull Long senderUserId,
      @NotBlank String recipientType,   // EMAIL or PHONE
      @NotBlank String recipientValue
  ) {}

  public record InvitationRow(
      long id,
      long challengeId,
      long senderUserId,
      String recipientType,
      String recipientValue,
      Long recipientUserId,
      String status,
      LocalDateTime initiatedAt,
      LocalDateTime completedAt,
      LocalDateTime expiresAt
  ) {}

  public record TopChallengeRow(long challengeId, String goalText, long participantCount) {}
}


