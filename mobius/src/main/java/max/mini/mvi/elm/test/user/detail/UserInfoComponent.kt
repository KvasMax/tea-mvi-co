package max.mini.mvi.elm.test.user.detail

import com.spotify.mobius.Connectable
import com.spotify.mobius.Mobius
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.Update
import com.spotify.mobius.android.AndroidLogger
import dagger.*
import max.mini.mvi.elm.test.ContextProvider
import max.mini.mvi.elm.test.FragmentNavigatorProvider
import max.mini.mvi.elm.test.RepositoryProvider
import max.mini.mvi.elm.test.UserInfoResultEmitterProvider
import max.mini.mvi.elm.test.base.FragmentControllerDelegate

@Component(
    dependencies = [
        ContextProvider::class,
        RepositoryProvider::class,
        FragmentNavigatorProvider::class,
        UserInfoResultEmitterProvider::class
    ],
    modules = [UserInfoModule::class]
)
interface UserInfoComponent {

    @Component.Factory
    interface Factory {
        fun create(
            contextProvider: ContextProvider,
            repositoryProvider: RepositoryProvider,
            fragmentNavigatorProvider: FragmentNavigatorProvider,
            userInfoResultEmitterProvider: UserInfoResultEmitterProvider,
            @BindsInstance userId: Int
        ): UserInfoComponent
    }

    fun inject(fragment: UserInfoFragment)

}

@Module(includes = [UserInfoModule.Binding::class])
class UserInfoModule {

    @Provides
    fun provideLoop(
        effectHandler: Connectable<UserInfoEffect, UserInfoEvent>
    ): MobiusLoop.Builder<UserInfoDataModel, UserInfoEvent, UserInfoEffect> {
        val loop =
            Mobius.loop(Update<UserInfoDataModel, UserInfoEvent, UserInfoEffect> { model, event ->
                UserInfoLogic.update(
                    model,
                    event
                )
            }, effectHandler)
                .init(UserInfoLogic::init)
                .logger(AndroidLogger.tag("UserInfo"))

        return loop
    }

    @Provides
    fun provideDelegate(
        loop: MobiusLoop.Builder<UserInfoDataModel, UserInfoEvent, UserInfoEffect>,
        userId: Int
    ): FragmentControllerDelegate<UserInfoViewModel, UserInfoDataModel, UserInfoEvent, UserInfoEffect> {
        return FragmentControllerDelegate(
            loop = loop,
            defaultStateProvider = {
                UserInfoDataModel(userId = userId)
            }, modelMapper = {
                it.mapped
            }
        )
    }

    @Module
    interface Binding {

        @Binds
        fun bindEffectHandler(effectHandler: UserInfoEffectHandler): Connectable<UserInfoEffect, UserInfoEvent>

        @Binds
        fun bindCoordinator(coordinator: RealUserInfoCoordinator): UserInfoCoordinator

    }

}