package max.mini.mvi.elm.test.user.list

import com.spotify.mobius.Connectable
import com.spotify.mobius.Mobius
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.Update
import com.spotify.mobius.android.AndroidLogger
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import max.mini.mvi.elm.test.RootDependencies
import max.mini.mvi.elm.test.base.FragmentControllerDelegate

@Component(
    dependencies = [RootDependencies::class],
    modules = [UserListModule::class]
)
interface UserListComponent {

    @Component.Factory
    interface Factory {
        fun create(
            rootDependencies: RootDependencies
        ): UserListComponent
    }

    fun inject(fragment: UsersFragment)

}

@Module(includes = [UserListModule.Bindings::class])
class UserListModule {

    @Provides
    fun provideLoop(
        effectHandler: Connectable<UserListEffect, UserListEvent>
    ): MobiusLoop.Builder<UserListModel, UserListEvent, UserListEffect> {
        val loop =
            Mobius.loop(Update<UserListModel, UserListEvent, UserListEffect> { model, event ->
                UserListLogic.update(
                    model,
                    event
                )
            }, effectHandler)
                .init(UserListLogic::init)
                .logger(AndroidLogger.tag("UserList"))

        return loop
    }

    @Provides
    fun provideDelegate(
        loop: MobiusLoop.Builder<UserListModel, UserListEvent, UserListEffect>
    ): FragmentControllerDelegate<UserListModel, UserListEvent, UserListEffect> {
        return FragmentControllerDelegate(
            loop
        ) {
            UserListModel()
        }
    }

    @Module
    interface Bindings {

        @Binds
        fun bindEffectHandler(effectHandler: UserListEffectHandler): Connectable<UserListEffect, UserListEvent>

        @Binds
        fun bindCoordinator(coordinator: RealUserListCoordinator): UserListCoordinator

    }

}