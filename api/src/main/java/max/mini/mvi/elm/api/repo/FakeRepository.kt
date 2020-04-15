package max.mini.mvi.elm.api.repo

import com.github.javafaker.Faker
import kotlinx.coroutines.delay
import max.mini.mvi.elm.api.dto.UserInfoDto
import max.mini.mvi.elm.utils.Either
import kotlin.random.Random

internal class FakeRepository : Repository {

    private val faker = Faker()

    private val cachedItems = mutableListOf<UserInfoDto>()

    override suspend fun getUsersForPage(
        page: Int
    ): Either<List<UserInfoDto>, Throwable> {
        delay(500)
        if (page == 0) {
            cachedItems.clear()
        }
        val listSize = 20
        return Either.Left(
            ArrayList<UserInfoDto>(listSize).apply {
                repeat(listSize) {
                    val name = faker.witcher().character()
                    add(
                        UserInfoDto(
                            id = Random.nextInt(),
                            name = name,
                            email = faker.internet().emailAddress(
                                name.replace(
                                    " ",
                                    "_"
                                ).toLowerCase()
                            ),
                            phone = faker.phoneNumber().cellPhone(),
                            website = faker.internet().publicIpV4Address()
                        )
                    )
                }
            }.also {
                cachedItems.addAll(it)
            }
        )
    }

    override suspend fun getUserInfo(
        id: Int
    ): Either<UserInfoDto, Throwable> {
        delay(500)
        return cachedItems.firstOrNull {
            it.id == id
        }?.let {
            Either.Left<UserInfoDto, Throwable>(it)
        } ?: Either.Right<UserInfoDto, Throwable>(
            IllegalArgumentException("There is no user with the specified id")
        )
    }

}