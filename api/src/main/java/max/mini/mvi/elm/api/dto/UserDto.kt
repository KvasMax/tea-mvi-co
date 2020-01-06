package max.mini.mvi.elm.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "name") val name: String? = null,
    @Json(name = "email") val email: String? = null
)