package edu.assignment3.healthtrack.repo;

import edu.assignment3.healthtrack.dto.HealthRecordDtos;
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
public class HealthRecordRepository {
  private final JdbcTemplate jdbc;

  public HealthRecordRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<HealthRecordDtos.MetricTypeRow> listMetricTypes() {
    return jdbc.query(
        "SELECT id, code, display_name, unit FROM metric_type ORDER BY code",
        (rs, i) -> new HealthRecordDtos.MetricTypeRow(
            rs.getLong("id"),
            rs.getString("code"),
            rs.getString("display_name"),
            rs.getString("unit")
        )
    );
  }

  public Long findMetricTypeIdByCode(String metricCode) {
    return jdbc.query(
        "SELECT id FROM metric_type WHERE code=?",
        rs -> rs.next() ? rs.getLong(1) : null,
        metricCode
    );
  }

  public long insertHealthRecord(long userId, long metricTypeId, LocalDateTime recordedAt, double metricValue, String note) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      PreparedStatement ps = con.prepareStatement(
          """
          INSERT INTO health_record (user_id, metric_type_id, recorded_at, metric_value, note)
          VALUES (?, ?, ?, ?, ?)
          """,
          Statement.RETURN_GENERATED_KEYS
      );
      ps.setLong(1, userId);
      ps.setLong(2, metricTypeId);
      ps.setTimestamp(3, SqlUtil.ts(recordedAt));
      ps.setDouble(4, metricValue);
      ps.setString(5, note);
      return ps;
    }, kh);
    Number key = kh.getKey();
    if (key == null) throw new IllegalStateException("Failed to insert health record.");
    return key.longValue();
  }

  public java.util.List<PatientHealthRecordRow> listRecentForUser(long userId, int limit) {
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
        (rs, i) -> new PatientHealthRecordRow(
            rs.getLong("id"),
            rs.getString("metric_code"),
            rs.getString("metric_name"),
            rs.getString("unit"),
            rs.getTimestamp("recorded_at").toLocalDateTime(),
            rs.getDouble("metric_value"),
            rs.getString("note")
        ),
        userId, limit
    );
  }

  public record PatientHealthRecordRow(
      long id,
      String metricCode,
      String metricName,
      String unit,
      java.time.LocalDateTime recordedAt,
      double value,
      String note
  ) {}
}


