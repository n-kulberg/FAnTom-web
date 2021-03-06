package store.shapes

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.utils.JvmSerializable
import model.*
import store.shapes.ShapesStore.*

interface ShapesStore : Store<Intent, State, Label> {

  sealed class Intent : JvmSerializable {
    data class HandleSliceNumberChange(val sliceNumber: Int) : Intent()
    data class HandleExternalSliceNumberChanged(val sliceNumber: Int, val cut: Cut) : Intent()
    data class HandleMousePosition(val dicomX: Double, val dicomY: Double) : Intent()
    data class HandleMarks(val list: List<MarkModel>) : Intent()
    data class HandleMoveInClick(val deltaX: Double, val deltaY: Double) : Intent()
    data class HandleStartClick(val startDicomX: Double, val startDicomY: Double) : Intent()
    data class HandleChangeCutType(val value: CutType) : Intent()

    object HandleStopMoving : Intent()
  }

  data class State(
    val horizontalCoefficient: Double,
    val verticalCoefficient: Double,
    val sliceNumber: Int,
    val position: PointPosition?,
    val shapes: List<Shape>,
    val rects: List<Rect>,
    val hounsfield: Int?,
    val marks: List<MarkModel>,
    val moveRect: Rect?
  ) : JvmSerializable

  sealed class Label {
    data class SelectMark(val mark: MarkModel) : Label()
    data class UnselectMark(val mark: MarkModel) : Label()
    data class UpdateMarkCoordinates(val mark: MarkModel) : Label()
    data class UpdateMarkWithSave(val mark: MarkModel) : Label()
    data class ChangeCutType(val cutType: CutType) : Label()
  }
}
