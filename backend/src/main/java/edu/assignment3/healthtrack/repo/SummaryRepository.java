package edu.assignment3.healthtrack.repo;

import edu.assignment3.healthtrack.dto.SummaryDtos;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public class SummaryRepository {
  private final JdbcTemplate jdbc;

  public SummaryRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public long countAppointments(long userId, LocalDate startDate, LocalDate endDate) {
    // inclusive range, by scheduled_at date
    Long count = jdbc.queryForObject(
        """
        SELECT COUNT(*)
        FROM appointment
        WHERE user_id=?
          AND DATE(scheduled_at) >= ?
          AND DATE(scheduled_at) <= ?
        """,
        Long.class,
        userId, java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate)
    );
    return count == null ? 0 : count;
  }

  public SummaryDtos.MetricMonthlyStatsResponse metricStats(long userId, String metricCode, LocalDate monthStart, LocalDate monthEnd) {
    return jdbc.query(
        """
        SELECT AVG(hr.metric_value) AS avg_v, MIN(hr.metric_value) AS min_v, MAX(hr.metric_value) AS max_v
        FROM health_record hr
        JOIN metric_type mt ON mt.id = hr.metric_type_id
        WHERE hr.user_id=?
          AND mt.code=?
          AND DATE(hr.recorded_at) >= ?
          AND DATE(hr.recorded_at) <= ?
        """,
        rs -> {
          if (!rs.next()) return new SummaryDtos.MetricMonthlyStatsResponse(userId, metricCode, null, null, null);
          Double avg = toDouble(rs.getObject("avg_v"));
          Double min = toDouble(rs.getObject("min_v"));
          Double max = toDouble(rs.getObject("max_v"));
          return new SummaryDtos.MetricMonthlyStatsResponse(userId, metricCode, avg, min, max);
        },
        userId, metricCode, java.sql.Date.valueOf(monthStart), java.sql.Date.valueOf(monthEnd)
    );
  }

  private static Double toDouble(Object v) {
    if (v == null) return null;
    if (v instanceof Double d) return d;
    if (v instanceof Float f) return f.doubleValue();
    if (v instanceof Integer i) return i.doubleValue();
    if (v instanceof Long l) return l.doubleValue();
    if (v instanceof BigDecimal bd) return bd.doubleValue();
    if (v instanceof Number n) return n.doubleValue();
    throw new IllegalArgumentException("Unsupported numeric type: " + v.getClass());
  }

  public List<SummaryDtos.ActiveUserRow> topActiveUsers(int limit) {
    // Activity = health_record count + completed challenge count (reported separately)
    return jdbc.query(
        """
        SELECT u.id AS user_id,
               u.full_name,
               COALESCE(hr.cnt, 0) AS record_count,
               COALESCE(cc.cnt, 0) AS completed_challenges
        FROM user_account u
        LEFT JOIN (
          SELECT user_id, COUNT(*) AS cnt
          FROM health_record
          GROUP BY user_id
        ) hr ON hr.user_id = u.id
        LEFT JOIN (
          SELECT user_id, COUNT(*) AS cnt
          FROM challenge_participant
          WHERE is_completed = TRUE
          GROUP BY user_id
        ) cc ON cc.user_id = u.id
        ORDER BY (COALESCE(hr.cnt,0) + COALESCE(cc.cnt,0)) DESC,
                 record_count DESC,
                 completed_challenges DESC,
                 u.id ASC
        LIMIT ?
        """,
        (rs, i) -> new SummaryDtos.ActiveUserRow(
            rs.getLong("user_id"),
            rs.getString("full_name"),
            rs.getLong("record_count"),
            rs.getLong("completed_challenges")
        ),
        limit
    );
  }
}


