package edu.assignment3.healthtrack.repo;

import edu.assignment3.healthtrack.dto.AccountDtos;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class AccountRepository {
  private final JdbcTemplate jdbc;

  public AccountRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public long createUser(String healthId, String fullName) {
    KeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      PreparedStatement ps = con.prepareStatement(
          "INSERT INTO user_account (health_id, full_name) VALUES (?, ?)",
          Statement.RETURN_GENERATED_KEYS
      );
      ps.setString(1, healthId);
      ps.setString(2, fullName);
      return ps;
    }, kh);
    Number key = kh.getKey();
    if (key == null) throw new IllegalStateException("Failed to create user.");
    return key.longValue();
  }

  public void updateUserName(long userId, String fullName) {
    int n = jdbc.update("UPDATE user_account SET full_name=? WHERE id=?", fullName, userId);
    if (n == 0) throw new IllegalArgumentException("User not found: " + userId);
  }

  public AccountDtos.UserSummary getUser(long userId) {
    return jdbc.query(
        "SELECT id, health_id, full_name, primary_provider_id FROM user_account WHERE id=?",
        rs -> rs.next()
            ? new AccountDtos.UserSummary(
                rs.getLong("id"),
                rs.getString("health_id"),
                rs.getString("full_name"),
                (Long) rs.getObject("primary_provider_id")
            )
            : null,
        userId
    );
  }

  public AccountDtos.UserSummary getUserByHealthId(String healthId) {
    return jdbc.query(
        "SELECT id, health_id, full_name, primary_provider_id FROM user_account WHERE health_id=?",
        rs -> rs.next()
            ? new AccountDtos.UserSummary(
                rs.getLong("id"),
                rs.getString("health_id"),
                rs.getString("full_name"),
                (Long) rs.getObject("primary_provider_id")
            )
            : null,
        healthId
    );
  }

  public AccountDtos.PhoneRow getPhone(long userId) {
    return jdbc.query(
        "SELECT phone, is_verified FROM user_phone WHERE user_id=?",
        rs -> rs.next()
            ? new AccountDtos.PhoneRow(rs.getString("phone"), rs.getBoolean("is_verified"))
            : null,
        userId
    );
  }

  public List<AccountDtos.EmailRow> listEmails(long userId) {
    return jdbc.query(
        "SELECT id, email, is_verified FROM user_email WHERE user_id=? ORDER BY id",
        (rs, i) -> new AccountDtos.EmailRow(rs.getLong("id"), rs.getString("email"), rs.getBoolean("is_verified")),
        userId
    );
  }

  public List<AccountDtos.ProviderRow> listLinkedProviders(long userId) {
    return jdbc.query(
        """
        SELECT p.id, p.license_no, p.display_name, p.specialty, p.is_verified
        FROM user_provider_link upl
        JOIN provider p ON p.id = upl.provider_id
        WHERE upl.user_id = ?
        ORDER BY p.display_name
        """,
        (rs, i) -> new AccountDtos.ProviderRow(
            rs.getLong("id"),
            rs.getString("license_no"),
            rs.getString("display_name"),
            rs.getString("specialty"),
            rs.getBoolean("is_verified")
        ),
        userId
    );
  }

  public List<AccountDtos.ProviderRow> listAllProviders() {
    return jdbc.query(
        "SELECT id, license_no, display_name, specialty, is_verified FROM provider ORDER BY display_name",
        (rs, i) -> new AccountDtos.ProviderRow(
            rs.getLong("id"),
            rs.getString("license_no"),
            rs.getString("display_name"),
            rs.getString("specialty"),
            rs.getBoolean("is_verified")
        )
    );
  }

  public void addEmail(long userId, String email) {
    jdbc.update("INSERT INTO user_email (user_id, email, is_verified) VALUES (?, ?, FALSE)", userId, email);
  }

  public void removeEmail(long userId, String email) {
    int n = jdbc.update("DELETE FROM user_email WHERE user_id=? AND email=?", userId, email);
    if (n == 0) throw new IllegalArgumentException("Email not found for this user.");
  }

  public void upsertPhone(long userId, String phone) {
    // user_phone PK is user_id, so use MySQL upsert
    jdbc.update(
        """
        INSERT INTO user_phone (user_id, phone, is_verified, verified_at)
        VALUES (?, ?, FALSE, NULL)
        ON DUPLICATE KEY UPDATE phone=VALUES(phone), is_verified=FALSE, verified_at=NULL
        """,
        userId, phone
    );
  }

  public void removePhone(long userId) {
    jdbc.update("DELETE FROM user_phone WHERE user_id=?", userId);
  }

  public void linkProvider(long userId, long providerId) {
    jdbc.update("INSERT INTO user_provider_link (user_id, provider_id) VALUES (?, ?)", userId, providerId);
  }

  public void unlinkProvider(long userId, long providerId) {
    jdbc.update("DELETE FROM user_provider_link WHERE user_id=? AND provider_id=?", userId, providerId);
    // If it was primary, clear it.
    jdbc.update("UPDATE user_account SET primary_provider_id=NULL WHERE id=? AND primary_provider_id=?", userId, providerId);
  }

  public void setPrimaryProvider(long userId, Long providerId) {
    if (providerId == null) {
      jdbc.update("UPDATE user_account SET primary_provider_id=NULL WHERE id=?", userId);
      return;
    }
    // Ensure the provider is linked before setting primary (business rule)
    Integer linked = jdbc.query(
        "SELECT 1 FROM user_provider_link WHERE user_id=? AND provider_id=? LIMIT 1",
        rs -> rs.next() ? 1 : null,
        userId, providerId
    );
    if (linked == null) throw new IllegalArgumentException("Provider must be linked before setting as primary care.");
    jdbc.update("UPDATE user_account SET primary_provider_id=? WHERE id=?", providerId, userId);
  }

  public Long findUserIdByEmail(String email) {
    return jdbc.query(
        """
        SELECT u.id
        FROM user_email ue
        JOIN user_account u ON u.id = ue.user_id
        WHERE ue.email = ?
        LIMIT 1
        """,
        rs -> rs.next() ? rs.getLong(1) : null,
        email
    );
  }

  public Long findUserIdByPhone(String phone) {
    return jdbc.query(
        """
        SELECT u.id
        FROM user_phone up
        JOIN user_account u ON u.id = up.user_id
        WHERE up.phone = ?
        LIMIT 1
        """,
        rs -> rs.next() ? rs.getLong(1) : null,
        phone
    );
  }

  public boolean isProviderLinked(long userId, long providerId) {
    Integer v = jdbc.query(
        "SELECT 1 FROM user_provider_link WHERE user_id=? AND provider_id=? LIMIT 1",
        rs -> rs.next() ? 1 : null,
        userId, providerId
    );
    return v != null;
  }
}


