import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("dev.architectury.loom") version "1.1-SNAPSHOT" apply false
    `maven-publish`
}

val minecraft_version: String by rootProject
val mod_version: String by rootProject
val yarn_version: String by rootProject
val maven_group: String by rootProject

architectury {
    minecraft = minecraft_version
}

subprojects {
    apply(plugin = "dev.architectury.loom")

    dependencies {
        "minecraft"("com.mojang:minecraft:${minecraft_version}")
        // The following line declares the yarn mappings you may select this one as well.
        "mappings"("net.fabricmc:yarn:${yarn_version}:v2")
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")

    version = mod_version
    group = maven_group

    repositories {
        ivy("https://github.com/HotswapProjects/HotswapAgent/releases/download") {
            patternLayout {
                artifact("[revision]/[artifact]-[revision].[ext]")
            }
            content {
                includeGroup("virtual.github.hotswapagent")
            }
            metadataSources {
                artifact()
            }
        }
    }

    val hotswap by configurations.creating {
        isVisible = false
    }

    dependencies {
        hotswap("virtual.github.hotswapagent:hotswap-agent:1.4.2-SNAPSHOT")
    }
    project.afterEvaluate {
        project.extensions.findByType<LoomGradleExtensionAPI>()?.apply {
            runs {
                named("client") {
                    vmArg("-ea")
                    if (project.properties["jarvis.hotswap"] == "true") {
                        vmArg("-XX:+AllowEnhancedClassRedefinition")
                        vmArg("-XX:HotswapAgent=external")
                        vmArg("-javaagent:${hotswap.resolve().single().absolutePath}")
                    }
                }
            }
        }
    }

    configure<BasePluginExtension> {
        archivesName.set("jarvis")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    configure<JavaPluginExtension> {
        withSourcesJar()
    }
}
subprojects {
    publishing {
        if (project.name != "common")
            publications {
                create<MavenPublication>("maven-api") {
                    artifactId = rootProject.name + "-" + project.name
                    from(components["java"])
                    pom {
                        licenses {
                            license {
                                name.set("LGPL-3.0 or later")
                                url.set("https://github.com/romangraef/jarvis/blob/HEAD/.licensesnip")
                            }
                        }
                        developers {
                            developer {
                                name.set("Linnea Gräf")
                                url.set("https://nea.moe")
                                email.set("nea+jarvis@nea.moe")
                            }
                        }
                        name.set("Jarivs Common Config Interface")
                        description.set("Jarvis Common Config Interface is a search engine and hud editor allowing you to edit all your minecraft mods config options and HUDs in one place")
                        inceptionYear.set("2023")
                        issueManagement {
                            url.set("https://github.com/romangraef/jarvis/issues")
                            system.set("Github Issues")
                        }
                        scm {
                            url.set("https://github.com/romangraef/jarvis")
                        }
                    }
                }
            }
    }

    configure<LoomGradleExtensionAPI> {
        runs {
            removeIf { it.name == "server" }
            named("client") {
                property("jarvis.test", "true")
            }
        }
    }
}