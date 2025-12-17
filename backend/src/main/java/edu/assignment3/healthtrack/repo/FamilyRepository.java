package edu.assignment3.healthtrack.repo;

import edu.assignment3.healthtrack.dto.FamilyDtos;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FamilyRepository {
  private final JdbcTemplate jdbc;

  public FamilyRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public boolean shareFamilyGroup(long viewerUserId, long targetUserId) {
    Integer v = jdbc.query(
        """
        SELECT 1
        FROM family_group_member a
        JOIN family_group_member b ON b.family_group_id = a.family_group_id
        WHERE a.user_id = ?
          AND b.user_id = ?
        LIMIT 1
        """,
        rs -> rs.next() ? 1 : null,
        viewerUserId, targetUserId
    );
    return v != null;
  }

  public List<FamilyDtos.FamilyMemberRow> listFamilyMembersForUser(long userId) {
    return jdbc.query(
        """
        SELECT fg.id AS family_group_id,
               fg.group_name,
               u.id AS user_id,
               u.health_id,
               u.full_name,
               fgm.member_role,
               fgm.can_manage
        FROM family_group_member me
        JOIN family_group fg ON fg.id = me.family_group_id
        JOIN family_group_member fgm ON fgm.family_group_id = me.family_group_id
        JOIN user_account u ON u.id = fgm.user_id
        WHERE me.user_id = ?
        ORDER BY fg.id, u.full_name, u.id
        """,
        (rs, i) -> new FamilyDtos.FamilyMemberRow(
            rs.getLong("family_group_id"),
            rs.getString("group_name"),
            rs.getLong("user_id"),
            rs.getString("health_id"),
            rs.getString("full_name"),
            rs.getString("member_role"),
            rs.getBoolean("can_manage")
        ),
        userId
    );
  }

  public List<FamilyDtos.MemberHealthRecordRow> listRecentHealthRecords(long targetUserId, int limit) {
    return jdbc.query(
        """
        SELECT hr.id,
               mt.code AS metric_code,
               mt.display_name AS metric_name,
               mt.unit,
               hr.recorded_at,
               hr.metric_value,
               hr.note
        FROM health_record hr
        JOIN metric_type mt ON mt.id = hr.metric_type_id
        WHERE hr.user_id = ?
        ORDER BY hr.recorded_at DESC, hr.id DESC
        LIMIT ?
        """,
        (rs, i) -> new FamilyDtos.MemberHealthRecordRow(
            rs.getLong("id"),
            rs.getString("metric_code"),
            rs.getString("metric_name"),
            rs.getString("unit"),
            rs.getTimestamp("recorded_at").toLocalDateTime(),
            rs.getDouble("metric_value"),
            rs.getString("note")
        ),
        targetUserId, limit
    );
  }

  public List<FamilyDtos.MemberChallengeRow> listChallengesForUser(long targetUserId) {
    return jdbc.query(
        """
        SELECT c.id AS challenge_id,
               c.goal_text,
               c.start_date,
               c.end_date,
               cp.progress_value,
               cp.is_completed
        FROM challenge_participant cp
        JOIN challenge c ON c.id = cp.challenge_id
        WHERE cp.user_id = ?
        ORDER BY c.id DESC
        """,
        (rs, i) -> new FamilyDtos.MemberChallengeRow(
            rs.getLong("challenge_id"),
            rs.getString("goal_text"),
            rs.getDate("start_date").toLocalDate(),
            rs.getDate("end_date").toLocalDate(),
            rs.getDouble("progress_value"),
            rs.getBoolean("is_completed")
        ),
        targetUserId
    );
  }
}


