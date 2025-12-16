/*
 *  Copyright (c) 2025 Fraunhofer Institute for Energy Economics and Energy System Technology (IEE)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer IEE - initial API and implementation
 *
 */

package de.fraunhofer.iee.identityhub.superuser;

import org.eclipse.edc.identityhub.spi.authentication.ServicePrincipal;
import org.eclipse.edc.identityhub.spi.participantcontext.ParticipantContextService;
import org.eclipse.edc.identityhub.spi.participantcontext.model.KeyDescriptor;
import org.eclipse.edc.identityhub.spi.participantcontext.model.ParticipantManifest;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

@Extension(value = "Super User Seed Extension")
public class SuperUserSeedExtension implements ServiceExtension {
    public static final String DEFAULT_SUPER_USER_PARTICIPANT_ID = "super-user";

    @Setting(key = "edc.ih.api.superuser.key", description = "Explicitly set the initial API key for the Super User, if empty autogenerate", required = false)
    private String superUserApiKey;

    @Inject
    private ParticipantContextService participantContextService;

    @Inject
    private Vault vault;

    private Monitor monitor;

    @Override
    public void initialize(ServiceExtensionContext context) {
        this.monitor = context.getMonitor();
    }

    @Override
    public void start() {
        // Only create the Super User if not present
        if (participantContextService.getParticipantContext(DEFAULT_SUPER_USER_PARTICIPANT_ID).succeeded()) {
            monitor.debug("Super User already created, skip seeding");
            return;
        }

        // Create the Super User
        this.participantContextService.createParticipantContext(ParticipantManifest.Builder.newInstance()
                        .participantId(DEFAULT_SUPER_USER_PARTICIPANT_ID)
                        .did("did:web:%s".formatted(DEFAULT_SUPER_USER_PARTICIPANT_ID))
                        .active(true)
                        .key(KeyDescriptor.Builder.newInstance()
                                .keyGeneratorParams(Map.of("algorithm", "EdDSA", "curve", "Ed25519"))
                                .keyId("%s#key-1".formatted(DEFAULT_SUPER_USER_PARTICIPANT_ID))
                                .privateKeyAlias("%s#key-1".formatted(DEFAULT_SUPER_USER_PARTICIPANT_ID))
                                .build())
                        .roles(List.of(ServicePrincipal.ROLE_ADMIN))
                        .build())
                // If the creation was successful, we set the api or generate it if not present
                .onSuccess(generatedKey -> {
                    ofNullable(this.superUserApiKey)
                            .map(key -> {
                                if (!key.contains(".")) {
                                    monitor.warning("Super-user key override: this key appears to have an invalid format, you may be unable to access some APIs. It must follow the structure: 'base64(<participantId>).<random-string>'");
                                }
                                participantContextService.getParticipantContext(DEFAULT_SUPER_USER_PARTICIPANT_ID)
                                        .onSuccess(pc -> vault.storeSecret(pc.getApiTokenAlias(), key)
                                                .onSuccess(v -> monitor.debug("Super User key override successful"))
                                                .onFailure(f -> monitor.warning("Error storing API key in vault: %s".formatted(f.getFailureDetail()))))
                                        .onFailure(f -> monitor.warning("Error overriding API key for '%s': %s".formatted(DEFAULT_SUPER_USER_PARTICIPANT_ID, f.getFailureDetail())));
                                return key;
                            })
                            .orElseGet(() -> {
                                monitor.info("Super User key not provided. Generated: %s".formatted(generatedKey));
                                return generatedKey.apiKey();
                            });
                })
                // If there was anything wrong, throw an error
                .orElseThrow(f -> new EdcException("Error creating Super-User: " + f.getFailureDetail()));
    }
}
