package com.lockerfish.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import io.ktor.server.application.*

/**
 * Singleton object responsible for initializing and providing access to Firebase services.
 */
object FirebaseAdmin {
  private lateinit var projectId: String

  /**
   * Gets the FirebaseApp instance associated with the project ID.
   */
  private val firebaseApp: FirebaseApp
    get() = FirebaseApp.getInstance(projectId)

  /**
   * Gets the FirebaseAuth instance associated with the FirebaseApp.
   */
  internal val firebaseAuth: FirebaseAuth
    get() = FirebaseAuth.getInstance(firebaseApp)

  /**
   * Initializes the FirebaseApp with the provided application configuration.
   *
   * application.yaml:
   * ```yaml
   * firebase:
   *  projectId: <projectId>
   *  serviceAccount: <serviceAccount.json>
   *  databaseUrl: [optional] https://<projectId>.firebaseio.com
   *  storageBucket: [optional] <projectId>.appspot.com
   *  ```
   *
   * @param application The Ktor application instance.
   */
  fun initialize(application: Application) {
    val config = application.environment.config.config("firebase")

    with(config) {
      projectId = property("projectId").getString()
      val serviceAccountFile = property("serviceAccount").getString()
      val serviceAccount = application::class.java.classLoader.getResourceAsStream(serviceAccountFile)

      val databaseUrl = propertyOrNull("databaseUrl")?.getString()
      val storageBucket = propertyOrNull("storageBucket")?.getString()

      with(FirebaseOptions.builder()) {
        setCredentials(GoogleCredentials.fromStream(serviceAccount))
        if (databaseUrl != null) setDatabaseUrl(databaseUrl)
        if (storageBucket != null) setStorageBucket(storageBucket)
        FirebaseApp.initializeApp(build(), projectId)
      }
    }
  }
}