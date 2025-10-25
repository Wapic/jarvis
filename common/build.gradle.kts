val enabled_platforms: String by rootProject
architectury {
    common(enabled_platforms)
}

loom {
    accessWidenerPath.set(file("src/main/resources/jarvis.accesswidener"))
}

val fabric_loader_version: String by rootProject

dependencies {
    api(project(path = ":api", configuration = "namedElements"))
    modImplementation("net.fabricmc:fabric-loader:${fabric_loader_version}")
}
