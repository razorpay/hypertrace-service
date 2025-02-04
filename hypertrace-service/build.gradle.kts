plugins {
  java
  application
  jacoco
  id("org.hypertrace.docker-java-application-plugin")
  id("org.hypertrace.docker-publish-plugin")
  id("org.hypertrace.jacoco-report-plugin")
}

var hypertraceUiVersion = "latest"

dependencies {
  implementation("org.hypertrace.core.attribute.service:attribute-service")
  implementation("org.hypertrace.core.attribute.service:attribute-service-impl")
  implementation("org.hypertrace.entity.service:entity-service")
  implementation("org.hypertrace.entity.service:entity-service-impl")
  implementation("org.hypertrace.entity.service:entity-service-change-event-generator")
  implementation("org.hypertrace.core.query.service:query-service")
  implementation("org.hypertrace.core.query.service:query-service-impl")
  implementation("org.hypertrace.gateway.service:gateway-service")
  implementation("org.hypertrace.gateway.service:gateway-service-impl")
  implementation("org.hypertrace.graphql:hypertrace-graphql-service")
  implementation("org.hypertrace.core.graphql:hypertrace-core-graphql-spi")
  implementation("org.hypertrace.core.bootstrapper:config-bootstrapper")
  implementation("org.hypertrace.config.service:config-service")
  implementation("org.hypertrace.config.service:config-service-impl")

  implementation("org.eclipse.jetty:jetty-server:9.4.44.v20210927")
  implementation("org.eclipse.jetty:jetty-servlet:9.4.44.v20210927")
  implementation("org.eclipse.jetty:jetty-rewrite:9.4.44.v20210927")

  implementation("org.hypertrace.core.serviceframework:platform-service-framework:0.1.29")
  implementation("org.hypertrace.core.grpcutils:grpc-server-utils:0.6.1")
  implementation("org.hypertrace.core.grpcutils:grpc-client-utils:0.6.1")
  implementation("org.hypertrace.core.documentstore:document-store:0.6.4")
  implementation("com.typesafe:config:1.4.1")
  implementation("org.slf4j:slf4j-api:1.7.32")

  runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.17.1")
  runtimeOnly("io.grpc:grpc-netty:1.43.2")
}

application {
  mainClass.set("org.hypertrace.core.serviceframework.PlatformServiceLauncher")
}

hypertraceDocker {
  defaultImage {
    imageName.set("hypertrace-service")
    buildArgs.put("HYPERTRACE_UI_VERSION", hypertraceUiVersion)
    dockerFile.set(file("Dockerfile"))
    namespace.set("razorpay")
  }
  tag("${project.name}" + "_" + versionBanner())
}

fun versionBanner(): String {
  val os = com.bmuschko.gradle.docker.shaded.org.apache.commons.io.output.ByteArrayOutputStream()
  project.exec {
    commandLine = "git rev-parse --verify --short HEAD".split(" ")
    standardOutput = os
  }
  return String(os.toByteArray()).trim()
}

// Config for gw run to be able to run this locally. Just execute gw run here on Intellij or on the console.
tasks.run<JavaExec> {
  jvmArgs = listOf("-Dbootstrap.config.uri=file:${project.buildDir}/resources/main/configs", "-Dservice.name=${project.name}")
}

tasks.processResources {
  dependsOn("copyServiceConfigs");
  dependsOn("copyBootstrapConfigs")
}

tasks.register<Copy>("copyServiceConfigs") {
  with(
      createCopySpec("attribute-service", "attribute-service"),
      createCopySpec("entity-service", "entity-service"),
      createCopySpec("gateway-service", "gateway-service"),
      createCopySpec("query-service", "query-service"),
      createCopySpec("hypertrace-graphql", "hypertrace-graphql-service"),
      createCopySpec("config-service", "config-service")
  ).into("./build/resources/main/configs/")
}

tasks.register<Copy>("copyBootstrapConfigs") {
  with(
      createBootstrapCopySpec("config-bootstrapper", "config-bootstrapper")
  ).into("./build/resources/main/configs/")
}

fun createCopySpec(projectName: String, serviceName: String): CopySpec {
  return copySpec {
    from("../${projectName}/${serviceName}/src/main/resources/configs/common") {
      include("application.conf")
      into("${serviceName}")
    }
  }
}

fun createBootstrapCopySpec(projectName: String, serviceName: String): CopySpec {
  return copySpec {
    from("../${projectName}/${serviceName}/src/main/resources/configs/${serviceName}") {
      into("${serviceName}")
    }
  }
}
