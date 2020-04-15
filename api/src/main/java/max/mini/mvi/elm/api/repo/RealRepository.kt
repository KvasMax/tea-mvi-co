package max.mini.mvi.elm.api.repo

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.interceptors.LogRequestInterceptor
import com.github.kittinunf.fuel.core.interceptors.LogResponseInterceptor
import com.github.kittinunf.fuel.coroutines.awaitObjectResult
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.moshi.defaultMoshi
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.result.Result
import com.squareup.moshi.Types
import max.mini.mvi.elm.api.dto.UserInfoDto
import max.mini.mvi.elm.utils.Either

internal class RealRepository : Repository {

    init {
        FuelManager.instance.apply {
            basePath = "https://jsonplaceholder.typicode.com/"
            addRequestInterceptor(
                LogRequestInterceptor
            )
            addResponseInterceptor(
                LogResponseInterceptor
            )
        }
    }

    override suspend fun getUsers(): Either<List<UserInfoDto>, Throwable> {
        val result = "users".httpGet()
            .awaitObjectResult<List<UserInfoDto>>(
                moshiDeserializerOf(
                    defaultMoshi.build().adapter(
                        Types.newParameterizedType(List::class.java, UserInfoDto::class.java)
                    )
                )
            )
        return when (result) {
            is Result.Failure -> Either.Right(result.error)
            is Result.Success -> Either.Left(result.value)
        }
    }

    override suspend fun getUserInfo(
        id: Int
    ): Either<UserInfoDto, Throwable> {
        val result = "users/$id".httpGet()
            .awaitObjectResult(moshiDeserializerOf(UserInfoDto::class.java))
        return when (result) {
            is Result.Failure -> Either.Right(result.error)
            is Result.Success -> Either.Left(result.value)
        }
    }
}