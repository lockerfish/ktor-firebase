package com.lockerfish.firebase

import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlin.test.Test
import kotlin.test.assertEquals

class FirebaseAuthProviderTest {

  data class User(
    val id: String,
    val name: String,
    val email: String,
  )

  private val mockedFirebaseAuth = mockk<FirebaseAuth>(relaxed = true)
  private val defaultTestUser = User("123", "name", "email")

  @Test
  fun `should return Unauthorized if token is empty`() = testApplication {
    setupServer()
    testUnauthorizedRoute("/api/user")

    val response = client.get("/api/user") { header("Authorization", "Bearer ") }
    assertEquals(HttpStatusCode.Unauthorized, response.status)
  }

  @Test
  fun `should return Unauthorized if token is invalid`() = testApplication {
    setupServer()
    testUnauthorizedRoute("/api/user")

    val response = client.get("/api/user") { header("Authorization", "Bearer invalid token") }
    assertEquals(HttpStatusCode.Unauthorized, response.status)
  }

  @Test
  fun `should return Unauthorized if auth scheme is not Bearer`() = testApplication {
    setupServer()
    testUnauthorizedRoute("/api/user")

    val response = client.get("/api/user") { header("Authorization", "not Bearer") }
    assertEquals(HttpStatusCode.Unauthorized, response.status)
  }

  @Test
  fun `should return Unauthorized if validate returns null`() = testApplication {
    setupServer { null } // mock validate to return null
    testUnauthorizedRoute("/api/user")

    val response = client.get("/api/user") { header("Authorization", "Bearer 1234") }
    assertEquals(HttpStatusCode.Unauthorized, response.status)
  }

  @Test
  fun `should return OK if token is valid and validate is not null`() = testApplication {
    setupServer()
    testAuthorizedRoute(path = "/api/user")

    val response = client.get("/api/user") { header("Authorization", "Bearer 1234") }
    assertEquals(HttpStatusCode.OK, response.status)
  }

  @Test
  fun `should return OK when using named authentication config`() = testApplication {
    setupServer("admin")
    testAuthorizedRoute("admin", path = "/api/user")

    val response = client.get("/api/user") { header("Authorization", "Bearer 1234") }
    assertEquals(HttpStatusCode.OK, response.status)
  }

  private fun ApplicationTestBuilder.setupServer(
    configName: String? = null, mockValidate: suspend ApplicationCall.(AuthorizedUser) -> Any? = { defaultTestUser }
  ) {
    mockkStatic(FirebaseAuth::class)

    install(Authentication) {
      val provider = FirebaseAuthProvider(
        FirebaseConfig(configName),
        auth = mockedFirebaseAuth,
        validate = mockValidate
      )
      register(provider)
    }
  }

  private fun ApplicationTestBuilder.testAuthorizedRoute(
    configName: String? = null,
    path: String,
  ) {
    routing {
      authenticate(configName) {
        route(path) {
          get {
            val user = call.principal<User>() as User
            assertEquals(defaultTestUser, user)
            call.respond(HttpStatusCode.OK, "Authorized route has a verified token and a non-null principal")
          }
        }
      }
    }
  }

  private fun ApplicationTestBuilder.testUnauthorizedRoute(path: String) {
    routing {
      authenticate {
        route(path) {
          get {
            assertEquals(expected = false, actual = true, message = "Unauthorized route should not be called")
          }
        }
      }
    }
  }
}
