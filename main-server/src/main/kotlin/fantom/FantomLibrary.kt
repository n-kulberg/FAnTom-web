package fantom

import util.debugLog
import com.badoo.reaktive.observable.debounce
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.scheduler.computationScheduler
import com.badoo.reaktive.subject.publish.PublishSubject
import model.*
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.*
import io.ktor.http.takeFrom
import kotlinx.serialization.json.Json

interface FantomLibrary {

  val userId: Int
  val endPoint: String
  val container: DockerContainer

  val onClose: (DockerContainer)  -> Unit

  suspend fun getAccessionNames(): List<String>
  suspend fun initResearch(researchId: String): ResearchInitResponse
  suspend fun getSlice(sliceRequest: SliceRequest, researchId: String): String
  suspend fun getHounsfield(axialCoord: Int, frontalCoord: Int, sagittalCoord: Int): Double
}

class FantomLibraryImpl(
  override val userId: Int,
  override val endPoint: String,
  override val container: DockerContainer,
  override val onClose: (DockerContainer) -> Unit
) : FantomLibrary {

  private val sessionDebounceSubject = PublishSubject<DockerContainer>()

  init {
    sessionDebounceSubject
      .debounce(30000, computationScheduler)
      .subscribe { container ->
        debugLog("going to close container")
        onClose(container)
      }
  }

  private val resultUrl = "$endPoint:${container.port}"

  private val client: HttpClient = HttpClient {
    install(JsonFeature) {
      serializer = KotlinxSerializer()
    }
  }

  override suspend fun getAccessionNames(): List<String> {
    return client.get<AccessionNamesResponse> {
      apiUrl("$RESEARCH_ROUTE/$LIST_ROUTE")
    }?.let {
      debugLog("size of accessions = ${it.accessionNames.size}, ${it.accessionNames.toString()}")
      it.accessionNames
    }
  }

  override suspend fun initResearch(researchId: String): ResearchInitResponse {
    return client.get {
      apiUrl("$RESEARCH_ROUTE/$INIT_ROUTE/$researchId")
    }
  }

  override suspend fun getSlice(sliceRequest: SliceRequest, researchId: String): String {
    return client.post {
      apiUrl(researchId)
      body = Json.stringify(SliceRequest.serializer(), sliceRequest)
    }
  }

  override suspend fun getHounsfield(
    axialCoord: Int,
    frontalCoord: Int,
    sagittalCoord: Int
  ): Double {
    return client.get<HounsfieldResponse> {
      apiUrl("$RESEARCH_ROUTE/$HOUNSFIELD_ROUTE?$TYPE_AXIAL=${axialCoord}&$TYPE_FRONTAL=${frontalCoord}&$TYPE_SAGITTAL=${sagittalCoord}")
    }.huValue
  }

  private fun HttpRequestBuilder.apiUrl(path: String) {
    sessionDebounceSubject.onNext(container)
    url {
      takeFrom(resultUrl)
      encodedPath = path
    }
  }

}