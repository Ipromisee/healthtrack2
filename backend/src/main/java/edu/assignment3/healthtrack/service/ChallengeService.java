package edu.assignment3.healthtrack.service;

import edu.assignment3.healthtrack.dto.ChallengeDtos;
import edu.assignment3.healthtrack.repo.AccountRepository;
import edu.assignment3.healthtrack.repo.ChallengeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChallengeService {
  private final ChallengeRepository repo;
  private final AccountRepository accountRepo;

  public ChallengeService(ChallengeRepository repo, AccountRepository accountRepo) {
    this.repo = repo;
    this.accountRepo = accountRepo;
  }

  public long create(ChallengeDtos.CreateChallengeRequest req) {
    if (req.endDate().isBefore(req.startDate())) {
      throw new IllegalArgumentException("endDate must be >= startDate");
    }
    return repo.createChallenge(req.creatorUserId(), req.goalText(), req.startDate(), req.endDate());
  }

  public List<ChallengeDtos.ChallengeRow> list() {
    return repo.listChallenges();
  }

  public List<ChallengeDtos.ChallengeRow> listForUser(long userId) {
    return repo.listChallengesForUser(userId);
  }

  @Transactional
  public void join(long challengeId, long userId) {
    repo.ensureParticipant(challengeId, userId);
  }

  public void updateProgress(ChallengeDtos.UpdateProgressRequest req) {
    LocalDateTime completedAt = req.isCompleted() ? LocalDateTime.now() : null;
    repo.updateProgress(req.challengeId(), req.userId(), req.progressValue(), req.isCompleted(), completedAt);
  }

  public List<ChallengeDtos.ParticipantRow> participants(long challengeId) {
    return repo.listParticipants(challengeId);
  }

  @Transactional
  public long invite(ChallengeDtos.InviteRequest req) {
    String type = req.recipientType().trim().toUpperCase();
    if (!type.equals("EMAIL") && !type.equals("PHONE")) {
      throw new IllegalArgumentException("recipientType must be EMAIL or PHONE");
    }
    Long recipientUserId = null;
    if (type.equals("EMAIL")) {
      // If the recipient email belongs to an existing user, link it.
      var u = accountRepo.findUserIdByEmail(req.recipientValue().trim().toLowerCase());
      if (u != null) recipientUserId = u;
    } else {
      var u = accountRepo.findUserIdByPhone(req.recipientValue().trim());
      if (u != null) recipientUserId = u;
    }
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime expiresAt = now.plusDays(15);
    return repo.createInvitation(req.challengeId(), req.senderUserId(), type, req.recipientValue(), recipientUserId, now, expiresAt);
  }

  public List<ChallengeDtos.InvitationRow> invitations(long challengeId) {
    return repo.listInvitations(challengeId);
  }

  public List<ChallengeDtos.TopChallengeRow> topChallenges(int limit) {
    return repo.topChallengesByParticipants(limit);
  }
}


