plugins {
    kotlin("jvm")
    `maven-publish`
    `publishing-conventions`
    `tests-with-compiler`
}

tasks.withType<Jar>().named(kotlin.target.artifactsTaskName).configure {
    manifest.attributes(
        "Premain-Class" to "org.jetbrains.compose.reload.agent.ComposeHotReloadAgent",
        "Can-Redefine-Classes" to "true",
        "Can-Retransform-Classes" to "true",
    )
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
}

publishing {
    publications.create("maven", MavenPublication::class) {
        from(components["java"])
    }
}

dependencies {
    implementation(project(":hot-reload-orchestration"))
    implementation(deps.slf4j.api)
    implementation(deps.coroutines.core)
    implementation(deps.javassist)
    implementation(deps.asm)
    implementation(deps.asm.tree)
    testImplementation(deps.junit.jupiter)
    testImplementation(deps.junit.jupiter.engine)
    testImplementation(kotlin("test"))

    testImplementation(kotlin("compiler-embeddable"))
    testImplementation(deps.logback)
}

