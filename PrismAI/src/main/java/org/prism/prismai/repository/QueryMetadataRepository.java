package org.prism.prismai.repository;

import org.prism.prismai.entities.QueryMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QueryMetadataRepository extends JpaRepository<QueryMetadata, Long> {

  Optional<QueryMetadata> findByQueryEmbedding_Id(Long queryEmbeddingId);

}
