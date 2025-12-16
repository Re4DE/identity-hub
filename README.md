# Identity Hub

This Identity Hub is based on the [EDC Identity Hub](https://github.com/eclipse-edc/IdentityHub) in the version 0.14.0.

## Requirements

The Identity Hub can be deployed without any other services.
However, without a Connector it is useless.
To deploy the Identity Hub alongside a Connector see the [Connector](https://gitlab.cc-asp.fraunhofer.de/future-energy-lab-testfeld/connector) documentation.

## Configuration

As the Identity Hub can not be deployed without a Connector, a complete 
description of the configuration is part of the Connector [Helm Chart](https://gitlab.cc-asp.fraunhofer.de/future-energy-lab-testfeld/connector/-/blob/main/charts/connector-ssi/README.md?ref_type=heads).

## Local development

Follow these step to use the `local-dev` runtime.

### 1. Start environment from the connector repository

As the Identity Hub can not be used without a Connector, there need to be a running instance of it.
You can use the [local-dev](https://gitlab.cc-asp.fraunhofer.de/future-energy-lab-testfeld/connector#1-start-environment-with-docker-compose) runtime from the Connector for that.

### 2. Start the Identity Hub

From project root execute:
```shell
.\gradlew.bat runtimes:local-dev:build
java '-Dedc.fs.config=runtimes/local-dev/src/main/resources/config.properties' -jar runtimes/local-dev/build/libs/local-dev.jar
```

The build command is only needed if you have done some changes on the source code.
