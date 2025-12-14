package com.budgetbud.kmp.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable(with = CategoryHistoryParser::class)
data class CategoryHistoryLineChartData(
    val name: String,
    val balances: Map<String, Float?>
)

object CategoryHistoryParser : KSerializer<CategoryHistoryLineChartData> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("CategoryHistoryLineChartData", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: CategoryHistoryLineChartData) {
        val jsonOutput = encoder as JsonEncoder
        val jsonObject = buildJsonObject {
            put("name", value.name)

            value.balances.forEach { (categoryName, balance) ->
                put(categoryName, balance?.let { JsonPrimitive(it) } ?: JsonNull)
            }
        }
        jsonOutput.encodeJsonElement(jsonObject)
    }

    override fun deserialize(decoder: Decoder): CategoryHistoryLineChartData {
        val jsonElement = decoder.decodeSerializableValue(JsonElement.serializer())
        val jsonObject = jsonElement.jsonObject

        val name = jsonObject["name"]?.jsonPrimitive?.content ?: throw IllegalStateException("Missing 'name' field in category history data.")

        val balances = mutableMapOf<String, Float?>()

        jsonObject.entries.forEach { (key, value) ->
            if (key != "name") {
                val balance: Float? = when (value) {
                    is JsonNull -> null
                    is JsonPrimitive -> value.contentOrNull?.toFloatOrNull()
                    else -> null
                }
                balances[key] = balance
            }
        }

        return CategoryHistoryLineChartData(name, balances)
    }
}