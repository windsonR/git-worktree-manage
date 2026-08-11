plugins {
    id("java") // Java support
    alias(libs.plugins.kotlin) // Kotlin support
    // 1.x: org.jetbrains.intellij -> 2.x: org.jetbrains.intellij.platform
    alias(libs.plugins.intelliJPlatform) // IntelliJ Platform Gradle Plugin
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(21)
}

// Configure project's dependencies
repositories {
    maven {
        url = uri("https://mirrors.huaweicloud.com/repository/maven/")
        name = "HuaweiCloud Maven"
    }
    maven {
        url = uri("https://www.jetbrains.com/intellij-repository/releases")
    }
    mavenCentral()

    // IntelliJ Platform Gradle Plugin Repositories Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
    intellijPlatform {
        defaultRepositories()
    }
}

// Dependencies are managed with Gradle version catalog - read more: https://docs.gradle.org/current/userguide/version_catalogs.html
dependencies {
    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        // 1.x intellij.version + intellij.type -> 2.x 平台依赖（IC 社区版用 intellijIdeaCommunity）
        intellijIdea(providers.gradleProperty("platformVersion"))

        // 1.x intellij.plugins -> 2.x 将 bundled 插件与 marketplace 插件分离声明
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })
        bundledModules(providers.gradleProperty("platformBundledModules").map { it.split(',') })
    }
}

// Configure IntelliJ Platform Gradle Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html
intellijPlatform {
    pluginConfiguration {
        // 1.x intellij.pluginName -> 2.x pluginConfiguration.name
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        // 1.x patchPluginXml.sinceBuild / untilBuild -> 2.x ideaVersion
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
        }
    }

    // 1.x signPlugin 任务 -> 2.x signing 扩展
    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    // 1.x publishPlugin 任务 -> 2.x publishing 扩展
    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }
}

tasks {
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }
}
