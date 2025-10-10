package com.budgetbud.kmp.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.floatOrNull

@Serializable
data class CategoryHistoryLineChartData(
    val name: String,
    val balances: Map<String, Float?> = emptyMap()
)

object CategoryHistoryParser {
    fun parse(jsonString: String): List<CategoryHistoryLineChartData> {
        val jsonArray = Json.parseToJsonElement(jsonString).jsonArray
        return jsonArray.map { jsonElement ->
            val jsonObject = jsonElement.jsonObject
            val name = jsonObject["name"]?.jsonPrimitive?.content ?: ""

            val balances = jsonObject.filterKeys { it != "name" }
                .mapValues {
                    it.value.jsonPrimitive.floatOrNull
                }
            CategoryHistoryLineChartData(name = name, balances = balances)
        }
    }
}