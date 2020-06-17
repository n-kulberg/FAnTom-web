package repository

import model.MarkDomain

interface MarksLocal {
  suspend fun get(markId: Int): MarkDomain?
  suspend fun getAll(): List<MarkDomain>
  suspend fun save(mark: MarkDomain)
  suspend fun saveList(marks: List<MarkDomain>)
  suspend fun delete(markId: Int)
}
