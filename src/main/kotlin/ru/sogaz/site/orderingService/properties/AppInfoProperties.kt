package ru.sogaz.site.orderingService.properties

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import ru.sogaz.site.orderingService.loggerFor


/**
 * Репозиторий информации о приложении.
 */
@ConfigurationProperties(prefix = "app.info")
class AppInfoProperties {
    private val logger = loggerFor(javaClass)

    private lateinit var applicationName: String
    private lateinit var artifactId: String
    private lateinit var groupId: String
    private lateinit var description: String
    private lateinit var version: String
    private lateinit var javaVersion: String

    @PostConstruct
    fun postConstruct() {
        logger.info("PostConstruct:")
        logger.info("applicationName = $applicationName")
        logger.info("description = $description")
        logger.info("version = $version")
        logger.info("artifactId = $artifactId")
        logger.info("groupId = $groupId")
        logger.info("java.version = $javaVersion")
    }
}

