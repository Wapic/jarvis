val enabled_platforms: String by rootProject
architectury {
    common(enabled_platforms)
}

val fabric_loader_version: String by rootProject

dependencies {
}

tasks.named("remapJar", AbstractArchiveTask::class) {
    archiveClassifier.set("")
    from("fabric.mod.json")
}

tasks.jar {
    archiveClassifier.set("dev")
}

