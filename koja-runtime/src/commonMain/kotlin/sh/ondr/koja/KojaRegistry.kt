package sh.ondr.koja

object KojaRegistry {
	val map = mutableMapOf<String, KojaMeta>()
}

/**
 * Pushes generated KDocs into the registry so we can retrieve them when generating json schemas.
 */
fun initializeKoja() { /* Serves as IR hook */ }
