package org.prism.prismai.AI_models;

import java.util.Map;

import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingModelFactory {
  private Map<String, TransformersEmbeddingModel> embeddingModels;

  public EmbeddingModelFactory(Map<String, TransformersEmbeddingModel> models) {
    this.embeddingModels = models;
  }

  public TransformersEmbeddingModel getModel(String modelName) {
    if (modelName.isBlank() || modelName.isEmpty()) {
      throw new IllegalArgumentException("model name must be provided for a valid transformer embedding model");
    }

    TransformersEmbeddingModel model = embeddingModels.get(modelName);
    if (model == null) {
      throw new IllegalArgumentException("Invalid model name, supporeted models are:" + embeddingModels.keySet());
    }

    return model;
  }
}
