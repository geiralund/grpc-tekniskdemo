import com.google.protobuf.gradle.* // ktlint-disable no-wildcard-imports

val grpcVersion = "1.29.0"
val grpcKotlinVersion = "0.1.3"
val protbufVersion = "3.11.1"
val protobufGradleVersion = "0.8.12"

plugins {
//    java
    kotlin("jvm") version "1.3.72"
    id("com.google.protobuf") version "0.8.12"
}

// Generate IntelliJ IDEA's .idea & .iml project files.
// protobuf-gradle-plugin automatically registers *.proto and the gen output files
// to IntelliJ as sources.
// For best results, install the Protobuf and Kotlin plugins for IntelliJ.
apply(plugin = "com.google.protobuf")
apply(plugin = "idea")

group = "no.nav.tekniskdemo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    // kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")



    // Grpc and Protobuf
    protobuf(files("src/proto/"))
    implementation("com.google.protobuf:protobuf-gradle-plugin:$protobufGradleVersion")
    api("io.grpc:grpc-api:$grpcVersion")
    api("io.grpc:grpc-protobuf:$grpcVersion")
    api("io.grpc:grpc-stub:$grpcVersion")
    api("io.grpc:grpc-netty-shaded:$grpcVersion")
    api("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")

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

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
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
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion"
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

java {
    val mainJavaSourceSet: SourceDirectorySet = sourceSets.getByName("main").java
    val protoSrcDir = "$buildDir/generated/source/proto/main"
    mainJavaSourceSet.srcDirs("$protoSrcDir/java", "$protoSrcDir/grpc", "$protoSrcDir/grpckotlin")
}


fun ProtobufConfigurator.generateProtoTasks(action: ProtobufConfigurator.GenerateProtoTaskCollection.() -> Unit) {
    generateProtoTasks(closureOf(action))
}