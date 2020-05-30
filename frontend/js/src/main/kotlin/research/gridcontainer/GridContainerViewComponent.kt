package research.gridcontainer

import com.arkivanov.mvikotlin.core.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.rx.Disposable
import com.arkivanov.mvikotlin.rx.Observer
import controller.CutController
import controller.GridContainerController
import controller.GridContainerControllerImpl
import destroy
import kotlinx.css.*
import model.Cut
import model.Grid
import model.ResearchSlicesSizesData
import react.*
import repository.ResearchRepository
import research.cut.CutContainer
import research.cut.cutContainer
import research.gridcontainer.GridContainerViewComponent.GridContainerStyles.columnOfRowsStyle
import research.gridcontainer.GridContainerViewComponent.GridContainerStyles.rowOfColumnsStyle
import resume
import styled.StyleSheet
import styled.css
import styled.styledDiv
import view.GridContainerView
import view.initialGridContainerModel

class GridContainerViewComponent(prps: GridContainerProps) :
  RComponent<GridContainerProps, GridContainerState>(prps) {

  private val cutsViewDelegate = GridContainerViewProxy(::updateState)
  private val lifecycleRegistry = LifecycleRegistry()
  private lateinit var controller: GridContainerController

  init {
    state = GridContainerState(initialGridContainerModel())
  }

  override fun componentDidMount() {
    lifecycleRegistry.resume()
    controller = createController()
    controller.onViewCreated(cutsViewDelegate, lifecycleRegistry)
  }

  private fun createController(): GridContainerController {
    val dependencies = props.dependencies
    val researchControllerDependencies =
      object : GridContainerController.Dependencies, Dependencies by dependencies {
        override val lifecycle: Lifecycle = lifecycleRegistry
      }
    return GridContainerControllerImpl(researchControllerDependencies)
  }

  override fun RBuilder.render() {
    val cutsModel = state.cutsModel
    if (cutsModel.items.isNotEmpty()) {
      styledDiv {
        css(columnOfRowsStyle)
        when (cutsModel.grid) {
          is Grid.Single -> singleCutContainer(cutsModel.items.first())
          is Grid.TwoVertical -> twoVerticalCutsContainer(cutsModel.items)
          is Grid.TwoHorizontal -> twoHorizontalCutsContainer(cutsModel.items)
          is Grid.Four -> fourCutsContainer(cutsModel.items)
        }
      }
    }
  }

  private fun RBuilder.singleCutContainer(item: Cut) {
    styledDiv {
      css(rowOfColumnsStyle)
      cutContainer(dependencies = dependencies(item))
    }
  }

  private fun RBuilder.twoHorizontalCutsContainer(items: List<Cut>) {
    styledDiv {
      css(rowOfColumnsStyle)
      cutContainer(dependencies = dependencies(items.first()))
      cutContainer(dependencies = dependencies(items.last()))
    }
  }

  private fun RBuilder.twoVerticalCutsContainer(items: List<Cut>) {
    styledDiv {
      css(rowOfColumnsStyle)
      cutContainer(dependencies = dependencies(items.first()))
    }

    styledDiv {
      css(rowOfColumnsStyle)
      cutContainer(dependencies = dependencies(items.last()))
    }
  }

  private fun RBuilder.fourCutsContainer(items: List<Cut>) {
    val leftTop = items[0]
    val rightTop = items[1]
    val leftBottom = items[2]
    val rightBottom = items[3]

    //это колонка строк срезов(верх низ)
    styledDiv {
      css(columnOfRowsStyle)

      //одна из строк (лево/право)
      styledDiv {
        css(rowOfColumnsStyle)

        //контейнер для среза
        cutContainer(
          dependencies = dependencies(leftTop)
        )
        //TODO() пока известно что сверху справа пустой, поэтому заглушка
        styledDiv {
          css {
            display = Display.flex
            flexDirection = FlexDirection.row
            position = Position.relative
            width = 50.pct
          }
        }

      }

      styledDiv {
        css(rowOfColumnsStyle)

        cutContainer(dependencies = dependencies(leftBottom))
        cutContainer(dependencies = dependencies(rightBottom))
      }
    }
  }

  private fun dependencies(cut: Cut): CutContainer.Dependencies =
    object : CutContainer.Dependencies, Dependencies by props.dependencies {
      override val cut: Cut = cut
      override val cutOutput: (CutController.Output) -> Unit = ::cutContainerOutput
    }

  private fun cutContainerOutput(output: CutController.Output) {
//    when (output) {
//      is CutController.Output.SliceNumberChanged -> TODO()
//      is CutController.Output.BrightnessChanged -> TODO()
//    }
  }

  private fun updateState(model: GridContainerView.Model) = setState { cutsModel = model }

  override fun componentWillUnmount() {
    lifecycleRegistry.destroy()
  }

  interface Dependencies {
    val storeFactory: StoreFactory
    val data: ResearchSlicesSizesData
    val gridContainerInputs: (Observer<GridContainerController.Input>) -> Disposable
    val cutsInput: (Observer<CutController.Input>) -> Disposable
    val researchRepository: ResearchRepository
    val researchId: Int
  }

  object GridContainerStyles : StyleSheet("CutsStyles", isStatic = true) {

    val columnOfRowsStyle by css {
      flex(1.0)
      display = Display.flex
      flexDirection = FlexDirection.column
    }

    val rowOfColumnsStyle by css {
      flex(1.0)
      display = Display.flex
      flexDirection = FlexDirection.row
    }
  }
}

class GridContainerState(
  var cutsModel: GridContainerView.Model
) : RState

interface GridContainerProps : RProps {
  var dependencies: GridContainerViewComponent.Dependencies
}

fun RBuilder.cuts(dependencies: GridContainerViewComponent.Dependencies) = child(
  GridContainerViewComponent::class
) {
  attrs.dependencies = dependencies
}
