plugins {
    id("pro.java-conventions")
    `maven-publish`
}

afterEvaluate {
    publishing {
        publications.create<MavenPublication>(name) {
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
            // TODO signing plugin
            from(components["java"])
        }
    }
}
