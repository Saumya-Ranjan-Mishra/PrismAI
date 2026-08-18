package org.prism.prismai.AI_models;

import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingModelConfig {
  @Bean
  public TransformersEmbeddingModel embeddingModel(
      @Value("${prism.embedding.tokenizer-resource}") String tokenizerResource,
      @Value("${prism.embedding.model-resource}") String modelResource) {
    TransformersEmbeddingModel model = new TransformersEmbeddingModel();
    model.setTokenizerResource(tokenizerResource);
    model.setModelResource(modelResource);
    return model;
  }
}
