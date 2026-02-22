package com.cirin0.worktimetracker.features.company.data.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class CompanyDetail(
    val id: Int,
    val name: String,
    val email: String?,
    val phone: String?,
    val address: String?,
    val description: String?,
    val logo: String?,
    val latitude: String?,
    val longitude: String?,
    @SerializedName("radius_meters")
    val radiusMeters: Int?,
    val manager: BaseUser,
    @JsonAdapter(EmployeesDeserializer::class)
    val employees: List<BaseUser>?,
    @SerializedName("employee_count") val usersCount: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
) {
    fun getLatitudeAsDouble(): Double? = latitude?.toDoubleOrNull()

    fun getLongitudeAsDouble(): Double? = longitude?.toDoubleOrNull()

    fun getEmployeesList(): List<BaseUser> = employees ?: emptyList()
}

data class BaseUser(
    val id: Int,
    val name: String,
    val email: String,
    val avatar: String?
)

class EmployeesDeserializer : JsonDeserializer<List<BaseUser>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<BaseUser>? {
        if (json == null || json.isJsonNull) {
            return null
        }

        return when {
            json.isJsonArray -> {
                json.asJsonArray.map { element ->
                    context!!.deserialize(element, BaseUser::class.java)
                }
            }

            json.isJsonObject -> {
                json.asJsonObject.entrySet().map { (_, value) ->
                    context!!.deserialize(value, BaseUser::class.java)
                }
            }

            else -> null
        }
    }
}

