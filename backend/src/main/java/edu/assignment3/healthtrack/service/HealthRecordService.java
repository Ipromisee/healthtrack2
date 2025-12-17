package edu.assignment3.healthtrack.service;

import edu.assignment3.healthtrack.dto.HealthRecordDtos;
import edu.assignment3.healthtrack.repo.HealthRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HealthRecordService {
  private final HealthRecordRepository repo;

  public HealthRecordService(HealthRecordRepository repo) {
    this.repo = repo;
  }

  public HealthRecordDtos.MetricTypeListResponse metricTypes() {
    List<HealthRecordDtos.MetricTypeRow> rows = repo.listMetricTypes();
    return new HealthRecordDtos.MetricTypeListResponse(rows);
  }

  public HealthRecordDtos.CreateHealthRecordResponse create(HealthRecordDtos.CreateHealthRecordRequest req) {
    String code = req.metricCode().trim();
    Long metricTypeId = repo.findMetricTypeIdByCode(code);
    if (metricTypeId == null) throw new IllegalArgumentException("Unknown metricCode: " + code);
    long id = repo.insertHealthRecord(req.userId(), metricTypeId, req.recordedAt(), req.metricValue(), req.note());
    return new HealthRecordDtos.CreateHealthRecordResponse(id);
  }
}


