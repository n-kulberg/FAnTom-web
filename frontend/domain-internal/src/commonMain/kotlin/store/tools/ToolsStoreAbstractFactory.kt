package store.tools

import com.arkivanov.mvikotlin.core.store.Executor
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.core.utils.JvmSerializable
import com.badoo.reaktive.utils.ensureNeverFrozen
import model.ResearchSlicesSizesDataNew
import model.ResearchType
import model.Tool
import store.tools.ToolsStore.Intent
import store.tools.ToolsStore.State

abstract class ToolsStoreAbstractFactory(
  private val storeFactory: StoreFactory,
  private val data: ResearchSlicesSizesDataNew
) {

  fun create(): ToolsStore =
    object : ToolsStore, Store<Intent, State, Nothing> by storeFactory.create(
      name = "ToolsStore",
      initialState = getInitialState(),
      executorFactory = ::createExecutor,
      reducer = ReducerImpl
    ) {
      init {
        ensureNeverFrozen()
      }
    }

  protected abstract fun createExecutor(): Executor<Intent, Nothing, State, Result, Nothing>

  protected sealed class Result : JvmSerializable {
    data class ToolChanged(val tool: Tool) : Result()
  }

  private object ReducerImpl : Reducer<State, Result> {
    override fun State.reduce(result: Result): State =
      when (result) {
        is Result.ToolChanged -> copy(current = result.tool)
      }
  }

  private fun getInitialState(): State =
    when (data.type) {
      ResearchType.CT -> State(list = listOf(Tool.MIP, Tool.Brightness, Tool.Preset))
      ResearchType.DX,
      ResearchType.MG -> State(list = listOf(Tool.Brightness, Tool.Preset))
    }
}
