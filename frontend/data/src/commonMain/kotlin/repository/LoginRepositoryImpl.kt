package repository

import local.LoginLocal
import local.LoginLocalDataSource
import model.*
import remote.LoginRemote
import remote.LoginRemoteDataSource

class LoginRepositoryImpl : LoginRepository {

    private val local: LoginLocal = LoginLocalDataSource()
    private val remote: LoginRemote = LoginRemoteDataSource()

    override suspend fun auth(login: String, password: String) {
        val response = remote.auth(AuthorizationRequest(login, password))
        return when {
            response.response != null -> local.saveToken(token = response.response!!.token)
            response.error != null -> handleErrorResponse(response.error!!)
            else -> throw ResearchApiExceptions.AuthFailedException
        }
    }

    override suspend fun tryToAuth(login: String, password: String) {
        remote.tryToAuth()
    }

    private fun <T : Any> handleErrorResponse(errorModel: ErrorModel): T {
        when (errorModel.error) {
            ErrorStringCode.AUTH_FAILED.value -> throw ResearchApiExceptions.AuthFailedException
            ErrorStringCode.INVALID_AUTH_CREDENTIALS.value -> throw ResearchApiExceptions.InvalidAuthCredentials
            else -> throw Exception(BASE_ERROR)
        }
    }

}