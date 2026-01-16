package io.finplan.domain.service.register;

import io.finplan.api.dto.register.RegisterRequest;
import io.finplan.domain.entity.User;
import io.finplan.domain.repository.UserRepository;
import io.finplan.infrastructure.supabase.SupabaseAdminAuthClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class RegisterService {

    private final SupabaseAdminAuthClient supabaseAdminAuthClient;
    private final UserRepository userRepository;

    public UUID register(RegisterRequest req) {
        String authUserId = supabaseAdminAuthClient.createUser(req.email(), req.password());
        UUID uuid = UUID.fromString(authUserId);

        try {
            completeProfile(uuid, req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return uuid;
    }

    @Transactional
    protected void completeProfile(UUID uuid, RegisterRequest request) {
        User profile = userRepository.findById(uuid).orElseThrow(
                () -> new IllegalArgumentException("Trigger don't create public.users for auth.users created; user_id=" + uuid)
        );

        profile.setName(request.fullName());
        userRepository.save(profile);
    }
}
