# Identity Hub

[![docker version](https://img.shields.io/github/v/tag/Re4DE/identity-hub?style=flat-square&logo=docker&label=latest%20version)](https://github.com/orgs/Re4DE/packages?repo_name=identity-hub)
[![license](https://img.shields.io/github/license/Re4DE/identity-hub?style=flat-square&logo=apache)](https://www.apache.org/licenses/LICENSE-2.0)
<br>
[![ci](https://img.shields.io/github/check-runs/Re4DE/identity-hub/main?style=flat-square&logo=github&label=ci)](https://github.com/Re4DE/identity-hub/actions)
[![snapshot build](https://img.shields.io/github/actions/workflow/status/Re4DE/identity-hub/build_snapshot.yml?branch=main&style=flat-square&logo=github&label=snapshot-build)](https://github.com/Re4DE/identity-hub/actions/workflows/build_snapshot.yml)

---

This Identity Hub is based on the [EDC Identity Hub](https://github.com/eclipse-edc/IdentityHub) in the version 0.14.0.
The Identity Hub need a running instance of a `PostgreSQL` database and a `HashiCorp Vault`.
The Identity Hub is part of the Connector software bundle. 
A productive standalone deployment is possible, but requires a deeply technical understanding of the EDCs.

## Versioning

We use semantic versioning and add the Eclipse Dataspace Components (EDC) version as a label to indicate compatibility.
For example, version `1.0.0-edc0.14.0` means that version `1.0.0` of the Identity Hub is compatible with all EDCs of version `0.14.0`.
If possible, we provide backports of fixes that affect older EDC versions as well.
To get the latest build of the Identity Hub, use the version `SNAPSHOT`.

## Configuration

The configuration parameters in the following table are the minimum fields needed to set up a local startup.
Many more configuration parameters are inherent from the EDCs, compare [EDC](https://github.com/eclipse-edc/IdentityHub).

| Name                                    | Example Value                        | Description                                                                                                                         |
|-----------------------------------------|--------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| edc.participant.id                      | did:web:localhost%3A10085:tester     | The ID of this Identity Hub, represented as DID:WEB, needs to be identical with the particpant ID of the used Connector             |
| edc.component.id                        | identityhub                          | The ID of this runtime component                                                                                                    |
| edc.hostname                            | localhost                            | Hostname of the Identity Hub                                                                                                        |
| edc.iam.did.web.use.https               | false                                | Switch between https and http for did providing, only `false` for local development                                                 |
| edc.issuer.api.superuser.key            | c3VwZXItdXNlcg==.devpass             | The api key of the super user in the form of 'base64(<participantId>).<random-string>'. If not present a random string will be used | 
| web.http.path                           | /api                                 | Default api path                                                                                                                    |
| web.http.port                           | 10080                                | Default api port                                                                                                                    |
| web.http.credentials.path               | /api/credentials                     | Credential api path                                                                                                                 |
| web.http.credentials.port               | 10081                                | Credential api port                                                                                                                 |
| web.http.identity.path                  | /api/identity                        | Identity api path                                                                                                                   |
| web.http.identity.port                  | 10082                                | Identity api port                                                                                                                   |
| web.http.sts.path                       | /api/sts                             | STS token api path                                                                                                                  |
| web.http.sts.port                       | 10083                                | STS token api port                                                                                                                  |
| web.http.version.path                   | /api/version                         | Version api path                                                                                                                    |
| web.http.version.port                   | 10084                                | Version api port                                                                                                                    |
| web.http.did.path                       | /                                    | DID api path                                                                                                                        |
| web.http.did.port                       | 10085                                | DID api port. If you change the port, the `edc.participant.id` needs to be updated accordingly                                      |
| edc.vault.hashicorp.url                 | http://localhost:8200                | The URL of the `HashiCorp Vault`                                                                                                    |
| edc.vault.hashicorp.token               | devpass                              | The `HashiCorp Vault` access token                                                                                                  |
| edc.sql.schema.autocreate               | true                                 | Flag to autogenerate tables in the `PostgreSQL` if not already done                                                                 |
| edc.datasource.default.user             | edc                                  | Username to authenticate in the database                                                                                            |
| edc.datasource.default.password         | devpass                              | Password to authenticate in the database                                                                                            |
| edc.datasource.default.url              | jdbc:postgresql://localhost:5432/edc | Connection URL of the database                                                                                                      |

## Production Deployment

The Identity Hub is part of the Connector software bundle. The productive deployment is part of the Connector [Helm Chart](https://github.com/Re4DE/connector/blob/main/charts/connector-dcp/README.md).

## Local development

Follow these step to use the `local-dev` runtime.

### 1. Start environment from the connector repository

As the Identity Hub cannot be used without a `PostgreSQL` database and a `HashiCorp Vault`, there must be a running instances of both.
You can use the [local-dev](https://github.com/Re4DE/connector/blob/main/runtimes/local-dev/docker-env/src/main/docker/docker-compose.yaml) runtime from the connector repository for that.
Furthermore, there needs to be a running instance of the Connector. 
You can use the local development environments of the [control-plane](https://github.com/Re4DE/connector/tree/main/runtimes/local-dev/controlplane-local) and [data-plane](https://github.com/Re4DE/connector/tree/main/runtimes/local-dev/dataplane-local).

### 2. Start the Identity Hub

From project root execute:
```shell
.\gradlew.bat runtimes:local-dev:build
java '-Dedc.fs.config=runtimes/local-dev/src/main/resources/config.properties' -jar runtimes/local-dev/build/libs/local-dev.jar
```

The build command is only needed if you have done some changes on the source code.
Both can also be triggered directly from your IDE, such as IntelliJ.
