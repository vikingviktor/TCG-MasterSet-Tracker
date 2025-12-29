// Init script to configure KAPT for Java 17
// Run with: gradlew -I init.gradle.kts bundleRelease

beforeSettings {
    System.setProperty("org.gradle.jvmargs", "-Xmx2048m -XX:+IgnoreUnrecognizedVMOptions --add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED")
}

gradle.projectsLoaded {
    gradle.rootProject {
        subprojects {
            plugins.withId("kotlin") {
                extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension> {
                    compilerOptions {
                        freeCompilerArgs.addAll(
                            listOf(
                                "-J--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
                                "-J--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
                                "-J--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
                                "-J--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
                                "-J--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"
                            )
                        )
                    }
                }
            }
        }
    }
}
