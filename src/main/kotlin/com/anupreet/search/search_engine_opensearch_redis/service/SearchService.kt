package com.anupreet.search.search_engine_opensearch_redis.service
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient


@Service
class SearchService(private val webClient: WebClient) {

    fun search(query: String): Map<String, String>? {
        return try {
            val response = webClient
                .get()
                .uri("/normalize?query={query}", query)
                .retrieve()
                .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
                .block()

            println("NER Response: $response")

            // Safely extract the inner map
            @Suppress("UNCHECKED_CAST")
            val entities = response?.get("entities") as? Map<String, String>

            entities
        } catch (e: Exception) {
            println("Error calling NER API: ${e.message}")
            null
        }
    }
}