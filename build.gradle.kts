val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val kmongo_version:String by project
val commons_codec_version:String by project

plugins {
    kotlin("jvm") version "1.7.21"
    id("io.ktor.plugin") version "2.1.3"
                id("org.jetbrains.kotlin.plugin.serialization") version "1.7.21"
    id("com.github.johnrengelman.shadow") version "5.2.0"
        }

group = "com.androidjunior9"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
    project.setProperty("mainClassName", mainClass.get())
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven { url=
        uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    }
}
val sshAntTask = configurations.create("sshAntTask")

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-websockets-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-sessions:$ktor_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")
     //  Kmongo
    implementation("org.litote.kmongo:kmongo:$kmongo_version")
    implementation("org.litote.kmongo:kmongo-coroutine:$kmongo_version")



    implementation("commons-codec:commons-codec:$commons_codec_version")
    sshAntTask("org.apache.ant:ant-jsch:1.10.12")
}
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes(
            "Main-Class" to application.mainClass.get()
        )
    }
}

ant.withGroovyBuilder {
    "taskdef"(
        "name" to "scp",
        "classname" to "org.apache.tools.ant.taskdefs.optional.ssh.Scp",
        "classpath" to configurations.get("sshAntTask").asPath
    )
    "taskdef"(
        "name" to "ssh",
        "classname" to "org.apache.tools.ant.taskdefs.optional.ssh.SSHExec",
        "classpath" to configurations.get("sshAntTask").asPath
    )
}

task("deploy") {
    dependsOn("clean", "shadowJar")
    ant.withGroovyBuilder {
        doLast {
            val knownHosts = File.createTempFile("knownhosts", "txt")
            val user = "root"
            val host = "Ip Address"
            val key = file("keys/idttt")
            val jarFileName = "com.androidjunior9.tictactoemultiplayer-all.jar"
            try {
                "scp"(
                    "file" to file("build/libs/$jarFileName"),
                    "todir" to "$user@$host:/root/jwtauth",
                    "keyfile" to "$key",
                    "trust" to true,
                    "knownhosts" to knownHosts
                )
                "ssh"(
                    "host" to host,
                    "username" to user,
                    "keyfile" to "$key",
                    "trust" to true,
                    "knownhosts" to knownHosts,
                    "command" to "mv /root/jwtauth/$jarFileName /root/jwtauth/jwtauth.jar"
                )
                "ssh"(
                    "host" to host,
                    "username" to user,
                    "keyfile" to "$key",
                    "trust" to true,
                    "knownhosts" to knownHosts,
                    "command" to "systemctl stop jwtauth"
                )
                "ssh"(
                    "host" to host,
                    "username" to user,
                    "keyfile" to "$key",
                    "trust" to true,
                    "knownhosts" to knownHosts,
                    "command" to "systemctl start jwtauth"
                )
            } finally {
                knownHosts.delete()
            }
        }
    }
}

