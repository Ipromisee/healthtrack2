package edu.assignment3.healthtrack.service;

import edu.assignment3.healthtrack.dto.SummaryDtos;
import edu.assignment3.healthtrack.repo.SummaryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SummaryService {
  private final SummaryRepository repo;

  public SummaryService(SummaryRepository repo) {
    this.repo = repo;
  }

  public SummaryDtos.AppointmentCountResponse appointmentCount(SummaryDtos.AppointmentCountRequest req) {
    long count = repo.countAppointments(req.userId(), req.startDate(), req.endDate());
    return new SummaryDtos.AppointmentCountResponse(req.userId(), count);
  }

  public SummaryDtos.MetricMonthlyStatsResponse metricStats(SummaryDtos.MetricMonthlyStatsRequest req) {
    return repo.metricStats(req.userId(), req.metricCode(), req.monthStart(), req.monthEnd());
  }

  public SummaryDtos.TopActiveUsersResponse topActiveUsers(int limit) {
    List<SummaryDtos.ActiveUserRow> rows = repo.topActiveUsers(limit);
    return new SummaryDtos.TopActiveUsersResponse(rows);
  }
}


