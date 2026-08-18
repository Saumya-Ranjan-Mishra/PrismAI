package org.prism.prismai.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.prism.prismai.entities.QueryEmbedding;
import org.prism.prismai.services.interfaces.QueryEmbeddingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/embeddings")
public class QueryEmbeddingController {

  private final QueryEmbeddingService queryEmbeddingService;

  public QueryEmbeddingController(QueryEmbeddingService queryEmbeddingService) {
    this.queryEmbeddingService = queryEmbeddingService;
  }

  @PostMapping
  public ResponseEntity<StoredQueryResponse> store(@Valid @RequestBody StoreQueryRequest request) {
    QueryEmbedding stored = queryEmbeddingService.storeQuery(request.message());
    return ResponseEntity.status(HttpStatus.CREATED).body(StoredQueryResponse.from(stored));
  }

  @GetMapping("/similar")
  public ResponseEntity<List<StoredQueryResponse>> similar(@RequestParam String message,
      @RequestParam(defaultValue = "5") int limit) {
    List<StoredQueryResponse> matches = queryEmbeddingService.findSimilarQueries(message, limit)
        .stream()
        .map(StoredQueryResponse::from)
        .toList();
    return ResponseEntity.ok(matches);
  }

  public record StoreQueryRequest(@NotBlank String message) {
  }

  public record StoredQueryResponse(Long id, String query, int dimensions, Instant createdAt) {

    static StoredQueryResponse from(QueryEmbedding entity) {
      return new StoredQueryResponse(
          entity.getId(),
          entity.getQuery(),
          entity.getEmbedding().length,
          entity.getCreatedAt());
    }
  }
}
