package edu.assignment3.healthtrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public final class AccountDtos {
  private AccountDtos() {}

  public record UserSummary(
      long id,
      String healthId,
      String fullName,
      Long primaryProviderId
  ) {}

  public record EmailRow(long id, String email, boolean isVerified) {}

  public record PhoneRow(String phone, boolean isVerified) {}

  public record ProviderRow(long id, String licenseNo, String displayName, String specialty, boolean isVerified) {}

  public record AccountInfo(
      UserSummary user,
      PhoneRow phone,
      List<EmailRow> emails,
      List<ProviderRow> linkedProviders
  ) {}

  public record CreateUserRequest(
      @NotBlank String healthId,
      @NotBlank String fullName,
      @NotBlank String email,
      @NotBlank String phone
  ) {}

  public record UpdateUserRequest(
      @NotNull Long userId,
      @NotBlank String fullName
  ) {}

  public record AddEmailRequest(
      @NotNull Long userId,
      @NotBlank String email
  ) {}

  public record RemoveEmailRequest(
      @NotNull Long userId,
      @NotBlank String email
  ) {}

  public record UpsertPhoneRequest(
      @NotNull Long userId,
      @NotBlank String phone
  ) {}

  public record RemovePhoneRequest(
      @NotNull Long userId
  ) {}

  public record LinkProviderRequest(
      @NotNull Long userId,
      @NotNull Long providerId
  ) {}

  public record UnlinkProviderRequest(
      @NotNull Long userId,
      @NotNull Long providerId
  ) {}

  public record SetPrimaryProviderRequest(
      @NotNull Long userId,
      Long providerId
  ) {}
}


