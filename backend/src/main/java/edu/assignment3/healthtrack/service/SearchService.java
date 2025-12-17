package edu.assignment3.healthtrack.service;

import edu.assignment3.healthtrack.repo.SearchRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SearchService {
  private final SearchRepository repo;

  public SearchService(SearchRepository repo) {
    this.repo = repo;
  }

  public List<SearchRepository.HealthRecordRow> healthRecords(String healthId, String metricCode, LocalDateTime start, LocalDateTime end) {
    return repo.searchHealthRecords(healthId, metricCode, start, end);
  }
}


