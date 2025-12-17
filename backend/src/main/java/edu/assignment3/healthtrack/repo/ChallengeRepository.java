package edu.assignment3.healthtrack.repo;

import edu.assignment3.healthtrack.dto.ChallengeDtos;
import edu.assignment3.healthtrack.util.SqlUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ChallengeRepository {
  private final JdbcTemplate jdbc;

  public ChallengeRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public long createChallenge(long creatorUserId, String goalText, java.time.LocalDate start, java.time.LocalDate end) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      PreparedStatement ps = con.prepareStatement(
          "INSERT INTO challenge (creator_user_id, goal_text, start_date, end_date) VALUES (?, ?, ?, ?)",
          Statement.RETURN_GENERATED_KEYS
      );
      ps.setLong(1, creatorUserId);
      ps.setString(2, goalText);
      ps.setDate(3, java.sql.Date.valueOf(start));
      ps.setDate(4, java.sql.Date.valueOf(end));
      return ps;
    }, kh);
    Number key = kh.getKey();
    if (key == null) throw new IllegalStateException("Failed to create challenge.");
    return key.longValue();
  }

  public List<ChallengeDtos.ChallengeRow> listChallenges() {
    return jdbc.query(
        "SELECT id, creator_user_id, goal_text, start_date, end_date FROM challenge ORDER BY id DESC",
        (rs, i) -> new ChallengeDtos.ChallengeRow(
            rs.getLong("id"),
            rs.getLong("creator_user_id"),
            rs.getString("goal_text"),
            rs.getDate("start_date").toLocalDate(),
            rs.getDate("end_date").toLocalDate()
        )
    );
  }

  public List<ChallengeDtos.ChallengeRow> listChallengesForUser(long userId) {
    return jdbc.query(
        """
        SELECT c.id, c.creator_user_id, c.goal_text, c.start_date, c.end_date
        FROM challenge_participant cp
        JOIN challenge c ON c.id = cp.challenge_id
        WHERE cp.user_id = ?
        ORDER BY c.id DESC
        """,
        (rs, i) -> new ChallengeDtos.ChallengeRow(
            rs.getLong("id"),
            rs.getLong("creator_user_id"),
            rs.getString("goal_text"),
            rs.getDate("start_date").toLocalDate(),
            rs.getDate("end_date").toLocalDate()
        ),
        userId
    );
  }

  public void ensureParticipant(long challengeId, long userId) {
    jdbc.update(
        """
        INSERT INTO challenge_participant (challenge_id, user_id, progress_value, is_completed, completed_at)
        VALUES (?, ?, 0, FALSE, NULL)
        ON DUPLICATE KEY UPDATE challenge_id=challenge_id
        """,
        challengeId, userId
    );
  }

  public void updateProgress(long challengeId, long userId, double progress, boolean isCompleted, LocalDateTime completedAt) {
    int n = jdbc.update(
        """
        UPDATE challenge_participant
        SET progress_value=?, is_completed=?, completed_at=?
        WHERE challenge_id=? AND user_id=?
        """,
        progress, isCompleted, SqlUtil.ts(completedAt), challengeId, userId
    );
    if (n == 0) throw new IllegalArgumentException("Participant not found for this challenge. Join first.");
  }

  public List<ChallengeDtos.ParticipantRow> listParticipants(long challengeId) {
    return jdbc.query(
        """
        SELECT cp.user_id, u.full_name, cp.progress_value, cp.is_completed
        FROM challenge_participant cp
        JOIN user_account u ON u.id = cp.user_id
        WHERE cp.challenge_id=?
        ORDER BY cp.is_completed DESC, cp.progress_value DESC, u.full_name
        """,
        (rs, i) -> new ChallengeDtos.ParticipantRow(
            rs.getLong("user_id"),
            rs.getString("full_name"),
            rs.getDouble("progress_value"),
            rs.getBoolean("is_completed")
        ),
        challengeId
    );
  }

  public long createInvitation(long challengeId, long senderUserId, String recipientType, String recipientValue,
                               Long recipientUserId, LocalDateTime initiatedAt, LocalDateTime expiresAt) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      PreparedStatement ps = con.prepareStatement(
          """
          INSERT INTO invitation
            (challenge_id, sender_user_id, recipient_type, recipient_value, recipient_user_id, status, initiated_at, completed_at, expires_at)
          VALUES (?, ?, ?, ?, ?, 'PENDING', ?, NULL, ?)
          """,
          Statement.RETURN_GENERATED_KEYS
      );
      ps.setLong(1, challengeId);
      ps.setLong(2, senderUserId);
      ps.setString(3, recipientType);
      ps.setString(4, recipientValue);
      if (recipientUserId == null) ps.setObject(5, null);
      else ps.setLong(5, recipientUserId);
      ps.setTimestamp(6, SqlUtil.ts(initiatedAt));
      ps.setTimestamp(7, SqlUtil.ts(expiresAt));
      return ps;
    }, kh);
    Number key = kh.getKey();
    if (key == null) throw new IllegalStateException("Failed to create invitation.");
    return key.longValue();
  }

  public List<ChallengeDtos.InvitationRow> listInvitations(long challengeId) {
    return jdbc.query(
        """
        SELECT id, challenge_id, sender_user_id, recipient_type, recipient_value, recipient_user_id,
               status, initiated_at, completed_at, expires_at
        FROM invitation
        WHERE challenge_id=?
        ORDER BY initiated_at DESC, id DESC
        """,
        (rs, i) -> new ChallengeDtos.InvitationRow(
            rs.getLong("id"),
            rs.getLong("challenge_id"),
            rs.getLong("sender_user_id"),
            rs.getString("recipient_type"),
            rs.getString("recipient_value"),
            (Long) rs.getObject("recipient_user_id"),
            rs.getString("status"),
            rs.getTimestamp("initiated_at").toLocalDateTime(),
            rs.getTimestamp("completed_at") == null ? null : rs.getTimestamp("completed_at").toLocalDateTime(),
            rs.getTimestamp("expires_at").toLocalDateTime()
        ),
        challengeId
    );
  }

  public List<ChallengeDtos.TopChallengeRow> topChallengesByParticipants(int limit) {
    return jdbc.query(
        """
        SELECT c.id AS challenge_id, c.goal_text, COUNT(cp.user_id) AS participant_count
        FROM challenge c
        LEFT JOIN challenge_participant cp ON cp.challenge_id = c.id
        GROUP BY c.id, c.goal_text
        ORDER BY participant_count DESC, c.id DESC
        LIMIT ?
        """,
        (rs, i) -> new ChallengeDtos.TopChallengeRow(
            rs.getLong("challenge_id"),
            rs.getString("goal_text"),
            rs.getLong("participant_count")
        ),
        limit
    );
  }
}


