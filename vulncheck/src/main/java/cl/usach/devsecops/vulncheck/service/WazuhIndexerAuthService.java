package cl.usach.devsecops.vulncheck.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

@Service
public class WazuhIndexerAuthService {

    @Value("${wazuh.ssh.enabled:true}")
    private boolean sshEnabled;

    @Value("${wazuh.indexer.local-port}")
    private int indexerLocalPort;

    @Value("${wazuh.indexer.remote-host}")
    private String indexerRemoteHost;

    @Value("${wazuh.indexer.remote-port}")
    private int indexerRemotePort;

    @Value("${wazuh.indexer.user}")
    private String indexerUser;

    @Value("${wazuh.indexer.password}")
    private String indexerPassword;

    private final RestTemplate restTemplate;
    private String cachedToken;
    private Instant tokenExpiry = Instant.MIN;

    public WazuhIndexerAuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getToken() {
        if (cachedToken == null || Instant.now().isAfter(tokenExpiry)) {
            cachedToken = fetchToken();
            tokenExpiry = Instant.now().plusSeconds(890);
        }
        return cachedToken;
    }

    @SuppressWarnings("unchecked")
    private String fetchToken() {
        String base = sshEnabled
                ? "https://127.0.0.1:" + indexerLocalPort
                : "https://" + indexerRemoteHost + ":" + indexerRemotePort;

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(indexerUser, indexerPassword, StandardCharsets.UTF_8);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                base + "/_plugins/_security/authtoken", request,
                (Class<Map<String, Object>>) (Class<?>) Map.class);

        return (String) response.getBody().get("authorization_token");
    }
}
