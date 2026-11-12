package de.fraunhofer.iee.identityhub.superuser;

import org.eclipse.edc.boot.system.injection.ObjectFactory;
import org.eclipse.edc.identityhub.spi.participantcontext.IdentityHubParticipantContextService;
import org.eclipse.edc.identityhub.spi.participantcontext.model.CreateParticipantContextResponse;
import org.eclipse.edc.identityhub.spi.participantcontext.model.IdentityHubParticipantContext;
import org.eclipse.edc.identityhub.spi.participantcontext.model.ParticipantManifest;
import org.eclipse.edc.junit.extensions.DependencyInjectionExtension;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.result.ServiceResult;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.system.configuration.ConfigFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(DependencyInjectionExtension.class)
class SuperUserSeedExtensionTest {

    private static final String SUPER_USER_PARTICIPANT_ID = "super-user";
    private static final String API_TOKEN_ALIAS = "super-user-apikey";
    private static final String SUPER_USER_API_KEY = "c3VwZXItdXNlcg==.ZGV2cGFzcw==";
    private static final String[] SUPER_USER_ROLE = { "identity-api:admin", "identity-api:write", "identity-api:read" };

    private final IdentityHubParticipantContextService participantContextService = mock();
    private final Vault vault = mock();
    private final Monitor monitor = mock();
    private final IdentityHubParticipantContext participantContext = mock();

    private ServiceExtensionContext context;
    private ObjectFactory factory;

    @BeforeEach
    void setUp(ServiceExtensionContext context, ObjectFactory factory) {
        this.context = context;
        this.factory = factory;

        when(context.getMonitor()).thenReturn(monitor);
        context.registerService(IdentityHubParticipantContextService.class, participantContextService);
        context.registerService(Vault.class, vault);
    }

    @Test
    void shouldSkip_whenSuperUserAlreadyExists() {
        when(participantContextService.getParticipantContext(SUPER_USER_PARTICIPANT_ID)).thenReturn(ServiceResult.success(participantContext));

        var extension = extension(factory, context, null);
        extension.start();

        verify(monitor).info("Super User already created, skip seeding");
        verify(participantContextService, never()).createParticipantContext(any());
        verifyNoInteractions(vault);
    }

    @Test
    void shouldCreateWithCorrectManifest_whenSuperUserDoesNotExist() {
        when(participantContextService.getParticipantContext(SUPER_USER_PARTICIPANT_ID)).thenReturn(ServiceResult.notFound("not found"));
        when(participantContextService.createParticipantContext(any())).thenReturn(ServiceResult.success(new CreateParticipantContextResponse("generated-key", null, null)));

        var extension = extension(factory, context, null);
        extension.start();

        var captor = ArgumentCaptor.forClass(ParticipantManifest.class);
        verify(participantContextService).createParticipantContext(captor.capture());

        var manifest = captor.getValue();
        var key = manifest.getKeys().stream().filter(k -> k.getKeyId().equals("super-user#key-1")).findFirst().orElse(null);
        assertThat(manifest.getParticipantContextId()).isEqualTo(SUPER_USER_PARTICIPANT_ID);
        assertThat(manifest.getDid()).isEqualTo("did:web:%s".formatted(SUPER_USER_PARTICIPANT_ID));
        assertThat(manifest.isActive()).isTrue();
        assertThat(key).isNotNull();
        assertThat(key.getKeyId()).isEqualTo("super-user#key-1");
        assertThat(key.getPrivateKeyAlias()).isEqualTo("super-user#key-1");
        assertThat(key.getKeyGeneratorParams()).containsEntry("algorithm", "EdDSA");
        assertThat(manifest.getScopes()).containsExactly(SUPER_USER_ROLE);

        verify(monitor).info(contains("Super User key not provided. Generated:"));
        verifyNoInteractions(vault);
    }

