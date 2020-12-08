package max.mini.mvi.elm.api.repo

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.interceptors.LogRequestInterceptor
import com.github.kittinunf.fuel.core.interceptors.LogResponseInterceptor
import com.github.kittinunf.fuel.coroutines.awaitObjectResult
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.result.Result
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import max.mini.mvi.elm.api.dto.UserInfoDto
import max.mini.mvi.elm.utils.Either

internal class RealRepository : Repository {

    init {
        FuelManager.instance.apply {
            basePath = "https://reqres.in/api/"
            addRequestInterceptor(
                LogRequestInterceptor
            )
            addResponseInterceptor(
                LogResponseInterceptor
            )
        }
    }

    override suspend fun getUsersForPage(
        page: Int
    ): Either<List<UserInfoDto>, Throwable> {
        val result = "users?page=${page + 1}".httpGet()
            .awaitObjectResult(
                moshiDeserializerOf(
                    UsersResponse::class.java
                )
            )
        return when (result) {
            is Result.Failure -> Either.Right(result.error)
            is Result.Success -> Either.Left(result.value.users)
        }
    }

    override suspend fun getUserInfo(
        id: Int
    ): Either<UserInfoDto, Throwable> {
        val result = "users/$id".httpGet()
            .awaitObjectResult(moshiDeserializerOf(UserResponse::class.java))
        return when (result) {
            is Result.Failure -> Either.Right(result.error)
            is Result.Success -> Either.Left(result.value.user)
        }
    }
}

@JsonClass(generateAdapter = true)
internal data class UsersResponse(
    @Json(name = "data")
    val users: List<UserInfoDto>
)

@JsonClass(generateAdapter = true)
internal data class UserResponse(
    @Json(name = "data")
    val user: UserInfoDto
)