package io.finplan.infrastructure.supabase;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class SupabaseAdminAuthClient {

    public final RestClient restClient;
    public final String serviceRoleKey;

    public SupabaseAdminAuthClient(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service-role-key}") String serviceRoleKey
    ) {
        this.serviceRoleKey = serviceRoleKey;
        this.restClient = RestClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader("apikey", serviceRoleKey)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + serviceRoleKey)
                .build();
    }


    public String createUser(String email, String password) {
        Map<String, Object> body = Map.of(
                "email", email,
                "password", password,
                "email_confirm", true
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = this.restClient.post()
                    .uri("/auth/v1/admin/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            assert response != null;
            return (String) response.get("id");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteUser(String userId) {
         this.restClient.delete()
                 .uri("/auth/v1/admin/users/{id}", userId)
                 .retrieve()
                 .toBodilessEntity();
    }
}
