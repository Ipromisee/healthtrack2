package edu.assignment3.healthtrack.controller;

import edu.assignment3.healthtrack.dto.ChallengeDtos;
import edu.assignment3.healthtrack.service.ChallengeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/challenge")
public class ChallengeController {
  private final ChallengeService service;

  public ChallengeController(ChallengeService service) {
    this.service = service;
  }

  @PostMapping("/create")
  public long create(@Valid @RequestBody ChallengeDtos.CreateChallengeRequest req) {
    return service.create(req);
  }

  @GetMapping("/list")
  public List<ChallengeDtos.ChallengeRow> list() {
    return service.list();
  }

  @GetMapping("/my")
  public List<ChallengeDtos.ChallengeRow> my(@RequestParam("userId") long userId) {
    return service.listForUser(userId);
  }

  @PostMapping("/join")
  public void join(@RequestParam("challengeId") long challengeId, @RequestParam("userId") long userId) {
    service.join(challengeId, userId);
  }

  @PostMapping("/progress")
  public void progress(@Valid @RequestBody ChallengeDtos.UpdateProgressRequest req) {
    service.updateProgress(req);
  }

  @GetMapping("/{challengeId}/participants")
  public List<ChallengeDtos.ParticipantRow> participants(@PathVariable("challengeId") long challengeId) {
    return service.participants(challengeId);
  }

  @PostMapping("/invite")
  public long invite(@Valid @RequestBody ChallengeDtos.InviteRequest req) {
    return service.invite(req);
  }

  @GetMapping("/{challengeId}/invitations")
  public List<ChallengeDtos.InvitationRow> invitations(@PathVariable("challengeId") long challengeId) {
    return service.invitations(challengeId);
  }

  @GetMapping("/top")
  public List<ChallengeDtos.TopChallengeRow> top(@RequestParam(defaultValue = "5") int limit) {
    return service.topChallenges(limit);
  }
}


