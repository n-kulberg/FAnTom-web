package repository

import model.Research

interface ResearchLocal {
  suspend fun get(accessionName: String): Research?
  suspend fun getAll(): List<Research>
  suspend fun save(research: Research)
  suspend fun saveList(list: List<Research>)
  suspend fun delete(name: String)
}