package max.mini.mvi.elm.api.repo

import max.mini.mvi.elm.api.dto.UserDto
import max.mini.mvi.elm.api.dto.UserInfoDto
import max.mini.mvi.elm.utils.Either

interface Repository {
    suspend fun getUsers(): Either<List<UserDto>, Throwable>
    suspend fun getUserInfo(id: Int): Either<UserInfoDto, Throwable>
}
