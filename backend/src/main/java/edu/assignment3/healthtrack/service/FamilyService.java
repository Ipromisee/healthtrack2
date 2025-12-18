package edu.assignment3.healthtrack.service;

import edu.assignment3.healthtrack.dto.FamilyDtos;
import edu.assignment3.healthtrack.repo.FamilyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  @Transactional
  public void addMember(FamilyDtos.AddMemberRequest req) {
    // actor must be OWNER in that group (as requested)
    FamilyRepository.MemberAuth auth = repo.getMemberAuth(req.actorUserId(), req.familyGroupId());
    if (auth == null) throw new IllegalArgumentException("无权限：你不是该家庭组成员。");
    if (!"OWNER".equalsIgnoreCase(auth.memberRole())) {
      throw new IllegalArgumentException("无权限：仅家庭组 OWNER 可以拉入新成员。");
    }
    if (!repo.userExists(req.targetUserId())) {
      throw new IllegalArgumentException("要添加的用户不存在（userId=" + req.targetUserId() + "）。");
    }
    if (repo.isMember(req.targetUserId(), req.familyGroupId())) {
      throw new IllegalArgumentException("该用户已在家庭组中。");
    }
    String role = (req.memberRole() == null || req.memberRole().isBlank()) ? "MEMBER" : req.memberRole().trim().toUpperCase();
    if (!role.equals("MEMBER") && !role.equals("MANAGER")) {
      // prevent creating another OWNER via UI
      role = "MEMBER";
    }
    boolean canManage = req.canManage() != null && req.canManage();
    repo.addMember(req.familyGroupId(), req.targetUserId(), role, canManage);
  }
}



