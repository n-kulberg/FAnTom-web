package store

import com.arkivanov.mvikotlin.core.store.Executor
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.reaktive.ReaktiveExecutor
import model.Mip
import repository.MipRepository
import store.tools.MipStore.*
import store.tools.MipStoreAbstractFactory

internal class MipStoreFactory(
  storeFactory: StoreFactory,
  mipRepository: MipRepository
) : MipStoreAbstractFactory(
  storeFactory = storeFactory,
  mipRepository = mipRepository
) {

  override fun createExecutor(): Executor<Intent, Nothing, State, Result, Label> =
    object : ReaktiveExecutor<Intent, Nothing, State, Result, Label>() {

      override fun executeIntent(intent: Intent, getState: () -> State) {
        when (intent) {
          is Intent.HandleMipClick -> changeMip(intent.mip)
          is Intent.HandleMipValueChanged -> changeMipValue(intent.value)
        }.let {}
      }

      private fun changeMip(mip: Mip) {
        dispatch(Result.MipMethodChanged(mip))
        publish(Label.MipMethodChanged(mip))
      }

      private fun changeMipValue(value: Int) {
        dispatch(Result.MipValueChanged(value))
        publish(Label.MipValueChanged(value))
      }
    }
}
