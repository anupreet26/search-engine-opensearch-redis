package com.anupreet.search.search_engine_opensearch_redis.controller

import com.anupreet.search.search_engine_opensearch_redis.service.CombinedSearchService
import com.anupreet.search.search_engine_opensearch_redis.service.SearchService
import com.anupreet.search.search_engine_opensearch_redis.service.SearchServiceWithOpenNLP
import com.anupreet.search.search_engine_opensearch_redis.service.SearchServiceWithStanfordCoreNLP
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/search")
class SearchController(
    val searchServiceWithStanfordCoreNLP: SearchServiceWithStanfordCoreNLP,
    val searchServiceWithOpenNLP: SearchServiceWithOpenNLP,
    val searchService: SearchService,
    val combinedSearchService: CombinedSearchService
    ) {

    @GetMapping("/stanford")
    fun searchWithStanfordCoreNLP(
        @RequestParam query: String
    ): ResponseEntity<Any> {

        // Mandatory Field Check
        if (query.isBlank()) {
            return ResponseEntity
                .badRequest()
                .body(mapOf("status" to "error", "message" to "Query must not be empty"))
        }

        // Length Check
        if (query.length > 100) {
            return ResponseEntity
                .badRequest()
                .body(mapOf("status" to "error", "message" to "Query too long (max 100 chars)"))
        }


        // Sanitization Check (Regex for alphanumeric + space)
        if (!query.matches(Regex("^[a-zA-Z0-9\\s]*$"))) {
            return ResponseEntity
                .badRequest()
                .body(mapOf("status" to "error", "message" to "Query contains invalid characters"))
        }

        // If all checks pass, proceed
        val results = searchServiceWithStanfordCoreNLP.search(query)
        return ResponseEntity.ok(results)
    }

    @GetMapping("/opennlp")
    fun searchWithOpenNLP(
        @RequestParam query: String
    ): ResponseEntity<Any> {

        // Mandatory Field Check
        if (query.isBlank()) {
            return ResponseEntity
                .badRequest()
                .body(mapOf("status" to "error", "message" to "Query must not be empty"))
        }

        // Length Check
        if (query.length > 100) {
            return ResponseEntity
                .badRequest()
                .body(mapOf("status" to "error", "message" to "Query too long (max 100 chars)"))
        }


        // Sanitization Check (Regex for alphanumeric + space)
        if (!query.matches(Regex("^[a-zA-Z0-9\\s]*$"))) {
            return ResponseEntity
                .badRequest()
                .body(mapOf("status" to "error", "message" to "Query contains invalid characters"))
        }

        // If all checks pass, proceed
        val results = searchServiceWithOpenNLP.search(query)
        return ResponseEntity.ok(results)
    }


    // python

        @GetMapping
        fun search(@RequestParam query: String): ResponseEntity<Map<String, String>?> {
            val response = searchService.search(query)
            return if (response != null) {
                ResponseEntity.ok(response)
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
            }
        }

    //combined

    @GetMapping("/combined")
    fun searchCombined
                (@RequestParam query: String): ResponseEntity<Map<String, Any>?> {
        val response = combinedSearchService.search(query)
        return if (response != null) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

}