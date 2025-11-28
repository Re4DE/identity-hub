plugins {
    id("application")
    alias(libs.plugins.shadow)
}

dependencies {
    runtimeOnly(libs.bundles.ih)

    runtimeOnly(project(":extensions:superuser-seed"))
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("${project.name}.jar")
}

edcBuild {
    publish.set(false)
}
