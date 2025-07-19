import org.jetbrains.kotlin.konan.properties.Properties

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin)

    id("maven-publish") // 배포를 위한 모듈
    id("signing") // 서명을 위한 모듈
}

/**
 * Project Settings
 */
ext["libraryName"] = "pattern-lock-screen"
ext["libraryVersion"] = "1.0.1"
ext["description"] = "Android Pattern Lock Screen Composable Function"
ext["url"] = "https://io.github.com/squart300kg/pattern-lock-screen/"

/**
 * Signing Settings
 */
ext["signing.keyId"] = "" // GPG 키를 생성하고 나온 keyId - 5번의 GPG 키 생성 후 local.properties에 등록
ext["signing.password"] = "" // GPG 키를 생성할 때 적어준 비밀번호 - 5번의 GPG 키 생성 후 local.properties에 등록
ext["signing.key"] = "" // GPG 키 생성 후 BASE64를 한 키값
// key 값의 경우 찾아보는 문서마다 어디서는 파일을, 어디서는 키값을 복붙 하도록 할 수 있습니다. 여기서는 키값을 그냥 붙여 넣어서 쓰겠습니다.
ext["ossrhUsername"] = "" // 지라 가입 시 사용한 Username
ext["ossrhPassword"] = "" // 지라 가입 시 사용한 Password

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

val androidSourceJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName<com.android.build.gradle.api.AndroidSourceSet>("main").java.srcDirs)
}

fun getExtraString(name: String) = ext[name]?.toString()

fun groupId(): String = "io.github.squart300kg"

afterEvaluate {
    // Grabbing secrets from local.properties file or from environment variables, which could be used on CI
    val secretPropsFile = project.rootProject.file("local.properties")
    if (secretPropsFile.exists()) {
        secretPropsFile.reader().use {
            Properties().apply {
                load(it)
            }
        }.onEach { (name, value) ->
            ext[name.toString()] = value
        }
    } else {
        // Use system environment variables
        ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
        ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
        ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
        ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
        ext["signing.key"] = System.getenv("SIGNING_KEY")
    }

    // Set up Sonatype repository
    publishing {
        val artifactName = getExtraString("libraryName") ?: name
        val libraryVersion = getExtraString("libraryVersion") ?: "DEV"
        val artifactDescription = getExtraString("description") ?: ""
        val artifactUrl: String = getExtraString("url") ?: "https://io.github.squart300kg/"

        println("artifactName $artifactName")
        println("libraryVersion $libraryVersion")
        println("artifactDescription $artifactDescription")
        println("artifactUrl $artifactUrl")

        // Configure maven central repository
        repositories {
            maven {
                name = "sonatype"
                setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = getExtraString("ossrhUsername")
                    password = getExtraString("ossrhPassword")
                }
            }
        }

        // Configure all publications
        publications {
            create<MavenPublication>("release") {
                groupId = groupId()
                artifactId = artifactName
                version = libraryVersion

                if (project.plugins.hasPlugin("com.android.library")) {
                    from(components.getByName("release"))
                } else {
                    from(components.getByName("java"))
                }

                // Stub android
                artifact(androidSourceJar.get())
                // Stub javadoc.jar artifact
                artifact(javadocJar.get())

                // Provide artifacts information requited by Maven Central
                pom {
                    name.set(artifactName)
                    description.set(artifactDescription)
                    url.set(artifactUrl)

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("sangyoonsong")
                            name.set("sangyoon")
                            email.set("squart300kg@naver.com")
                        }
                    }
                    scm {
                        url.set(artifactUrl)
                    }
                }
            }
        }
    }

    // Signing artifacts. Signing.* extra properties values will be used
    signing {
        useInMemoryPgpKeys(
            getExtraString("signing.keyId"),
            getExtraString("signing.key"),
            getExtraString("signing.password"),
        )
        sign(publishing.publications)
    }
}

android {
    namespace = "com.screen.lock.pattern"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.material)
    implementation(libs.junit)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}