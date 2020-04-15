package max.mini.mvi.elm.api

import max.mini.mvi.elm.api.repo.FakeRepository
import max.mini.mvi.elm.api.repo.Repository

object RepositoryFactory {
    fun createRepository(): Repository = FakeRepository()
}