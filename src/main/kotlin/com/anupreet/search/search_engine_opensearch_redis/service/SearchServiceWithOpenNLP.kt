package com.anupreet.search.search_engine_opensearch_redis.service

import opennlp.tools.lemmatizer.LemmatizerME
import opennlp.tools.lemmatizer.LemmatizerModel
import opennlp.tools.tokenize.SimpleTokenizer
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class SearchServiceWithOpenNLP {

    val stopWords = setOf(
        "a", "an", "the", "and", "or", "but", "is", "are", "was", "were",
        "has", "have", "had", "of", "in", "on", "at", "to", "for", "with",
        "that", "this", "it", "from", "by", "as", "be", "if"
    )

    fun search(string: String): List<String> {
        return normalizeQuery(string)
    }

    fun normalizeQuery(text: String): List<String> {
        tokenize(text).let { tokens ->
            lemmatize(tokens).let { lemmas ->
                return lemmas.filterNot { stopWords.contains(it) }
            }
        }
    }

    fun tokenize(text: String): Array<String> {
        val tokenizer = SimpleTokenizer.INSTANCE
        return tokenizer.tokenize(text)
    }

    fun lemmatize(tokens: Array<String>): List<String> {
        // Load the binary lemmatizer model
        val inputStream: InputStream? =
            object {}.javaClass.getResourceAsStream("/models/opennlp-en-ud-ewt-lemmas-1.2-2.5.0.bin")

        if (inputStream == null) {
            throw Exception("Lemmatizer model file not found!")
        }

        val lemmatizerModel = LemmatizerModel(inputStream)
        val lemmatizer = LemmatizerME(lemmatizerModel)

        // Dummy POS tags (for testing)
        val posTags = Array(tokens.size) { "VB" } // Set part-of-speech as verbs

        // Lemmatize the tokens
        return lemmatizer.lemmatize(tokens, posTags).toList()
    }
}