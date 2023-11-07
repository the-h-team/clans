import org.gradle.api.Project

/**
 * Gets a subproject identifier.
 *
 * For example, `getSubproject("api")` = `":${rootProject.name}-api"`
 *
 * @param name the subproject
 */
fun Project.getSubproject(name: String) = ":${rootProject.name}-$name"