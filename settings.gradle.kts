rootProject.name = "clans"
sequenceOf(
    "api",
    "cli"
).forEach {
    include("${rootProject.name}-$it")
    project(":${rootProject.name}-$it").projectDir = file(it)
}