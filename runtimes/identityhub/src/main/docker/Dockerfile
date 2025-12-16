#
#  Copyright (c) 2025 Fraunhofer Institute for Energy Economics and Energy System Technology (IEE)
#
#  This program and the accompanying materials are made available under the
#  terms of the Apache License, Version 2.0 which is available at
#  https://www.apache.org/licenses/LICENSE-2.0
#
#  SPDX-License-Identifier: Apache-2.0
#
#  Contributors:
#       Fraunhofer IEE - initial API and implementation
#
#

FROM eclipse-temurin:17.0.14_7-jdk-alpine AS builder

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew clean runtimes:identityhub:build

FROM eclipse-temurin:22.0.2_9-jre-alpine

# Optional JVM arguments, such as memory settings
ARG JVM_ARGS=""

RUN apk --no-cache add curl

WORKDIR /app

COPY --from=builder /app/runtimes/identityhub/build/libs/identityhub.jar identityhub.jar

HEALTHCHECK --interval=5s --timeout=5s --retries=10 CMD curl --fail http://localhost:8080/api/check/health

# Use "exec" for graceful termination (SIGINT) to reach JVM.
# ARG can not be used in ENTRYPOINT so storing value in an ENV variable
ENV ENV_JVM_ARGS=$JVM_ARGS
CMD [ "sh", "-c", "exec java $ENV_JVM_ARGS -jar identityhub.jar --log-level=debug"]