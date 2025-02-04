plugins {
  java
  application
  jacoco
  id("org.hypertrace.docker-java-application-plugin")
  id("org.hypertrace.docker-publish-plugin")
  id("org.hypertrace.jacoco-report-plugin")
}

dependencies {
  implementation("org.hypertrace.core.attribute.service:attribute-service")
  implementation("org.hypertrace.core.attribute.service:attribute-service-impl")
  implementation("org.hypertrace.entity.service:entity-service")
  implementation("org.hypertrace.entity.service:entity-service-change-event-generator")
  implementation("org.hypertrace.entity.service:entity-service-impl")
  implementation("org.hypertrace.config.service:config-service")
  implementation("org.hypertrace.config.service:config-service-impl")

  implementation("org.eclipse.jetty:jetty-server:9.4.44.v20210927")
  implementation("org.eclipse.jetty:jetty-servlet:9.4.44.v20210927")
  implementation("org.eclipse.jetty:jetty-rewrite:9.4.44.v20210927")

  implementation("org.hypertrace.core.serviceframework:platform-service-framework:0.1.29")
  implementation("org.hypertrace.core.grpcutils:grpc-server-utils:0.6.1")
  implementation("org.hypertrace.core.grpcutils:grpc-client-utils:0.6.1")
  implementation("org.hypertrace.core.documentstore:document-store:0.6.0")
  implementation("com.typesafe:config:1.4.1")
  implementation("org.slf4j:slf4j-api:1.7.32")
  constraints {
    implementation("com.google.protobuf:protobuf-java:3.19.2") {
      because("https://snyk.io/vuln/SNYK-JAVA-COMGOOGLEPROTOBUF-2331703")
    }
  }

  runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.17.1")
  runtimeOnly("io.grpc:grpc-netty:1.43.2")

  testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

application {
  mainClass.set("org.hypertrace.core.serviceframework.PlatformServiceLauncher")
}

hypertraceDocker {
  defaultImage {
    imageName.set("hypertrace-service")
    javaApplication {
      envVars.put("CLUSTER_NAME", "default-cluster")
      envVars.put("POD_NAME", "default-pod")
    }
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
  jvmArgs = listOf("-Dservice.name=${project.name}", "-Dcluster.name=default-cluster")
}

tasks.processResources {
  dependsOn("copyServiceConfigs");
}

tasks.register<Copy>("copyServiceConfigs") {
  with(
      createCopySpec("entity-service", "entity-service"),
      createCopySpec("attribute-service", "attribute-service"),
      createCopySpec("config-service", "config-service")
  ).into("./build/resources/main/configs/")
}

fun createCopySpec(projectName: String, serviceName: String): CopySpec {
  return copySpec {
    from("../${projectName}/${serviceName}/src/main/resources/configs/common") {
      include("application.conf")
      into(serviceName)
    }
  }
}

tasks.test {
  useJUnitPlatform()
}
