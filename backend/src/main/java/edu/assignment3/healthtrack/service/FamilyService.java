package edu.assignment3.healthtrack.service;

import edu.assignment3.healthtrack.dto.FamilyDtos;
import edu.assignment3.healthtrack.repo.FamilyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FamilyService {
  private final FamilyRepository repo;

  public FamilyService(FamilyRepository repo) {
    this.repo = repo;
  }

  public FamilyDtos.FamilyMemberListResponse members(long userId) {
    List<FamilyDtos.FamilyMemberRow> rows = repo.listFamilyMembersForUser(userId);
    return new FamilyDtos.FamilyMemberListResponse(rows);
  }

  public FamilyDtos.MemberHealthRecordListResponse memberHealthRecords(long viewerUserId, long targetUserId, int limit) {
    if (!repo.shareFamilyGroup(viewerUserId, targetUserId)) {
      throw new IllegalArgumentException("无权限查看：对方不是你的家庭成员。");
    }
    int safeLimit = Math.min(Math.max(limit, 1), 200);
    return new FamilyDtos.MemberHealthRecordListResponse(repo.listRecentHealthRecords(targetUserId, safeLimit));
  }

  public FamilyDtos.MemberChallengeListResponse memberChallenges(long viewerUserId, long targetUserId) {
    if (!repo.shareFamilyGroup(viewerUserId, targetUserId)) {
      throw new IllegalArgumentException("无权限查看：对方不是你的家庭成员。");
    }
    return new FamilyDtos.MemberChallengeListResponse(repo.listChallengesForUser(targetUserId));
  }
}


