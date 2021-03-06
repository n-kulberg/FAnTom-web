import model.*
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toUser(): UserModel = UserModel(
  id = this[UserVos.id],
  name = this[UserVos.name],
  password = this[UserVos.password],
  role = this[UserVos.role]
)

fun ResultRow.toResearch(): ResearchModel = ResearchModel(
  id = this[ResearchVos.id],
  accessionNumber = this[ResearchVos.accessionNumber],
  studyInstanceUID = this[ResearchVos.studyInstanceUID],
  studyID = this[ResearchVos.studyID],
  protocol = this[ResearchVos.protocol],
  accessionNumberBase = this[ResearchVos.accessionNumber_base],
  accessionNumberLastDigit = this[ResearchVos.accessionNumber_lastDigit],
  jsonName = this[ResearchVos.json_name],
  doctor1 = this[ResearchVos.doctor1],
  doctor2 = this[ResearchVos.doctor2],
  posInBlock = this[ResearchVos.pos_in_block],
  modality = this[ResearchVos.modality],
  category = this[ResearchVos.category]
)

fun ResultRow.toUserResearch(): UserResearchModel = UserResearchModel(
  userId = this[UserResearchVos.userId],
  researchId = this[UserResearchVos.researchId],
  seen = this[UserResearchVos.seen] == 1,
  done = this[UserResearchVos.done] == 1
)

fun ResultRow.toCovidMark(): CovidMarkModel = CovidMarkModel(
  userId = this[CovidMarksVos.userId],
  researchId = this[CovidMarksVos.researchId],
  rightUpperLobeValue = this[CovidMarksVos.rightUpperLobeValue],
  middleLobeValue = this[CovidMarksVos.middleLobeValue],
  rightLowerLobeValue = this[CovidMarksVos.rightLowerLobeValue],
  leftUpperLobeValue = this[CovidMarksVos.leftUpperLobeValue],
  leftLowerLobeValue = this[CovidMarksVos.leftLowerLobeValue]
)

fun ResultRow.toMultiPlanarMark(): MarkEntity = MarkEntity(
  id = this[MultiPlanarMarksVos.id],
  markData = MarkData(
    x = this[MultiPlanarMarksVos.x],
    y = this[MultiPlanarMarksVos.y],
    z = this[MultiPlanarMarksVos.z],
    radiusHorizontal = this[MultiPlanarMarksVos.radius],
    radiusVertical = this[MultiPlanarMarksVos.radius],
    sizeVertical = this[MultiPlanarMarksVos.size],
    sizeHorizontal = this[MultiPlanarMarksVos.size],
    cutType = this[MultiPlanarMarksVos.cutType],
    shapeType = SHAPE_TYPE_CIRCLE
  ),
  type = this[MultiPlanarMarksVos.type],
  comment = this[MultiPlanarMarksVos.comment],
)

fun ResultRow.toPlanarMark(): MarkEntity = MarkEntity(
  id = this[PlanarMarksVos.id],
  markData = MarkData(
    x = this[PlanarMarksVos.x],
    y = this[PlanarMarksVos.y],
    z = -1.0,
    radiusHorizontal = this[PlanarMarksVos.radiusHorizontal],
    radiusVertical = this[PlanarMarksVos.radiusVertical],
    sizeVertical = this[PlanarMarksVos.sizeVertical],
    sizeHorizontal = this[PlanarMarksVos.sizeHorizontal],
    cutType = this[PlanarMarksVos.cutType],
    shapeType = this[PlanarMarksVos.shapeType]
  ),
  type = this[PlanarMarksVos.type],
  comment = this[PlanarMarksVos.comment],
)

fun ResultRow.toExportedMarkModel(): ExportedMarkModel = ExportedMarkModel(
  id = this[ExpertMarksVos.id],
  diameterMm = this[ExpertMarksVos.diameterMm],
  type = this[ExpertMarksVos.type],
  version = this[ExpertMarksVos.version],
  x = this[ExpertMarksVos.x],
  y = this[ExpertMarksVos.y],
  z = this[ExpertMarksVos.z],
  zType = this[ExpertMarksVos.zType],
  expertDecision = this[ExpertMarksVos.expertDecision],
  expertDecisionId = this[ExpertMarksVos.expertDecisionId],
  expertDecisionComment = this[ExpertMarksVos.expertDecisionComment],
  expertDecisionMachineLearning = this[ExpertMarksVos.expertDecisionMachineLearning] == 1,
  expertDecisionProperSize = this[ExpertMarksVos.expertDecisionProperSize] == 1,
  expertDecisionType = this[ExpertMarksVos.expertDecisionType],
)