package edu.assignment3.healthtrack.controller;

import edu.assignment3.healthtrack.dto.FamilyDtos;
import edu.assignment3.healthtrack.service.FamilyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/family")
public class FamilyController {
  private final FamilyService service;

  public FamilyController(FamilyService service) {
    this.service = service;
  }

  @GetMapping("/{userId}/members")
  public FamilyDtos.FamilyMemberListResponse members(@PathVariable("userId") long userId) {
    return service.members(userId);
  }

  @PostMapping("/member/health-records")
  public FamilyDtos.MemberHealthRecordListResponse memberHealthRecords(
      @Valid @RequestBody FamilyDtos.MemberDataRequest req,
      @RequestParam(defaultValue = "50") int limit
  ) {
    return service.memberHealthRecords(req.viewerUserId(), req.targetUserId(), limit);
  }

  @PostMapping("/member/challenges")
  public FamilyDtos.MemberChallengeListResponse memberChallenges(@Valid @RequestBody FamilyDtos.MemberDataRequest req) {
    return service.memberChallenges(req.viewerUserId(), req.targetUserId());
  }
}


