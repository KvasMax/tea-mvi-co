package max.mini.mvi.elm.api.model

import max.mini.mvi.elm.api.dto.UserDto
import max.mini.mvi.elm.utils.Either

interface Repository {
    suspend fun getUsers(): Either<List<UserDto>, Throwable>
}
