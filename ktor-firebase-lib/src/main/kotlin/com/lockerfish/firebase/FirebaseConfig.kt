package com.lockerfish.firebase

import io.ktor.server.auth.*

/**
 * Configuration class for Firebase authentication.
 *
 * @param name The name of the authentication provider.
 */
class FirebaseConfig(name: String?) : AuthenticationProvider.Config(name) {

  var realm: String = "Server App"

  /**
   * Function to validate the Firebase token.
   * This function should be overridden to provide custom validation logic.
   */
  internal var authenticate: AuthenticationFunction<AuthorizedUser> =
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
  fun validate(validate: AuthenticationFunction<AuthorizedUser>) {
    this.authenticate = validate
  }
}
