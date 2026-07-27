package com.visiontwin.backend.service;

import com.visiontwin.backend.entity.KnowledgeBaseLayer2;
import com.visiontwin.backend.repository.KnowledgeBaseLayer2Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorStoreService {

    private final KnowledgeBaseLayer2Repository repository;
    private final AIService aiService;

    /**
     * Search the vector database for the top K matching chunks based on semantic similarity.
     */
    public List<KnowledgeBaseLayer2> search(UUID machineId, String queryText, int topK) {
        log.info("Searching vector base for machine {} with query: {}", machineId, queryText);
        
        // 1. Generate query embedding
        double[] queryEmbedding = aiService.getEmbedding(queryText);
        
        // 2. Fetch all vector chunks for the machine
        List<KnowledgeBaseLayer2> allChunks = repository.findByMachineId(machineId);
        if (allChunks.isEmpty()) {
            log.warn("No vector knowledge base found for machine: {}", machineId);
            return List.of();
        }

        // 3. Compute cosine similarities
        List<ScoredChunk> scoredChunks = new ArrayList<>();
        for (KnowledgeBaseLayer2 chunk : allChunks) {
            double similarity = computeCosineSimilarity(queryEmbedding, chunk.getEmbedding());
            scoredChunks.add(new ScoredChunk(chunk, similarity));
        }

        // 4. Sort and return top K
        scoredChunks.sort(Comparator.comparingDouble((ScoredChunk s) -> s.score).reversed());
        
        List<KnowledgeBaseLayer2> results = new ArrayList<>();
        int count = Math.min(topK, scoredChunks.size());
        for (int i = 0; i < count; i++) {
            ScoredChunk sc = scoredChunks.get(i);
            log.info("Match block source: {}, similarity score: {}", sc.chunk.getSource(), sc.score);
            results.add(sc.chunk);
        }
        
        return results;
    }

    private double computeCosineSimilarity(double[] vectorA, double[] vectorB) {
        if (vectorA == null || vectorB == null || vectorA.length != vectorB.length || vectorA.length == 0) {
            return 0.0;
        }
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private static class ScoredChunk {
        final KnowledgeBaseLayer2 chunk;
        final double score;

        ScoredChunk(KnowledgeBaseLayer2 chunk, double score) {
            this.chunk = chunk;
            this.score = score;
        }
    }
}
