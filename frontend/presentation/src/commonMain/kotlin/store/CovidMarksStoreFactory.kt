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
import model.*
import repository.CovidMarksRepository
import store.covid.CovidMarksStore.*
import store.covid.CovidMarksStoreAbstractFactory

internal class CovidMarksStoreFactory(
  storeFactory: StoreFactory,
  val repository: CovidMarksRepository,
  val research: Research,
  val data: ResearchSlicesSizesDataNew
) : CovidMarksStoreAbstractFactory(
  storeFactory = storeFactory
) {

  override fun createExecutor(): Executor<Intent, Unit, State, Result, Label> = ExecutorImpl()

  private inner class ExecutorImpl : ReaktiveExecutor<Intent, Unit, State, Result, Label>() {

    override fun executeAction(action: Unit, getState: () -> State) {
      singleFromCoroutine {
        repository.getMark(research.id)
      }
        .map(CovidMarkEntity::toLungLobeModelMap)
        .subscribeOn(ioScheduler)
        .map(Result::Loaded)
        .observeOn(mainScheduler)
        .subscribeScoped(
          isThreadLocal = true,
          onSuccess = ::dispatch,
          onError = ::handleError
        )
    }

    override fun executeIntent(intent: Intent, getState: () -> State) {
      when (intent) {
        is Intent.ChangeVariant ->
          handleChangeVariant(
            lungLobeModel = intent.lungLobeModel,
            variant = intent.variant,
            getState = getState
          )
        Intent.DismissError -> dispatch(Result.DismissErrorRequested)
        Intent.ReloadRequested -> null
        Intent.HandleCloseResearch -> handleCloseResearch(getState)
      }.let {}
    }

    private fun handleCloseResearch(getState: () -> State) {
      val mark = getState().covidLungLobes
      val modelWithNoValue = mark.values.firstOrNull { it.value == null }
      if (modelWithNoValue != null) {
        dispatch(Result.Error(COVID_MARK_TYPE_NOT_SET))
      } else {
        publish(Label.CloseResearch)
      }
    }

    private fun handleChangeVariant(
      lungLobeModel: LungLobeModel,
      variant: Int,
      getState: () -> State
    ) {
      singleFromCoroutine {
        val newModel = lungLobeModel.changeValue(variant)
        val newMap = getState().covidLungLobes.toMutableMap()
        newMap[lungLobeModel.id] = newModel
        repository.saveMark(newMap.toCovidMarkEntity(), research.id)
        return@singleFromCoroutine newMap
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

    private fun handleError(error: Throwable) {
      val result = when (error) {
        is ResearchApiExceptions -> Result.Error(error.error)
        else -> {
          println("cut: other exception ${error.message}")
          Result.Error(BASE_ERROR)
        }
      }
      dispatch(result)
    }
  }

}