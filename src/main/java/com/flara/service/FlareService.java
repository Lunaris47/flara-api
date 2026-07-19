package com.flara.service;

import com.flara.entity.Flare;
import com.flara.entity.User;
import com.flara.repository.FlareRepository;
import com.flara.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles saving and retrieving flare records.
 */
@Service
@RequiredArgsConstructor
public class FlareService {

    private final FlareRepository flareRepository;
    private final UserRepository userRepository;

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public Flare saveFlare(String username, Flare flare) {
        User user = getUser(username);
        flare.setUser(user);
        if (flare.getStartDate() == null) flare.setStartDate(LocalDateTime.now());
        return flareRepository.save(flare);
    }

    public List<Flare> getFlares(String username) {
        User user = getUser(username);
        return flareRepository.findByUserIdOrderByStartDateDesc(user.getId());
    }

    public List<Flare> getFlaresByRange(String username, LocalDateTime start, LocalDateTime end) {
        User user = getUser(username);
        return flareRepository.findByUserIdAndStartDateBetweenOrderByStartDateDesc(
                user.getId(), start, end);
    }

    public Flare updateFlare(String username, Long flareId, Flare updated) {
        User user = getUser(username);
        Flare existing = flareRepository.findById(flareId)
                .orElseThrow(() -> new RuntimeException("Flare not found."));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized.");
        }
        updated.setId(flareId);
        updated.setUser(user);
        return flareRepository.save(updated);
    }

    public void deleteFlare(String username, Long flareId) {
        User user = getUser(username);
        Flare existing = flareRepository.findById(flareId)
                .orElseThrow(() -> new RuntimeException("Flare not found."));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized.");
        }
        flareRepository.deleteById(flareId);
    }
}