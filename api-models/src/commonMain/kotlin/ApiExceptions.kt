package model

sealed class ResearchApiExceptions(val error: String) : Throwable() {

  object AuthFailedException : ResearchApiExceptions(AUTH_FAILED)
  object InvalidAuthCredentials : ResearchApiExceptions(INVALID_AUTH_CREDENTIALS)

  object ResearchListFetchException : ResearchApiExceptions(USER_RESEARCHES_LIST_FAILED)
  object ResearchInitializationException : ResearchApiExceptions(RESEARCH_INITIALIZATION_FAILED)
  object ResearchDataFetchError : ResearchApiExceptions(RESEARCH_DATA_FETCH_FAILED)
  object ResearchNotFoundException : ResearchApiExceptions(RESEARCH_NOT_FOUND)
  object CloseResearchException : ResearchApiExceptions(RESEARCH_CLOSE_FAILED)

  object SliceFetchException : ResearchApiExceptions(GET_SLICE_FAILED)
  object IncorrectSliceNumberException : ResearchApiExceptions(INCORRECT_SLICE_NUMBER)

  object HounsfieldFetchError : ResearchApiExceptions(HOUNSFIELD_FETCH_ERROR)
  object IncorrectAxialValueException : ResearchApiExceptions(INCORRECT_AXIAL_COORD)
  object IncorrectFrontalValueException : ResearchApiExceptions(INCORRECT_FRONTAL_COORD)
  object IncorrectSagittalValueException : ResearchApiExceptions(INCORRECT_SAGITTAL_COORD)

  object MarksFetchException : ResearchApiExceptions(MARKS_FETCH_EXCEPTION)
  object MarkCreateException : ResearchApiExceptions(MARK_CREATE_EXCEPTION)
  object MarkUpdateException : ResearchApiExceptions(MARK_UPDATE_EXCEPTION)
  object MarkDeleteException : ResearchApiExceptions(MARK_DELETE_EXCEPTION)

  object ConfirmCtTypeForResearchException : ResearchApiExceptions(CREATE_MARK_FAILED)

  object CloseSessionException : ResearchApiExceptions(SESSION_CLOSE_FAILED)
  object SessionExpiredException : ResearchApiExceptions(SESSION_EXPIRED)
}
