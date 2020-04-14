package max.mini.mvi.elm.api.repo

import com.github.kittinunf.fuel.coroutines.awaitObjectResult
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.moshi.defaultMoshi
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.result.Result
import com.squareup.moshi.Types
import max.mini.mvi.elm.api.dto.UserDto
import max.mini.mvi.elm.api.dto.UserInfoDto
import max.mini.mvi.elm.utils.Either

internal class RealRepository : Repository {

    private val baseUrl = "https://jsonplaceholder.typicode.com/"

    override suspend fun getUsers(): Either<List<UserDto>, Throwable> {
        val result = "${baseUrl}users".httpGet().awaitObjectResult<List<UserDto>>(
            moshiDeserializerOf(
                defaultMoshi.build().adapter(
                    Types.newParameterizedType(List::class.java, UserDto::class.java)
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
        val result = "${baseUrl}users/$id".httpGet()
            .awaitObjectResult(moshiDeserializerOf(UserInfoDto::class.java))
        return when (result) {
            is Result.Failure -> Either.Right(result.error)
            is Result.Success -> Either.Left(result.value)
        }
    }
}