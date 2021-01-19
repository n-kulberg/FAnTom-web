package research.covid

import com.arkivanov.mvikotlin.core.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.core.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.subscribe
import com.ccfraser.muirwik.components.list.*
import controller.CovidMarksController
import controller.CovidMarksControllerImpl
import kotlinx.css.*
import model.ResearchSlicesSizesDataNew
import react.*
import repository.MarksRepository
import resume
import styled.css
import styled.styledDiv
import view.CovidMarksView
import view.CovidMarksView.Model
import view.initialCovidMarksModel

class CovidMarksComponent(prps: CovidMarksProps) :
  RComponent<CovidMarksProps, CovidMarksState>(prps) {

  private var expandedItem: String? = null

  private val marksViewDelegate = CovidMarksViewProxy(::updateState)
  private val lifecycleRegistry = LifecycleRegistry()
  private lateinit var controller: CovidMarksController

  init {
    state = CovidMarksState(initialCovidMarksModel())
  }

  override fun componentDidMount() {
    lifecycleRegistry.resume()
    controller = createController()
    controller.onViewCreated(
      marksViewDelegate,
      lifecycleRegistry
    )
    val dependencies = props.dependencies
    val disposable = dependencies.marksInput.subscribe { controller.input(it) }
    lifecycleRegistry.doOnDestroy(disposable::dispose)
  }

  private fun createController(): CovidMarksController {
    val dependencies = props.dependencies
    val marksControllerDependencies =
      object : CovidMarksController.Dependencies, Dependencies by dependencies {
        override val lifecycle: Lifecycle = lifecycleRegistry
        override val researchId: Int = dependencies.data.researchId
      }
    return CovidMarksControllerImpl(marksControllerDependencies)
  }

  override fun RBuilder.render() {

    styledDiv {
      css {
        display = Display.flex
        flexDirection = FlexDirection.column
      }
      mList {
        state.model.lungLobeModels.forEach { lungLobeModel ->
          val panelName = "panel${lungLobeModel.shortName}"

          covidMarkView(
            model = lungLobeModel,
            handlePanelClick = { expandClick(panelName) },
            handleVariantClick = {
              marksViewDelegate.dispatch(CovidMarksView.Event.VariantChosen(lungLobeModel, it))
            },
            expand = expandedItem == panelName,
            fullWidth = props.dependencies.open
          )
        }
      }
    }
  }

  private fun expandClick(panelName: String) =
    setState { expandedItem = if (expandedItem == panelName) null else panelName }


  private fun updateState(marksModel: Model) = setState { model = marksModel }

  interface Dependencies {
    val storeFactory: StoreFactory
    val marksRepository: MarksRepository
    val marksOutput: (CovidMarksController.Output) -> Unit
    val data: ResearchSlicesSizesDataNew
    val marksInput: Observable<CovidMarksController.Input>
    val open: Boolean
  }
}

class CovidMarksState(var model: Model) : RState

interface CovidMarksProps : RProps {
  var dependencies: CovidMarksComponent.Dependencies
}

fun RBuilder.covidMarks(dependencies: CovidMarksComponent.Dependencies) = child(CovidMarksComponent::class) {
  attrs.dependencies = dependencies
}