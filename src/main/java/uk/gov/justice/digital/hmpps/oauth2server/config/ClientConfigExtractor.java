package uk.gov.justice.digital.hmpps.oauth2server.config;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClientConfigExtractor {

    private final ObjectMapper objectMapper;

    @Autowired
    public ClientConfigExtractor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<OauthClientConfig> getClientConfigurations(final String clientData) {
        List<OauthClientConfig> config;

        try {
            byte[] decodedData = Base64.decodeBase64(clientData);
            config = objectMapper.readValue(new String(decodedData), new TypeReference<List<OauthClientConfig>>() {
            });
        } catch (Exception e) {
            config = null;
        }
        return config;
    }
}