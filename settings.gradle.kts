/*
 *  Copyright (c) 2025 Fraunhofer Institute for Energy Economics and Energy System Technology (IEE)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer IEE - initial API and implementation
 *
 */

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