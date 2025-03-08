package com.lockerfish.firebase

import com.google.firebase.auth.FirebaseAuth
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
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
  private val config: FirebaseConfig,
  private val headerParser: (ApplicationCall) -> HttpAuthHeader? = { call -> call.request.parseAuthorizationHeaderOrNull() },
  private val auth: FirebaseAuth = FirebaseAdmin.firebaseAuth,
  private val validate: AuthenticationFunction<AuthorizedUser> = config.authenticate,
) : AuthenticationProvider(config) {

  private val errorKey: String = "FirebaseAuthProvider"

  /**
   * Authenticates the context using Firebase.
   *
   * @param context The authentication context.
   */
  override suspend fun onAuthenticate(context: AuthenticationContext) {

    val token = (headerParser(context.call) as? HttpAuthHeader.Single)
      ?.takeIf {
        it.authScheme.equals(AuthScheme.Bearer, ignoreCase = true)
      }
      ?: let {
        context.challenge(errorKey, AuthenticationFailedCause.NoCredentials) { challengeFunc, call ->
          call.respond(unauthorizedResponse())
          challengeFunc.complete()
        }
        logger.warn("AuthHeader is not present or not a Bearer token.")
        return
      }

    val principal = try {
      auth.verifyIdToken(token.blob, true)
    } catch (ex: Exception) {
      logger.warn("Firebase verification failed.\n${ex.message ?: ex.javaClass.simpleName}")
      null
    }
      ?.let {
        validate(context.call, it.toAuthorizedUser())
      }
      ?: let {
        context.challenge(errorKey, AuthenticationFailedCause.InvalidCredentials) { challengeFunc, call ->
          call.respond(unauthorizedResponse())
          challengeFunc.complete()
        }
        logger.warn("Unable to obtain principal.")
        return
      }

    context.principal(principal)
  }

  /**
   * Creates an unauthorized response with the specified challenge.
   *
   * @return The unauthorized response.
   */
  private fun unauthorizedResponse() = UnauthorizedResponse(
    HttpAuthHeader.bearerAuthChallenge(
      scheme = AuthScheme.Bearer,
      realm = config.realm
    )
  )

}

/**
 * Registers a Firebase authentication provider.
 *
 * @param name The name of the authentication provider.
 * @param configure The configuration function for the Firebase authentication provider.
 */
fun AuthenticationConfig.firebase(
  name: String? = null,
  configure: FirebaseConfig.() -> Unit
) {
  val provider = FirebaseAuthProvider(FirebaseConfig(name).apply(configure))
  register(provider)
}
