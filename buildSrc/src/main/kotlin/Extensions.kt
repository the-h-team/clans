import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure

/**
 * Gets a subproject identifier.
 *
 * For example, `getSubproject("api")` = `":${rootProject.name}-api"`
 *
 * @param name the subproject
 */
fun Project.getSubproject(name: String) = ":${rootProject.name}-$name"

/**
 * Configures the name property of the project's generated pom.
 *
 * In Maven, this is usually a human-readable name for the project.
 *
 * @param name a name
 */
fun Project.setMavenName(name: String) {
    afterEvaluate {
        configure<PublishingExtension> {
            publications.findByName(this@afterEvaluate.name)?.let { it as? MavenPublication }?.apply {
                pom {
                    this.name.set(name)
                }
            }
        }
    }
}