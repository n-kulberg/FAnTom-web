package store

import com.arkivanov.mvikotlin.core.store.Executor
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.reaktive.ReaktiveExecutor
import com.badoo.reaktive.coroutinesinterop.singleFromCoroutine
import com.badoo.reaktive.scheduler.ioScheduler
import com.badoo.reaktive.scheduler.mainScheduler
import com.badoo.reaktive.single.map
import com.badoo.reaktive.single.observeOn
import com.badoo.reaktive.single.subscribeOn
import model.Filter
import model.RESEARCH_INITIALIZATION_FAILED
import model.ResearchApiExceptions
import repository.ResearchRepository
import store.list.ListStore.Intent
import store.list.ListStore.State
import store.list.ListStoreAbstractFactory

internal class ListStoreFactory(
  storeFactory: StoreFactory,
  private val repository: ResearchRepository
) : ListStoreAbstractFactory(
  storeFactory = storeFactory
) {

  override fun createExecutor(): Executor<Intent, Unit, State, Result, Nothing> = ExecutorImpl()

  private inner class ExecutorImpl : ReaktiveExecutor<Intent, Unit, State, Result, Nothing>() {
    override fun executeAction(action: Unit, getState: () -> State) = load()

    override fun executeIntent(intent: Intent, getState: () -> State) {
      when (intent) {
        is Intent.HandleFilterChanged -> filterChanged(intent.filter)
        Intent.DismissError -> dispatch(Result.DismissErrorRequested)
        Intent.ReloadRequested -> load()
      }.let {}
    }

    private fun filterChanged(filter: Filter) {
      singleFromCoroutine {
        repository.getFiltered(filter)
      }
        .subscribeOn(ioScheduler)
        .map(Result::Loaded)
        .observeOn(mainScheduler)
        .subscribeScoped(
          isThreadLocal = true,
          onSuccess = ::dispatch,
          onError = ::handleError
        )
    }

    private fun load() {
      singleFromCoroutine {
        repository.getResearches()
      }
        .subscribeOn(ioScheduler)
        .map(Result::Loaded)
        .observeOn(mainScheduler)
        .subscribeScoped(isThreadLocal = true, onSuccess = ::dispatch)
    }

    private fun handleError(error: Throwable) {
      val result = when (error) {
        is ResearchApiExceptions.ResearchListFetchException -> Result.Error(error.error)
        is ResearchApiExceptions.ResearchDataFetchError -> Result.Error(error.error)
        is ResearchApiExceptions.ResearchNotFoundException -> Result.Error(error.error)
        else -> {
          println("list: other exception ${error.message}")
          Result.Error(RESEARCH_INITIALIZATION_FAILED)
        }
      }
      dispatch(result)
    }
  }
}
