package repository

import model.MarkData
import model.MarkEntity

interface MarksRepository {

  val token: suspend () -> String

  suspend fun getMarks(researchId: Int): List<MarkEntity>
  suspend fun saveMark(markToSave: MarkData, researchId: Int): MarkEntity
  suspend fun updateMark(mark: MarkEntity, researchId: Int)
  suspend fun deleteMark(id: Int, researchId: Int)
  suspend fun clean()
}
