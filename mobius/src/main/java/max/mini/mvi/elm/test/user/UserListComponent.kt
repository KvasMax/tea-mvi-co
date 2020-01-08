package max.mini.mvi.elm.test.user

import android.content.Context
import com.spotify.mobius.Mobius
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.Update
import com.spotify.mobius.android.AndroidLogger
import dagger.Component
import dagger.Module
import dagger.Provides
import max.mini.mvi.elm.test.AppComponent
import max.mini.mvi.elm.test.base.FragmentControllerDelegate

@Component(
    dependencies = [AppComponent::class],
    modules = [UserListModule::class]
)
interface UserListComponent {

    @Component.Factory
    interface Factory {
        fun create(
            appComponent: AppComponent
        ): UserListComponent
    }

    fun getFragment(): UsersFragment

}

@Module
class UserListModule {

    @Provides
    fun provideLoop(
        context: Context

    ): MobiusLoop.Builder<UserListModel, UserListEvent, UserListEffect> {
        val loop =
            Mobius.loop(Update<UserListModel, UserListEvent, UserListEffect> { model, event ->
                UserListLogic.update(
                    model,
                    event
                )
            }, UserListEffectHandler(context))
                .init(UserListLogic::init)
                .logger(AndroidLogger.tag("UserList"))

        return loop
    }

    @Provides
    fun provideDelegate(
        loop: MobiusLoop.Builder<UserListModel, UserListEvent, UserListEffect>
    ): FragmentControllerDelegate<UserListModel, UserListEvent, UserListEffect> {
        val delegate =
            object :
                FragmentControllerDelegate<UserListModel, UserListEvent, UserListEffect>(loop) {
                override fun createDefaultModel(): UserListModel = UserListModel()
            }
        return delegate
    }

    @Provides
    fun provideFragment(
        delegate: FragmentControllerDelegate<UserListModel, UserListEvent, UserListEffect>
    ): UsersFragment {
        val fragment = UsersFragment(delegate)
        return fragment
    }

}