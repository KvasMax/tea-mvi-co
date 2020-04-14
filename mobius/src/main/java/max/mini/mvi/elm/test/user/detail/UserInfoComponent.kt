package max.mini.mvi.elm.test.user.detail

import com.spotify.mobius.Connectable
import com.spotify.mobius.Mobius
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.Update
import com.spotify.mobius.android.AndroidLogger
import dagger.*
import max.mini.mvi.elm.test.RootDependencies
import max.mini.mvi.elm.test.base.FragmentControllerDelegate

@Component(
    dependencies = [RootDependencies::class],
    modules = [UserInfoModule::class]
)
interface UserInfoComponent {

    @Component.Factory
    interface Factory {
        fun create(
            rootDependencies: RootDependencies,
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
    ): MobiusLoop.Builder<UserInfoModel, UserInfoEvent, UserInfoEffect> {
        val loop =
            Mobius.loop(Update<UserInfoModel, UserInfoEvent, UserInfoEffect> { model, event ->
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
        loop: MobiusLoop.Builder<UserInfoModel, UserInfoEvent, UserInfoEffect>,
        userId: Int
    ): FragmentControllerDelegate<UserInfoModel, UserInfoEvent, UserInfoEffect> {
        return FragmentControllerDelegate(
            loop
        ) {
            UserInfoModel(id = userId)
        }
    }

    @Module
    interface Binding {

        @Binds
        fun bindEffectHandler(effectHandler: UserInfoEffectHandler): Connectable<UserInfoEffect, UserInfoEvent>

    }

}