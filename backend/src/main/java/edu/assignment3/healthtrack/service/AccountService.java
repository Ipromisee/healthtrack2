package edu.assignment3.healthtrack.service;

import edu.assignment3.healthtrack.dto.AccountDtos;
import edu.assignment3.healthtrack.repo.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountService {
  private final AccountRepository repo;

  public AccountService(AccountRepository repo) {
    this.repo = repo;
  }

  @Transactional
  public AccountDtos.UserSummary createUser(AccountDtos.CreateUserRequest req) {
    long userId = repo.createUser(req.healthId(), req.fullName());
    repo.addEmail(userId, req.email());
    repo.upsertPhone(userId, req.phone());
    AccountDtos.UserSummary u = repo.getUser(userId);
    if (u == null) throw new IllegalStateException("Created user not found.");
    return u;
  }

  public AccountDtos.AccountInfo getAccountInfo(long userId) {
    AccountDtos.UserSummary user = repo.getUser(userId);
    if (user == null) throw new IllegalArgumentException("User not found: " + userId);
    AccountDtos.PhoneRow phone = repo.getPhone(userId);
    List<AccountDtos.EmailRow> emails = repo.listEmails(userId);
    List<AccountDtos.ProviderRow> providers = repo.listLinkedProviders(userId);
    return new AccountDtos.AccountInfo(user, phone, emails, providers);
  }

  public List<AccountDtos.ProviderRow> listProviders() {
    return repo.listAllProviders();
  }

  public void updateUser(AccountDtos.UpdateUserRequest req) {
    repo.updateUserName(req.userId(), req.fullName());
  }

  public void addEmail(AccountDtos.AddEmailRequest req) {
    repo.addEmail(req.userId(), req.email());
  }

  public void removeEmail(AccountDtos.RemoveEmailRequest req) {
    repo.removeEmail(req.userId(), req.email());
  }

  public void upsertPhone(AccountDtos.UpsertPhoneRequest req) {
    repo.upsertPhone(req.userId(), req.phone());
  }

  public void removePhone(AccountDtos.RemovePhoneRequest req) {
    repo.removePhone(req.userId());
  }

  public void linkProvider(AccountDtos.LinkProviderRequest req) {
    repo.linkProvider(req.userId(), req.providerId());
  }

  public void unlinkProvider(AccountDtos.UnlinkProviderRequest req) {
    repo.unlinkProvider(req.userId(), req.providerId());
  }

  public void setPrimary(AccountDtos.SetPrimaryProviderRequest req) {
    repo.setPrimaryProvider(req.userId(), req.providerId());
  }
}


