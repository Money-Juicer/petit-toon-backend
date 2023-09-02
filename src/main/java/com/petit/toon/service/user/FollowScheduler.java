package com.petit.toon.service.user;

import com.petit.toon.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowScheduler {

    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * 0") // 매주 일요일 자정
    public void updateInfluencer() {
        userRepository.updateInfluenceStatus(true);
        userRepository.updateInfluenceStatus(false);
    }
}
