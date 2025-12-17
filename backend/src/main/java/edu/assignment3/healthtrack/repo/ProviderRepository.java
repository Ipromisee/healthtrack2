package edu.assignment3.healthtrack.repo;

import edu.assignment3.healthtrack.dto.ProviderDtos;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProviderRepository {
  private final JdbcTemplate jdbc;

  public ProviderRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public ProviderDtos.ProviderLoginResponse login(String licenseNo, String loginCode) {
    return jdbc.query(
        """
        SELECT p.id AS provider_id, p.license_no, p.display_name, p.specialty
        FROM `provider` p
        JOIN `provider_account` pa ON pa.`provider_id` = p.`id`
        WHERE p.`license_no` = ?
          AND pa.`login_code` = ?
        """,
        rs -> rs.next()
            ? new ProviderDtos.ProviderLoginResponse(
                rs.getLong("provider_id"),
                rs.getString("license_no"),
                rs.getString("display_name"),
                rs.getString("specialty")
            )
            : null,
        licenseNo, loginCode
    );
  }
}


