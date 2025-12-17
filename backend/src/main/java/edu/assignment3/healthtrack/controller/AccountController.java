package edu.assignment3.healthtrack.controller;

import edu.assignment3.healthtrack.dto.AccountDtos;
import edu.assignment3.healthtrack.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")
public class AccountController {
  private final AccountService service;

  public AccountController(AccountService service) {
    this.service = service;
  }

  @PostMapping("/create")
  public AccountDtos.UserSummary create(@Valid @RequestBody AccountDtos.CreateUserRequest req) {
    return service.createUser(req);
  }

  @GetMapping("/{userId}")
  public AccountDtos.AccountInfo get(@PathVariable("userId") long userId) {
    return service.getAccountInfo(userId);
  }

  @PutMapping("/update")
  public void update(@Valid @RequestBody AccountDtos.UpdateUserRequest req) {
    service.updateUser(req);
  }

  @PostMapping("/email/add")
  public void addEmail(@Valid @RequestBody AccountDtos.AddEmailRequest req) {
    service.addEmail(req);
  }

  @PostMapping("/email/remove")
  public void removeEmail(@Valid @RequestBody AccountDtos.RemoveEmailRequest req) {
    service.removeEmail(req);
  }

  @PostMapping("/phone/upsert")
  public void upsertPhone(@Valid @RequestBody AccountDtos.UpsertPhoneRequest req) {
    service.upsertPhone(req);
  }

  @PostMapping("/phone/remove")
  public void removePhone(@Valid @RequestBody AccountDtos.RemovePhoneRequest req) {
    service.removePhone(req);
  }

  @PostMapping("/provider/link")
  public void linkProvider(@Valid @RequestBody AccountDtos.LinkProviderRequest req) {
    service.linkProvider(req);
  }

  @PostMapping("/provider/unlink")
  public void unlinkProvider(@Valid @RequestBody AccountDtos.UnlinkProviderRequest req) {
    service.unlinkProvider(req);
  }

  @PostMapping("/provider/set-primary")
  public void setPrimary(@Valid @RequestBody AccountDtos.SetPrimaryProviderRequest req) {
    service.setPrimary(req);
  }

  @GetMapping("/providers")
  public List<AccountDtos.ProviderRow> listProviders() {
    return service.listProviders();
  }
}


