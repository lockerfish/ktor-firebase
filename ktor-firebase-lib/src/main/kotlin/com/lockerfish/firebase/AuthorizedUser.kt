package com.lockerfish.firebase

data class AuthorizedUser(
  val uid: String,
  val tenantId: String?,
  val name: String?,
  val email: String,
  val isEmailVerified: Boolean,
  val picture: String?,
  val issuer: String?,
  val claims: Map<String, Any>,
)
