import com.hypherionmc.modpublisher.properties.CurseEnvironment
import com.hypherionmc.modpublisher.properties.ModLoader
import com.hypherionmc.modpublisher.properties.ReleaseType

plugins {
    id("com.gradleup.shadow")
    id("com.hypherionmc.modutils.modpublisher") version "2.+"
}

architectury {
    platformSetupLoomIde()
    fabric()
}

val minecraftVersion = project.properties["minecraft_version"] as String

configurations {
    create("common")
    "common" {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
    create("shadowBundle")
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    getByName("developmentFabric").extendsFrom(configurations["common"])
    "shadowBundle" {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

loom.accessWidenerPath.set(project(":Common").loom.accessWidenerPath)


// Fabric Datagen Gradle config.  Remove if not using Fabric datagen
fabricApi.configureDataGeneration()

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${project.properties["fabric_loader_version"]}")
    modApi("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_api_version"]}+$minecraftVersion")

    "common"(project(":Common", "namedElements")) { isTransitive = false }
    "shadowBundle"(project(":Common", "transformProductionFabric"))

    modApi("corgitaco.corgilib:Corgilib-Fabric:$minecraftVersion-${project.properties["corgilib_version"]}")

    modLocalRuntime("org.joml:joml:1.10.4")
    modLocalRuntime("org.anarres:jcpp:1.4.14")
    modLocalRuntime("io.github.douira:glsl-transformer:2.0.0-pre14")

    // Mod dependencies
    modLocalRuntime ("maven.modrinth:sodium:${project.properties["sodium_version"]}")
    modLocalRuntime ("maven.modrinth:iris:${project.properties["iris_version"]}")
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
    }

    shadowJar {
        exclude("architectury.common.json", "dev/corgitaco/enhancedcelestials/fabric/datagen/**")
        configurations = listOf(project.configurations.getByName("shadowBundle"))
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        injectAccessWidener.set(true)
        inputFile.set(shadowJar.get().archiveFile)
        dependsOn(shadowJar)
    }
}

publisher {
    apiKeys {
        curseforge(getPublishingCredentials().first)
        modrinth(getPublishingCredentials().second)
        github(project.properties["github_token"].toString())
    }

    curseID.set(project.properties["curseforge_id"].toString())
    modrinthID.set(project.properties["modrinth_id"].toString())
    githubRepo.set("https://github.com/JT122406/Enhanced-Celestials")
    setReleaseType(ReleaseType.RELEASE)
    projectVersion.set("${project.version}-fabric")
    displayName.set("${project.properties["mod_name"]}-fabric-${project.version}")
    changelog.set(projectDir.toPath().parent.resolve("CHANGELOG.md").toFile().readText())
    artifact.set(tasks.remapJar)
    setGameVersions(minecraftVersion)
    setLoaders(ModLoader.FABRIC, ModLoader.QUILT)
    setCurseEnvironment(CurseEnvironment.SERVER)
    setJavaVersions(JavaVersion.VERSION_21)
    val depends = mutableListOf("fabric-api", "corgilib")
    curseDepends.required.set(depends)
    modrinthDepends.required.set(depends)
}

private fun getPublishingCredentials(): Pair<String?, String?> {
    val curseForgeToken = (project.findProperty("curseforge_key") ?: System.getenv("CURSEFORGE_KEY") ?: "") as String?
    val modrinthToken = (project.findProperty("modrinth_key") ?: System.getenv("MODRINTH_KEY") ?: "") as String?
    return Pair(curseForgeToken, modrinthToken)
}
