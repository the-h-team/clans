import java.util.Base64

plugins {
    id("pro.java-conventions")
    `maven-publish`
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:reference", true)
    options.quiet()
}

afterEvaluate {
    publishing {
        val publicationName = name
        publications.create<MavenPublication>(publicationName) {
            pom {
                name.set("ClansPro")
                description.set(project.description!!)
                url.set(project.properties["url"] as String)
                inceptionYear.set(project.properties["inceptionYear"] as String)
                organization {
                    name.set("Sanctum")
                    url.set("https://github.com/the-h-team")
                }
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://opensource.org/licenses/Apache-2.0")
                        distribution.set("manual") // TODO: override for API
                    }
                }
                developers {
                    developer {
                        id.set("ms5984")
                        name.set("Matt")
                        url.set("https://github.com/ms5984")
                    }
                    developer {
                        id.set("Hempfest")
                        name.set("Austin")
                        url.set("https://github.com/Hempfest")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/the-h-team/ClansPro.git")
                    developerConnection.set("scm:git:ssh://github.com/the-h-team/ClansPro.git")
                    url.set("https://github.com/the-h-team/ClansPro/tree/main")
                }
            }
            from(components["java"])
        }
        val signingKey = findProperty("base64SigningKey")
        val signingKeyPassphrase = findProperty("signingKeyPassphrase")
        if (signingKey is String && signingKeyPassphrase is String) {
            apply(plugin = "signing")
            configure<SigningExtension> {
                useInMemoryPgpKeys(
                    base64Decode(signingKey),
                    signingKeyPassphrase
                )
                sign(publishing.publications[publicationName])
            }
        }
    }
}

fun base64Decode(base64: String?) : String? {
    if (base64 == null) return null
    return Base64.getDecoder().decode(base64).toString(Charsets.UTF_8)
}
