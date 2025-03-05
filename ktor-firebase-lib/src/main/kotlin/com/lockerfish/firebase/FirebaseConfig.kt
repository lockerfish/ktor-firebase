package com.lockerfish.firebase

import io.ktor.server.application.*
import io.ktor.server.auth.*

/**
 * Configuration class for Firebase authentication.
 *
 * @param name The name of the authentication provider.
 */
class FirebaseConfig(name: String?) : AuthenticationProvider.Config(name) {

  /**
   * Function to validate the Firebase token.
   * This function should be overridden to provide custom validation logic.
   */
  internal var firebaseValidate: suspend ApplicationCall.(AuthorizedUser) -> Any? =
    {
      throw IllegalStateException(
        "Firebase auth validate function is not specified, use firebase { validate { ... } } to fix this"
      )
    }

  /**
   * Sets the validation function for the Authorized User.
   *
   * @param validate The validation function.
   */
  fun validate(validate: suspend ApplicationCall.(AuthorizedUser) -> Any?) {
    firebaseValidate = validate
  }
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