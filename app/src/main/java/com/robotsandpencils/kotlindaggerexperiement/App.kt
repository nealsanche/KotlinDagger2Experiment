package com.robotsandpencils.kotlindaggerexperiement

import android.app.Activity
import android.app.Application
import android.support.annotation.VisibleForTesting
import android.support.v4.app.Fragment
import com.robotsandpencils.kotlindaggerexperiement.app.modules.AppComponent
import com.robotsandpencils.kotlindaggerexperiement.app.modules.AppModule
import com.robotsandpencils.kotlindaggerexperiement.app.modules.DaggerAppComponent
import com.robotsandpencils.kotlindaggerexperiement.app.modules.UserComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import net.danlew.android.joda.JodaTimeAndroid
import timber.log.Timber
import javax.inject.Inject

/**
 * App
 */

open class App : Application(), HasActivityInjector, HasSupportFragmentInjector {
    @Inject
    internal lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    internal lateinit var dispatchingFragmentInjector: DispatchingAndroidInjector<Fragment>

    @set:VisibleForTesting
    lateinit var component: AppComponent

    @set:VisibleForTesting
    lateinit var userComponent: UserComponent

    override fun onCreate() {
        component = createComponent()
        resetUserComponent()

        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        JodaTimeAndroid.init(this)
    }

    fun resetUserComponent() {
        userComponent = createUserComponent()
        userComponent.inject(this)
    }

    override fun activityInjector(): AndroidInjector<Activity> {
        return dispatchingActivityInjector
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return dispatchingFragmentInjector
    }

    open fun createComponent(): AppComponent {
        return DaggerAppComponent
                .builder()
                .appModule(AppModule(this))
                .build()
    }

    open fun createUserComponent(): UserComponent {
        return component.userComponent()
                .build()
    }
}
