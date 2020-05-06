package dao

import ResearchVos
import model.ResearchModel
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import toResearch
import util.database

interface ResearchDaoFacade {
  suspend fun getResearchById(researchId: Int): ResearchModel?
  suspend fun getResearchByAccessionName(accessionNumber: String): ResearchModel?
  suspend fun createResearch(researchModel: ResearchModel)
  suspend fun deleteResearch(researchId: Int)
  suspend fun updateResearch(researchModel: ResearchModel)
}

class ResearchDao() : ResearchDaoFacade {
  override suspend fun getResearchById(researchId: Int): ResearchModel? {
    return database {
      ResearchVos.select { ResearchVos.id eq researchId }
    }
      .firstOrNull()
      ?.toResearch()
  }

  override suspend fun getResearchByAccessionName(accessionNumber: String): ResearchModel? {
    return database {
      ResearchVos.select { ResearchVos.accessionNumber eq accessionNumber }
    }
      .firstOrNull()
      ?.toResearch()
  }

  override suspend fun createResearch(researchModel: ResearchModel) {
    return database {
      ResearchVos.insert {
        it[accessionNumber] = researchModel.accessionNumber
        it[studyInstanceUID] = researchModel.studyInstanceUID
        it[studyID] = researchModel.studyID
        it[protocol] = researchModel.protocol
        it[accessionNumber_base] = researchModel.accessionNumberBase
        it[accessionNumber_lastDigit] = researchModel.accessionNumberLastDigit
        it[json_name] = researchModel.jsonName
        it[doctor1] = researchModel.doctor1
        it[doctor2] = researchModel.doctor2
        it[pos_in_block] = researchModel.posInBlock
      }
    }
  }

  override suspend fun deleteResearch(researchId: Int) {
    database {
      ResearchVos.deleteWhere { ResearchVos.id eq researchId }
    }
  }

  override suspend fun updateResearch(researchModel: ResearchModel) {
    return database {
      ResearchVos.update(where = {ResearchVos.id eq researchModel.id}){
        it[accessionNumber] = researchModel.accessionNumber
        it[studyInstanceUID] = researchModel.studyInstanceUID
        it[studyID] = researchModel.studyID
        it[protocol] = researchModel.protocol
        it[accessionNumber_base] = researchModel.accessionNumberBase
        it[accessionNumber_lastDigit] = researchModel.accessionNumberLastDigit
        it[json_name] = researchModel.jsonName
        it[doctor1] = researchModel.doctor1
        it[doctor2] = researchModel.doctor2
        it[pos_in_block] = researchModel.posInBlock
      }
    }
  }


}