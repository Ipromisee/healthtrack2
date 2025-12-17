package edu.assignment3.healthtrack.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public final class SqlUtil {
  private SqlUtil() {}

  public static Timestamp ts(LocalDateTime dt) {
    return dt == null ? null : Timestamp.valueOf(dt);
  }
}


