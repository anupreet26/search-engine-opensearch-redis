package com.anupreet.search.search_engine_opensearch_redis.service

import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.ling.CoreAnnotations
import java.util.*

@Service
class CombinedSearchService(private val webClient: WebClient) {

    private val pipeline by lazy { createPipeline() }

    // Create Stanford CoreNLP pipeline
    private fun createPipeline(): StanfordCoreNLP {
        val props = Properties()
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner")
        props.setProperty("tokenize.language", "en")
        return StanfordCoreNLP(props)
    }

    // Call NER API and combine with Stanford NLP output
    fun search(query: String): Map<String, Any> {
        val nerEntities = callNERApi(query) ?: emptyMap<String, String>()
        val lemmas = normalizeQuery(query)

        return mapOf(
            "nerEntities" to nerEntities,
            "lemmas" to lemmas
        )
    }

    // Call NER API using WebClient
    private fun callNERApi(query: String): Map<String, String>? {
        return try {
            val response = webClient
                .get()
                .uri("/normalize?query={query}", query)
                .retrieve()
                .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
                .block()

            @Suppress("UNCHECKED_CAST")
            response?.get("entities") as? Map<String, String>
        } catch (e: Exception) {
            println("Error calling NER API: ${e.message}")
            null
        }
    }

    // Stanford CoreNLP processing
    private fun normalizeQuery(text: String): List<String> {
        val doc = pipeline.process(text)
        val lemmas = mutableListOf<String>()

        for (sentence in doc.get(CoreAnnotations.SentencesAnnotation::class.java)) {
            for (token in sentence.get(CoreAnnotations.TokensAnnotation::class.java)) {
                val lemma = token.get(CoreAnnotations.LemmaAnnotation::class.java) ?: token.originalText().lowercase()
                val ner = token.get(CoreAnnotations.NamedEntityTagAnnotation::class.java) ?: "O"

                // Expand "bp" only if identified as a medical condition
                if (ner == "MEDICAL_CONDITION" && lemma.lowercase() == "bp") {
                    lemmas.add("blood pressure")
                } else if (!stopWords.contains(lemma.lowercase())) {
                    lemmas.add(lemma.lowercase())
                }

                // Log recognized entities for debugging
                if (ner != "O") {
                    println("NER: ${token.originalText()} → $ner")
                }
            }
        }
        return lemmas
    }

    private val stopWords = setOf(
        "a", "an", "the", "and", "or", "but", "is", "are", "was", "were",
        "has", "have", "had", "of", "in", "on", "at", "to", "for", "with",
        "that", "this", "it", "from", "by", "as", "be", "if"
    )
}