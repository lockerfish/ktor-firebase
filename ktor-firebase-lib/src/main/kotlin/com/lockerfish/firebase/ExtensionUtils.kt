package com.lockerfish.firebase

import com.google.firebase.auth.FirebaseToken
import io.ktor.http.auth.*
import io.ktor.server.auth.*
import io.ktor.server.request.*

/**
 * Parses the authorization header from the application request.
 *
 * @return The parsed HttpAuthHeader or null if parsing fails.
 */
internal fun ApplicationRequest.parseAuthorizationHeaderOrNull(): HttpAuthHeader? = try {
  parseAuthorizationHeader()
} catch (ex: Exception) {
  logger.warn("Unable to parse authorization header.\n${ex.message ?: ex.javaClass.simpleName}")
  null
}

/**
 * Converts a Firebase token to an AuthorizedUser.
 *
 * @return The AuthorizedUser object.
 */
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