    @Test
    void shouldStoreApiKeyInVault_whenSuperUserDoesNotExistAndApiKeyProvided() {
        when(participantContextService.getParticipantContext(SUPER_USER_PARTICIPANT_ID))
                .thenReturn(ServiceResult.notFound("not found")).thenReturn(ServiceResult.success(participantContext));
        when(participantContextService.createParticipantContext(any()))
                .thenReturn(ServiceResult.success(new CreateParticipantContextResponse("generated-key", null, null)));
        when(vault.storeSecret(any(), any())).thenReturn(Result.success());
        when(participantContext.getApiTokenAlias()).thenReturn(API_TOKEN_ALIAS);

        var extension = extension(factory, context, SUPER_USER_API_KEY);
        extension.start();

        verify(monitor).info("Super User key override successful");
        verify(vault).storeSecret(API_TOKEN_ALIAS, SUPER_USER_API_KEY);
    }

    @Test
    void shouldWarn_whenApiKeyHasInvalidFormat() {
        when(participantContextService.getParticipantContext(SUPER_USER_PARTICIPANT_ID))
                .thenReturn(ServiceResult.notFound("not found")).thenReturn(ServiceResult.success(participantContext));
        when(participantContextService.createParticipantContext(any()))
                .thenReturn(ServiceResult.success(new CreateParticipantContextResponse("generated-key", null, null)));
        when(vault.storeSecret(any(), any())).thenReturn(Result.success());
        when(participantContext.getApiTokenAlias()).thenReturn(API_TOKEN_ALIAS);

        var extension = extension(factory, context, "invalid-format-key");
        extension.start();

        verify(monitor).severe(contains("Super-user key override: this key appears to have an invalid format, you may be unable to access some APIs. It must follow the structure: 'base64(<participantId>).<random-string>'"));
        verifyNoInteractions(vault);
    }

    @Test
    void shouldWarn_whenVaultFailsToStoreApiKey() {
        when(participantContextService.getParticipantContext(SUPER_USER_PARTICIPANT_ID))
                .thenReturn(ServiceResult.notFound("not found")).thenReturn(ServiceResult.success(participantContext));
        when(participantContextService.createParticipantContext(any()))
                .thenReturn(ServiceResult.success(new CreateParticipantContextResponse("generated-key", null, null)));
        when(vault.storeSecret(any(), any())).thenReturn(Result.failure("vault error"));
        when(participantContext.getApiTokenAlias()).thenReturn(API_TOKEN_ALIAS);

        var extension = extension(factory, context, SUPER_USER_API_KEY);
        extension.start();

        verify(monitor).warning("Error storing API key in vault: vault error");
    }

    @Test
    void shouldWarn_whenParticipantContextRetrievalFailsAfterCreation() {
        when(participantContextService.getParticipantContext(SUPER_USER_PARTICIPANT_ID))
                .thenReturn(ServiceResult.notFound("not found")).thenReturn(ServiceResult.notFound("retrieval error"));
        when(participantContextService.createParticipantContext(any()))
                .thenReturn(ServiceResult.success(new CreateParticipantContextResponse("generated-key", null, null)));

        var extension = extension(factory, context, SUPER_USER_API_KEY);
        extension.start();

        verify(monitor).warning("Error overriding API key for '%s': %s".formatted(SUPER_USER_PARTICIPANT_ID, "retrieval error"));
        verifyNoInteractions(vault);
    }

    @Test
    void shouldThrow_whenParticipantCreationFails() {
        when(participantContextService.getParticipantContext(SUPER_USER_PARTICIPANT_ID))
                .thenReturn(ServiceResult.notFound("not found"));
        when(participantContextService.createParticipantContext(any()))
                .thenReturn(ServiceResult.badRequest("invalid manifest"));

        var extension = extension(factory, context, null);

        var exception = assertThrows(EdcException.class, extension::start);
        assertThat(exception.getMessage()).contains("Error creating Super-User: invalid manifest");
    }

    private SuperUserSeedExtension extension(ObjectFactory factory, ServiceExtensionContext context, String apiKey) {
        Map<String, String> configMap = apiKey != null ? Map.of("edc.ih.api.superuser.key", apiKey) : Map.of();
        when(context.getConfig()).thenReturn(ConfigFactory.fromMap(configMap));

        var extension = factory.constructInstance(SuperUserSeedExtension.class);
        extension.initialize(context);
        return extension;
    }
}