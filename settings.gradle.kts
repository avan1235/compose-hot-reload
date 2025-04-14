/*
 * Copyright 2024-2025 JetBrains s.r.o. and Compose Hot Reload contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        mavenLocal {
            mavenContent {
                includeGroupByRegex("org.jetbrains.kotlin.*")
            }
        }

        maven(file("build/repo"))
        maven("https://packages.jetbrains.team/maven/p/firework/dev") {
            mavenContent {
                includeGroupAndSubgroups("org.jetbrains.compose.hot-reload")
            }
        }

        gradlePluginPortal {
            content {
                includeGroupByRegex("org.gradle.*")
                includeGroupByRegex("com.gradle.*")
            }
        }

        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

/*
Configure Repositories / Dependencies
*/
dependencyResolutionManagement {
    versionCatalogs {
        create("deps") {
            from(files("dependencies.toml"))
        }
    }

    repositories {
        repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS


        mavenLocal {
            mavenContent {
                includeGroupByRegex("org.jetbrains.kotlin")
            }
        }

        maven(file("build/repo"))
        maven("https://packages.jetbrains.team/maven/p/firework/dev") {
            mavenContent {
                includeGroupAndSubgroups("org.jetbrains.compose.hot-reload")
            }
        }

        google {
            mavenContent {
                includeGroupByRegex(".*android.*")
                includeGroupByRegex(".*androidx.*")
                includeGroupByRegex(".*google.*")
            }
        }

        mavenCentral()
    }
}

include(":hot-reload-core")
include(":hot-reload-analysis")
include(":hot-reload-agent")
include(":hot-reload-orchestration")
include(":hot-reload-gradle-plugin")
include(":hot-reload-gradle-core")
include(":hot-reload-runtime-api")
include(":hot-reload-runtime-jvm")
include(":hot-reload-devtools")
include(":hot-reload-test")
include(":hot-reload-test:core")
include(":hot-reload-test:gradle-plugin")
include(":hot-reload-test:gradle-testFixtures")
include(":tests")


gradle.beforeProject {
    group = "org.jetbrains.compose.hot-reload"
    version = project.providers.gradleProperty("version").get()

    plugins.apply("test-conventions")
    plugins.apply("main-conventions")
    plugins.apply("kotlin-conventions")
}

gradle.afterProject {
    tasks.named { name -> name == "commonizeNativeDistribution" }.configureEach {
        enabled = false
    }
}


buildCache {
    local {
        directory = rootDir.resolve(".local/build-cache")
    }
}
