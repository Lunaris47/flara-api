package com.flara.security;

import com.flara.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Adapts Flara's own user records to the shape Spring Security expects.
 *
 * <p>Spring Security has no knowledge of {@link com.flara.entity.User}; it works
 * in terms of its own {@link UserDetails} contract. This class is the single
 * bridge between the two, and it is consulted from two places: by
 * {@link JwtFilter} when resolving a token's subject, and by the
 * {@code DaoAuthenticationProvider} in {@link SecurityConfig} during login.
 *
 * <p>Every user is granted the same {@code USER} role. Role-based access control
 * is not used in this application, because authorization is enforced per record
 * in the service layer by comparing a record's owner against the caller rather
 * than by consulting a role.
 */
@Service
@RequiredArgsConstructor
public class FlaraUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by username and maps it into Spring Security's model.
     *
     * <p>Only the fields Spring Security needs are copied across. The password
     * is the stored BCrypt hash, not a plaintext value; the framework compares
     * against it using the configured {@code PasswordEncoder}.
     *
     * @param username the username taken from a login request or a token subject
     * @return the user in Spring Security's representation
     * @throws UsernameNotFoundException if no account exists with that username,
     *                                   which the API surfaces as 401 rather than
     *                                   404 so that the response does not reveal
     *                                   which usernames are registered
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.flara.entity.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }
}
