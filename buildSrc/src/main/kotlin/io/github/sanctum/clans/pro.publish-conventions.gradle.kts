plugins {
    id("pro.java-conventions")
    `maven-publish`
}

publishing {
    publications.create<MavenPublication>(name) {
        from(components["java"])
    }
}
