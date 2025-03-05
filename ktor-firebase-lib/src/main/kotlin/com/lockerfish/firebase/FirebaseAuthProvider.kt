package com.lockerfish.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.util.logging.*

/**
 * Logger for Firebase authentication provider.
 */
internal val logger = KtorSimpleLogger("com.lockerfish.firebase")

/**
 * Authentication provider for Firebase.
 *
 * @param config The configuration for Firebase authentication.
 * @param headerParser Function to parse the authorization header from the application call.
 * @param auth The FirebaseAuth instance used for token verification.
 * @param validate The validation function for Firebase tokens.
 */
internal class FirebaseAuthProvider(
  config: FirebaseConfig,
  private val headerParser: (ApplicationCall) -> HttpAuthHeader? = { call -> call.request.parseAuthorizationHeaderOrNull() },
  private val auth: FirebaseAuth = FirebaseAdmin.firebaseAuth,
  private val validate: suspend ApplicationCall.(AuthorizedUser) -> Any? = config.firebaseValidate,
) : AuthenticationProvider(config) {

  private val errorKey: String = "FirebaseAuthProvider"

  /**
   * Authenticates the context using Firebase.
   *
   * @param context The authentication context.
   */
  override suspend fun onAuthenticate(context: AuthenticationContext) {
    val token = headerParser(context.call)
    if (token == null) {
      context.error(errorKey, AuthenticationFailedCause.Error("Unable to parse authorization header"))
      return
    }

    if (token.authScheme != "Bearer" || token !is HttpAuthHeader.Single || token.blob.isEmpty()) {
      context.challenge(
        errorKey,
        AuthenticationFailedCause.InvalidCredentials
      ) { challengeFunc, _ ->
        challengeFunc.complete()
        context.error(errorKey, AuthenticationFailedCause.Error("Invalid token"))
      }
      logger.error("Invalid token.\n $token")
      return
    }

    val firebaseToken = try {
      auth.verifyIdToken(token.blob, true)
    } catch (ex: Exception) {
      logger.error("Firebase verification failed.\n ${ex.message ?: ex.javaClass.simpleName}")
      context.error(errorKey, AuthenticationFailedCause.Error("Firebase verification failed"))
      return
    }

    validate(context.call, firebaseToken.toAuthorizedUser())?.let {
      context.principal(it)
    } ?: run {
      logger.error("Validation returned a null principal.")
      context.error(errorKey, AuthenticationFailedCause.Error("Validation returned a null principal."))
    }
  }
}

/**
 * Parses the authorization header from the application request.
 *
 * @return The parsed HttpAuthHeader or null if parsing fails.
 */
internal fun ApplicationRequest.parseAuthorizationHeaderOrNull(): HttpAuthHeader? = try {
  parseAuthorizationHeader()
} catch (ex: Exception) {
  logger.error("Unable to parse authorization header.\n ${ex.message ?: ex.javaClass.simpleName}")
  null
}

internal fun FirebaseToken.toAuthorizedUser() =
  AuthorizedUser(
    this.uid,
    this.tenantId,
    this.name,
    this.email,
    this.isEmailVerified,
    this.picture,
    this.issuer,
    this.claims
  )
