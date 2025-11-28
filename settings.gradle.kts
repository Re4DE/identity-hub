rootProject.name = "identity-hub"

// this is needed to have access to snapshot builds of plugins
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}

// add dependencies
include(":runtimes:identityhub")
include(":runtimes:local-dev")

include(":extensions:superuser-seed")