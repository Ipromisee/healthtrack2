package edu.assignment3.healthtrack.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SearchRepository {
  private final JdbcTemplate jdbc;

  public SearchRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<HealthRecordRow> searchHealthRecords(String healthId, String metricCode, LocalDateTime start, LocalDateTime end) {
    StringBuilder sql = new StringBuilder(
        """
        SELECT hr.id, u.health_id, u.full_name, mt.code AS metric_code, mt.display_name, mt.unit,
               hr.recorded_at, hr.metric_value, hr.note
        FROM health_record hr
        JOIN user_account u ON u.id = hr.user_id
        JOIN metric_type mt ON mt.id = hr.metric_type_id
        WHERE 1=1
        """
    );
    List<Object> args = new ArrayList<>();

    if (healthId != null && !healthId.isBlank()) {
      sql.append(" AND u.health_id = ?");
      args.add(healthId);
    }
    if (metricCode != null && !metricCode.isBlank()) {
      sql.append(" AND mt.code = ?");
      args.add(metricCode);
    }
    if (start != null) {
      sql.append(" AND hr.recorded_at >= ?");
      args.add(java.sql.Timestamp.valueOf(start));
    }
    if (end != null) {
      sql.append(" AND hr.recorded_at <= ?");
      args.add(java.sql.Timestamp.valueOf(end));
    }

    sql.append(" ORDER BY hr.recorded_at DESC, hr.id DESC");

    return jdbc.query(
        sql.toString(),
        (rs, i) -> new HealthRecordRow(
            rs.getLong("id"),
            rs.getString("health_id"),
            rs.getString("full_name"),
            rs.getString("metric_code"),
            rs.getString("display_name"),
            rs.getString("unit"),
            rs.getTimestamp("recorded_at").toLocalDateTime(),
            rs.getDouble("metric_value"),
            rs.getString("note")
        ),
        args.toArray()
    );
  }

  public record HealthRecordRow(
      long id,
      String healthId,
      String fullName,
      String metricCode,
      String metricName,
      String unit,
      LocalDateTime recordedAt,
      double value,
      String note
  ) {}
}


