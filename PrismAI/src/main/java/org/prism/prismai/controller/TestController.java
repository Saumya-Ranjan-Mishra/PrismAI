package org.prism.prismai.controller;

import java.util.List;

import org.prism.prismai.DTO.QueryEmbeddingDto;
import org.prism.prismai.service.interfaces.QueryEmbeddingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
  private final QueryEmbeddingService embeddingService;

  public TestController(QueryEmbeddingService embeddingService) {
    this.embeddingService = embeddingService;
  }

  @GetMapping("/similarQuries")
  public ResponseEntity<List<QueryEmbeddingDto>> getSimilarQueries(@RequestParam String query) {
    List<QueryEmbeddingDto> dto = embeddingService.findSimilarQueries(query, 5);
    return ResponseEntity.status(HttpStatus.OK).body(dto);
  }
}
