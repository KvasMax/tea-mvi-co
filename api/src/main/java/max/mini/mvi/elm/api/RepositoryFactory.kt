package max.mini.mvi.elm.api

import max.mini.mvi.elm.api.repo.RealRepository
import max.mini.mvi.elm.api.repo.Repository

object RepositoryFactory {
    fun createRepository(): Repository = RealRepository()
}