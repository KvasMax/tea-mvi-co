package max.mini.mvi.elm.api

import max.mini.mvi.elm.api.model.RealRepository
import max.mini.mvi.elm.api.model.Repository

object RepositoryFactory {
    fun createRepository(): Repository = RealRepository()
}