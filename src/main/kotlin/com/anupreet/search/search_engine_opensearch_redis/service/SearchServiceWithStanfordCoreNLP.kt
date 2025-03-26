package com.anupreet.search.search_engine_opensearch_redis.service

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.springframework.stereotype.Service
import java.util.Properties

@Service
class SearchServiceWithStanfordCoreNLP {

    private val pipeline by lazy { createPipeline() }


    // Create Stanford CoreNLP pipeline
    private fun createPipeline(): StanfordCoreNLP {
        val props = Properties()
        // Remove "sutime" to avoid JAXB dependency if time parsing isn't needed
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner")
        props.setProperty("tokenize.language", "en")
        return StanfordCoreNLP(props)
    }

    fun search(string: String): List<String> {
        return normalizeQuery(string)
    }


    fun normalizeQuery(text: String): List<String> {
        val doc = pipeline.process(text)
        val lemmas = mutableListOf<String>()

        for (sentence in doc.get(CoreAnnotations.SentencesAnnotation::class.java)) {
            for (token in sentence.get(CoreAnnotations.TokensAnnotation::class.java)) {
                val lemma = token.get(CoreAnnotations.LemmaAnnotation::class.java) ?: token.originalText().lowercase()
                val ner = token.get(CoreAnnotations.NamedEntityTagAnnotation::class.java) ?: "O"

                // Decide when to expand abbreviation based on context
                if (ner == "MEDICAL_CONDITION" && lemma.lowercase() == "bp") {
                    lemmas.add("blood pressure") // Expand when it's a medical condition
                } else if (!stopWords.contains(lemma.lowercase())) {
                    lemmas.add(lemma.lowercase()) // Keep the lemma if not a stop word
                }

                // Print recognized entities for debugging
                if (ner != "O") {
                    println("NER: ${token.originalText()} → $ner")
                }
            }
        }
        return lemmas
    }

    val stopWords = setOf(
        "a", "an", "the", "and", "or", "but", "is", "are", "was", "were",
        "has", "have", "had", "of", "in", "on", "at", "to", "for", "with",
        "that", "this", "it", "from", "by", "as", "be", "if"
    )


//    // Function to expand medical abbreviations
//    private fun expandMedicalTerms(term: String, ner: String): String {
//        // Dynamically fetch from Redis or PostgreSQL (pseudo-code)
//        val medicalTerms = mapOf(
//            "bp" to "blood pressure",
//            "hr" to "heart rate",
//            "mi" to "myocardial infarction",
//            "htn" to "hypertension"
//        )
//        return medicalTerms.getOrDefault(term, term)
//    }

//    fun normalizeQuery(text: String): List<String> {
//        tokenize(text).let { tokens ->
//            lemmatize(tokens).let { lemmas ->
//                return lemmas.filterNot { stopWords.contains(it) }
//            }
//        }
//    }


    //
//    val lemmatizationMap = mapOf(
//        "children" to "child",
//        "men" to "man",
//        "women" to "woman",
//        "running" to "run",
//        "ran" to "run",
//        "went" to "go",
//        "feet" to "foot",
//        "better" to "good",
//        "worse" to "bad",
//        "cars" to "car",
//        "buses" to "bus",
//        "teeth" to "tooth"
//    )


//
//    fun lemmatize(word: String): String {
//        return lemmatizationMap.getOrDefault(word, word) // Use root form if available
//    }
//
//    // Function to tokenize, remove stop words, and lemmatize
//    fun processText(text: String): List<String> {
//        return text
//            .lowercase() // Normalize to lowercase
//            .replace(Regex("[^a-zA-Z0-9\\s]"), "") // Remove special chars
//            .split("\\s+".toRegex()) // Split by spaces
//            .filter { it.isNotBlank() } // Remove empty tokens
//            .filterNot { stopWords.contains(it) } // Remove stop words
//            .map { word -> lemmatize(word) } // Lemmatize words
//    }
}