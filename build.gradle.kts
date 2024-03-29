import com.google.protobuf.gradle.* // ktlint-disable no-wildcard-imports

val grpcVersion = "1.41.0"
val grpcKotlinVersion = "1.2.0"
val protbufVersion = "3.11.1"
val protobufGradleVersion = "0.8.17"

plugins {
    java
    application
    kotlin("jvm") version "1.5.31"
    id("com.google.protobuf") version "0.8.17"
    id("org.openjfx.javafxplugin") version "0.0.10"
}

application {
    mainClass.set("no.nav.tekniskdemo.ChatClient")
}

// Generate IntelliJ IDEA's .idea & .iml project files.
// protobuf-gradle-plugin automatically registers *.proto and the gen output files
// to IntelliJ as sources.
// For best results, install the Protobuf and Kotlin plugins for IntelliJ.
apply(plugin = "idea")
apply(plugin = "com.google.protobuf")

// Alternative to recognize generated gRpc and proto generated source code instead of applying plugin = "idea"
java {
    val mainJavaSourceSet: SourceDirectorySet = sourceSets.getByName("main").java
    val protoSrcDir = "$buildDir/generated/source/proto/main"
    mainJavaSourceSet.srcDirs("$protoSrcDir/java", "$protoSrcDir/grpc", "$protoSrcDir/grpckotlin")
}

group = "no.nav.tekniskdemo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    // kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2-native-mt")

    // Grpc and Protobuf
    protobuf(files("src/proto/"))
    implementation("com.google.protobuf:protobuf-gradle-plugin:$protobufGradleVersion")
    implementation("io.grpc:grpc-api:$grpcVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")

    implementation("io.grpc:grpc-okhttp:$grpcVersion")

    if (JavaVersion.current().isJava9Compatible) {
        // Workaround for @javax.annotation.Generated
        // see: https://github.com/grpc/grpc-java/issues/3633
        compileOnly("javax.annotation:javax.annotation-api:1.3.2")
    }

    // identifiable
    implementation("de.huxhorn.sulky:de.huxhorn.sulky.ulid:8.2.0")

    // test
    testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_16
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "16"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "16"
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protbufVersion"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        id("grpckotlin") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk7@jar"
        }
    }

    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc")
                id("grpckotlin")
            }

        }
    }
}




fun ProtobufConfigurator.generateProtoTasks(action: ProtobufConfigurator.GenerateProtoTaskCollection.() -> Unit) {
    generateProtoTasks(closureOf(action))
}

javafx {
    version = "16"
    modules("javafx.base", "javafx.graphics","javafx.controls", "javafx.fxml")
}