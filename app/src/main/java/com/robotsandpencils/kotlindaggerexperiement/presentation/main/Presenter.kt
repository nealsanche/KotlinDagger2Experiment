package com.robotsandpencils.kotlindaggerexperiement.presentation.main

import android.util.Log
import com.robotsandpencils.kotlindaggerexperiement.app.db.User
import com.robotsandpencils.kotlindaggerexperiement.app.repositories.MainRepository
import com.robotsandpencils.kotlindaggerexperiement.presentation.base.BasePresenter
import com.robotsandpencils.kotlindaggerexperiement.presentation.base.UiThreadQueue
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.run

/**
 * A super simple presenter
 */

class Presenter(private val mainRepository: MainRepository, uiThreadQueue: UiThreadQueue) :
        BasePresenter<Contract.View>(uiThreadQueue), Contract.Presenter {

    override fun attach(view: Contract.View) {
        super.attach(view)

        view.setTitle("Presenter Attached")

        val viewModel = view.getViewModel()
        viewModel.users = mainRepository.getUserDao().getAll()
    }

    override fun addUser(id: String, firstName: String, lastName: String) {
        // Use Coroutines to rn this in the background and then do something on the UI
        // thread if successful.
        val deferred = async(CommonPool) {
            mainRepository.getUserDao().insertAll(User(id.toInt(), firstName, lastName))
            run(UI) {
                uiThreadQueue.run {
                    view?.setTitle("Record Added")
                    view?.clearFields()
                }
            }
        }

        // This will be called back when done, and if there is an error, throwable will be set
        deferred.invokeOnCompletion { throwable ->
            if (throwable != null) {
                Log.e("DB", "Unable to save: ${Thread.currentThread().name}", throwable)

                async(CommonPool) {
                    run(UI) {
                        uiThreadQueue.run {
                            view?.showError(throwable.message)
                        }
                    }
                }
            }
        }
    }

    override fun removeUser(user: User) {
        async(CommonPool) {
            mainRepository.getUserDao().delete(user)

            run(UI) {
                uiThreadQueue.run {
                    view?.setTitle("Record Deleted")
                }
            }
        }
    }
}
