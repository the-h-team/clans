plugins {
    id("pro.java-conventions")
}

repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "panther-snapshots"
        content {
            includeVersionByRegex("com.github.the-h-team", "panther.*", ".+-SNAPSHOT")
        }
    }
}

val pantherVersion by extra("1.0.2-SNAPSHOT")

dependencies {
    implementation("com.github.the-h-team:panther-common:$pantherVersion")
}
