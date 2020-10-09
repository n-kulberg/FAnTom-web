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
import model.Cut
import model.GET_SLICE_FAILED
import model.ResearchApiExceptions
import model.getSliceNumberByMark
import repository.BrightnessRepository
import repository.MipRepository
import repository.ResearchRepository
import store.cut.CutStore.*
import store.cut.CutStoreAbstractFactory

internal class CutStoreFactory(
  storeFactory: StoreFactory,
  val cut: Cut,
  val repository: ResearchRepository,
  val researchId: Int,
  brightnessRepository: BrightnessRepository,
  mipRepository: MipRepository
) : CutStoreAbstractFactory(
  storeFactory = storeFactory,
  cut = cut,
  researchId = researchId,
  brightnessRepository = brightnessRepository,
  mipRepository = mipRepository
) {
  override fun createExecutor(): Executor<Intent, Unit, State, Result, Label> = ExecutorImpl()

  private inner class ExecutorImpl : ReaktiveExecutor<Intent, Unit, State, Result, Label>() {

    override fun executeAction(action: Unit, getState: () -> State) =
      load(getState = getState, loadingType = Result.MainLoading)

    override fun executeIntent(intent: Intent, getState: () -> State) {
      when (intent) {
        is Intent.HandleSliceNumberChange -> {
          val sliceNumber = intent.sliceNumber
          if (inBounds(sliceNumber)) {
            changeSliceNumber(sliceNumber, getState)
          } else null
        }
        is Intent.HandleBlackChanged -> {
          dispatch(Result.BlackChanged(blackValue = intent.blackValue))
          load(getState, Result.SecondaryLoading)
        }
        is Intent.HandleWhiteChanged -> {
          dispatch(Result.WhiteChanged(whiteValue = intent.whiteValue))
          load(getState, Result.SecondaryLoading)
        }
        is Intent.HandleGammaChanged -> {
          dispatch(Result.GammaChanged(gammaValue = intent.gammaValue))
          load(getState, Result.SecondaryLoading)
        }
        is Intent.HandleMipChanged -> {
          dispatch(Result.MipChanged(mip = intent.mip))
          load(getState, Result.SecondaryLoading)
        }
        is Intent.HandleMipValueChanged -> {
          dispatch(Result.MipValueChanged(mipValue = intent.mipValue))
          load(getState, Result.SecondaryLoading)
        }
        is Intent.HandlePresetChanged -> {
          dispatch(Result.PresetChanged(black = intent.presets.black, white = intent.presets.white))
          load(getState, Result.SecondaryLoading)
        }
        is Intent.HandleCircleDrawn -> {
          publish(Label.CircleDrawn(intent.circle, getState().sliceNumber, cut))
        }
        is Intent.HandleRectangleDrawn -> {
          publish(Label.RectangleDrawn(intent.rectangle, getState().sliceNumber, cut))
        }
        is Intent.HandleExternalSliceNumberChanged -> {
          publish(Label.ExternalSliceNumberChanged(intent.externalCut, intent.sliceNumber))
        }
        is Intent.HandleMarks -> {
          publish(Label.Marks(intent.list))
        }
        is Intent.HandleMarkSelected -> {
          publish(Label.SelectMark(intent.mark))
        }
        is Intent.HandleMarkCenter -> {
          publish(Label.CenterMark(intent.mark))
        }
        is Intent.HandleMarkUnselect -> {
          publish(Label.UnselectMark(intent.mark))
        }
        is Intent.ChangeSliceNumberByMarkCenter -> {
          cut.getSliceNumberByMark(intent.mark)?.let { changeSliceNumber(it, getState) }
        }
        is Intent.ChangeContrastBrightness -> {
          handleContrastBrightness(intent.deltaX, intent.deltaY, getState)
        }
        Intent.ContrasBrightnessChanged -> {
          val state = getState()
          publish(Label.ContrastBrightnessChanged(state.black, state.white))
        }
        is Intent.ChangeSliceNumberByDraw -> {
          val sliceNumber = getState().sliceNumber + intent.deltaDicomY
          if (inBounds(sliceNumber)) {
            changeSliceNumber(sliceNumber, getState)
          } else null
        }
        is Intent.HandleMarkUpdateWithoutSave -> {
          publish(Label.UpdateMarkWithoutSave(intent.mark))
        }
        is Intent.HandleStopMoving -> {
          publish(Label.StopMoving)
        }
        is Intent.HandleMarkUpdateWithSave -> {
          publish(Label.UpdateMarkWithSave(intent.mark))
        }
        is Intent.HandleStartClick -> {
          publish(Label.StartClick(intent.startDicomX, intent.startDicomY))
        }
        Intent.OpenFullCut -> publish(Label.OpenFullCut(cut))

        is Intent.HandleChangeCutType -> {
          publish(Label.ChangeCutType(intent.cutType, cut))
        }
        Intent.DismissErrorRequested -> dispatch(Result.DismissError)
      }.let {}
    }

    private fun inBounds(sliceNumber: Int) = sliceNumber <= cut.data.n_images && sliceNumber > 0

    private fun changeSliceNumber(sliceNumber: Int, getState: () -> State) {
      val resultSliceNumber = when {
        sliceNumber < 1 -> 1
        sliceNumber > cut.data.n_images -> cut.data.n_images
        else -> sliceNumber
      }
      if (sliceNumber != getState().sliceNumber) {
        dispatch(Result.SliceNumberChanged(sliceNumber = resultSliceNumber))
        load(getState, Result.SecondaryLoading)
        publish(Label.SliceNumberChanged(sliceNumber = sliceNumber, cut = cut))
      }
    }

    private fun handleContrastBrightness(
      deltaX: Double,
      deltaY: Double,
      state: () -> State
    ) {
      val oldBlack = state().black
      val oldWhite = state().white
      val black = oldBlack - deltaY - deltaX
      val white = oldWhite - deltaY + deltaX
      dispatch(Result.BlackChanged(blackValue = black.toInt()))
      dispatch(Result.WhiteChanged(whiteValue = white.toInt()))
      load(state, Result.SecondaryLoading)
    }

    private fun load(getState: () -> State, loadingType: Result) {
      val state = getState()
      if (state.mainLoading.not() && state.secondaryLoading.not()) {
        dispatch(loadingType)
        singleFromCoroutine {
          repository.getSlice(
            researchId = researchId,
            type = cut.type.intType,
            black = state.black,
            white = state.white,
            gamma = state.gamma,
            mipMethod = state.mipMethod.intValue,
            sliceNumber = state.sliceNumber,
            aproxSize = state.mipValue
          )
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
    }

    private fun handleError(error: Throwable) {
      val result = when (error) {
        is ResearchApiExceptions -> Result.Error(error.error)
        else -> {
          println("cut: other exception ${error.message}")
          Result.Error(GET_SLICE_FAILED)
        }
      }
      dispatch(result)
    }
  }

}


