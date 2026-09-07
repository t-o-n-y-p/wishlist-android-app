import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.openapi)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

sourceSets {
    main {
        java.srcDir(layout.buildDirectory.dir("/generate-resources/main/src/main/kotlin"))
    }
}

openApiGenerate {
    val openapiGroup = "ru.otus.wishlist.api"
    generatorName.set("kotlin")
    packageName.set(openapiGroup)
    apiPackage.set("$openapiGroup.api")
    modelPackage.set("$openapiGroup.models")
    invokerPackage.set("$openapiGroup.invoker")
    inputSpec.set(
        layout.projectDirectory
            .file("/src/main/resources/wishlist.yaml")
            .asFile.absolutePath
    )
    globalProperties.apply {
        put("models", "")
        put("modelDocs", "false")
    }
    configOptions = mapOf(
        "library" to "multiplatform",
        "enumPropertyNaming" to "UPPERCASE",
        "collectionType" to "list"
    )
    additionalProperties = mapOf(
        "dateLibrary" to "kotlinx-datetime"
    )
}

tasks {
    compileKotlin {
        dependsOn(openApiGenerate)
        kotlin {
            compilerOptions {
                jvmTarget = JvmTarget.JVM_17
            }
        }
    }
}