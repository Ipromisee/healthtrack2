package edu.assignment3.healthtrack.repo;

import edu.assignment3.healthtrack.dto.AppointmentDtos;
import edu.assignment3.healthtrack.util.SqlUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AppointmentRepository {
  private final JdbcTemplate jdbc;

  public AppointmentRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public long createAppointment(Long userId, Long providerId, LocalDateTime scheduledAt, String appointmentType, String memo) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      PreparedStatement ps = con.prepareStatement(
          """
          INSERT INTO appointment (user_id, provider_id, scheduled_at, appointment_type, memo, status)
          VALUES (?, ?, ?, ?, ?, 'SCHEDULED')
          """,
          Statement.RETURN_GENERATED_KEYS
      );
      ps.setLong(1, userId);
      ps.setLong(2, providerId);
      ps.setTimestamp(3, SqlUtil.ts(scheduledAt));
      ps.setString(4, appointmentType);
      ps.setString(5, memo);
      return ps;
    }, kh);
    Number key = kh.getKey();
    if (key == null) throw new IllegalStateException("Failed to create appointment.");
    return key.longValue();
  }

  public AppointmentRowForCancel getForCancel(long appointmentId) {
    return jdbc.query(
        "SELECT id, scheduled_at, status FROM appointment WHERE id=?",
        rs -> rs.next()
            ? new AppointmentRowForCancel(
                rs.getLong("id"),
                rs.getTimestamp("scheduled_at").toLocalDateTime(),
                rs.getString("status")
            )
            : null,
        appointmentId
    );
  }

  public void markCancelled(long appointmentId, LocalDateTime cancelledAt, String reason) {
    int n = jdbc.update("UPDATE appointment SET status='CANCELLED' WHERE id=? AND status='SCHEDULED'", appointmentId);
    if (n == 0) throw new IllegalArgumentException("Appointment not found or not cancellable (must be SCHEDULED).");
    jdbc.update(
        "INSERT INTO appointment_cancellation (appointment_id, cancelled_at, cancel_reason) VALUES (?, ?, ?)",
        appointmentId, SqlUtil.ts(cancelledAt), reason
    );
  }

  public ProviderAppointmentForCancel getForProviderCancel(long appointmentId) {
    return jdbc.query(
        "SELECT id, provider_id, status FROM appointment WHERE id=?",
        rs -> rs.next()
            ? new ProviderAppointmentForCancel(
                rs.getLong("id"),
                rs.getLong("provider_id"),
                rs.getString("status")
            )
            : null,
        appointmentId
    );
  }

  public void markCancelledByProvider(long appointmentId, LocalDateTime cancelledAt, String reason) {
    int n = jdbc.update("UPDATE appointment SET status='CANCELLED' WHERE id=? AND status='SCHEDULED'", appointmentId);
    if (n == 0) throw new IllegalArgumentException("Appointment not found or not cancellable (must be SCHEDULED).");
    jdbc.update(
        "INSERT INTO appointment_cancellation (appointment_id, cancelled_at, cancel_reason) VALUES (?, ?, ?)",
        appointmentId, SqlUtil.ts(cancelledAt), reason
    );
  }

  public List<ProviderAppointmentRow> listForProvider(long providerId) {
    return jdbc.query(
        """
        SELECT a.id AS appointment_id,
               a.user_id,
               u.health_id AS user_health_id,
               u.full_name AS user_name,
               a.scheduled_at,
               a.appointment_type,
               a.status,
               a.memo,
               ac.cancel_reason
        FROM appointment a
        JOIN user_account u ON u.id = a.user_id
        LEFT JOIN appointment_cancellation ac ON ac.appointment_id = a.id
        WHERE a.provider_id = ?
        ORDER BY a.scheduled_at DESC, a.id DESC
        """,
        (rs, i) -> new ProviderAppointmentRow(
            rs.getLong("appointment_id"),
            rs.getLong("user_id"),
            rs.getString("user_health_id"),
            rs.getString("user_name"),
            rs.getTimestamp("scheduled_at").toLocalDateTime(),
            rs.getString("appointment_type"),
            rs.getString("status"),
            rs.getString("memo"),
            rs.getString("cancel_reason")
        ),
        providerId
    );
  }

  public List<PatientRow> listPatientsForProvider(long providerId) {
    return jdbc.query(
        """
        SELECT u.id AS user_id, u.health_id, u.full_name
        FROM user_provider_link upl
        JOIN user_account u ON u.id = upl.user_id
        WHERE upl.provider_id = ?
        ORDER BY u.full_name, u.id
        """,
        (rs, i) -> new PatientRow(
            rs.getLong("user_id"),
            rs.getString("health_id"),
            rs.getString("full_name")
        ),
        providerId
    );
  }

  public boolean providerHasPatient(long providerId, long userId) {
    Integer v = jdbc.query(
        "SELECT 1 FROM user_provider_link WHERE provider_id=? AND user_id=? LIMIT 1",
        rs -> rs.next() ? 1 : null,
        providerId, userId
    );
    return v != null;
  }

  public List<AppointmentDtos.AppointmentRow> search(AppointmentDtos.AppointmentSearchRequest req) {
    StringBuilder sql = new StringBuilder(
        """
        SELECT a.id, a.user_id, a.provider_id, p.display_name AS provider_name,
               a.scheduled_at, a.appointment_type, a.memo, a.status,
               ac.cancel_reason
        FROM appointment a
        JOIN provider p ON p.id = a.provider_id
        LEFT JOIN appointment_cancellation ac ON ac.appointment_id = a.id
        JOIN user_account u ON u.id = a.user_id
        WHERE 1=1
        """
    );
    List<Object> args = new ArrayList<>();

    if (req.healthId() != null && !req.healthId().isBlank()) {
      sql.append(" AND u.health_id = ?");
      args.add(req.healthId());
    }
    if (req.providerId() != null) {
      sql.append(" AND a.provider_id = ?");
      args.add(req.providerId());
    }
    if (req.appointmentType() != null && !req.appointmentType().isBlank()) {
      sql.append(" AND a.appointment_type = ?");
      args.add(req.appointmentType());
    }
    if (req.start() != null) {
      sql.append(" AND a.scheduled_at >= ?");
      args.add(SqlUtil.ts(req.start()));
    }
    if (req.end() != null) {
      sql.append(" AND a.scheduled_at <= ?");
      args.add(SqlUtil.ts(req.end()));
    }

    sql.append(" ORDER BY a.scheduled_at DESC, a.id DESC");

    return jdbc.query(
        sql.toString(),
        (rs, i) -> new AppointmentDtos.AppointmentRow(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getLong("provider_id"),
            rs.getString("provider_name"),
            rs.getTimestamp("scheduled_at").toLocalDateTime(),
            rs.getString("appointment_type"),
            rs.getString("memo"),
            rs.getString("status"),
            rs.getString("cancel_reason")
        ),
        args.toArray()
    );
  }

  public record AppointmentRowForCancel(long id, LocalDateTime scheduledAt, String status) {}

  public record ProviderAppointmentForCancel(long appointmentId, long providerId, String status) {}

  public record ProviderAppointmentRow(
      long appointmentId,
      long userId,
      String userHealthId,
      String userName,
      LocalDateTime scheduledAt,
      String appointmentType,
      String status,
      String memo,
      String cancelReason
  ) {}

  public record PatientRow(long userId, String healthId, String fullName) {}
}


