package remote

import com.badoo.reaktive.observable.debounce
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.scheduler.computationScheduler
import com.badoo.reaktive.subject.publish.PublishSubject
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import model.*
import repository.FantomLibraryDataSource
import debugLog

class FantomLibraryDataSourceImpl(
  override val endPoint: String,
  override val onClose: () -> Unit
) : FantomLibraryDataSource {

  private val sessionDebounceSubject = PublishSubject<Boolean>()

  private val client: HttpClient = HttpClient {
    install(JsonFeature) {
      serializer = KotlinxSerializer(Json(JsonConfiguration(ignoreUnknownKeys = true)))
    }
    install(HttpTimeout) {
      requestTimeoutMillis = 600000
    }
    install(Logging) {
      logger = Logger.DEFAULT
      level = LogLevel.ALL
    }
  }

  init {
    sessionDebounceSubject
      .debounce(tenMinutes, computationScheduler)
      .subscribe { container ->
        debugLog("going to close container")
        onClose()
      }
  }

  override suspend fun getAccessionNames(): List<String> {
    return client.get<AccessionNamesResponse> {
      apiUrl("$RESEARCH_ROUTE/$LIST_ROUTE")
    }.let {
      debugLog("size of accessions = ${it.accessionNames.size}, ${it.accessionNames}")
      it.accessionNames
    }
  }

  override suspend fun initResearch(accessionNumber: String): ResearchInitResponseNew {
    return client.get {
      apiUrl("$RESEARCH_ROUTE/$INIT_ROUTE")
    }
  }

  override suspend fun getSlice(
    sliceRequest: SliceRequestNew,
    researchName: String
  ): SliceResponse {
    return client.post {
      apiUrl("/$RESEARCH_ROUTE/$SLICE_ROUTE")
      val stringify = Json.stringify(SliceRequestNew.serializer(), sliceRequest)
      body = stringify
    }
  }

  override suspend fun getHounsfield(
    request: HounsfieldRequestNew
  ): HounsfieldResponse {
    return client.post {
      apiUrl("$RESEARCH_ROUTE/$BRIGHTNESS_ROUTE")
      body = Json.stringify(HounsfieldRequestNew.serializer(), request)
    }
  }

  override suspend fun closeSession() {
    return client.get {
      apiUrl("$RESEARCH_ROUTE/$CLOSE_ROUTE")
    }
  }

  private fun HttpRequestBuilder.apiUrl(path: String) {
    sessionDebounceSubject.onNext(true)
    url {
      takeFrom(endPoint)
      encodedPath = path
    }
  }

}
