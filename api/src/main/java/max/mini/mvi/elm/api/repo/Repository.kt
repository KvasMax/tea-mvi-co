package max.mini.mvi.elm.api.repo

import max.mini.mvi.elm.api.dto.UserInfoDto
import max.mini.mvi.elm.utils.Either

interface Repository {
    suspend fun getUsersForPage(page: Int): Either<List<UserInfoDto>, Throwable>
    suspend fun getUserInfo(id: Int): Either<UserInfoDto, Throwable>
}
