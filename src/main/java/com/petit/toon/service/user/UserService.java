package com.petit.toon.service.user;

import com.petit.toon.entity.user.Authority;
import com.petit.toon.entity.user.AuthorityType;
import com.petit.toon.entity.user.ProfileImage;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.AuthorityRepository;
import com.petit.toon.repository.user.ProfileImageRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.user.request.SignupServiceRequest;
import com.petit.toon.service.user.response.SignupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.petit.toon.service.user.ProfileImageService.DEFAULT_PROFILE_IMAGE_ID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final ProfileImageRepository profileImageRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse register(SignupServiceRequest request) {
        if (userRepository.findOneByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("이미 사용 중인 이메일 주소입니다");
        }

        Authority authority = authorityRepository.findOneByAuthorityName(AuthorityType.USER)
                .orElseThrow(() -> new RuntimeException("Authority does not exist. Initialize data of ROLE_USER"));

        ProfileImage defaultProfileImage = profileImageRepository.findById(DEFAULT_PROFILE_IMAGE_ID)
                .orElseThrow(() -> new RuntimeException("Default Profile Image not found"));

        User user = User.builder()
                .name(request.getName())
                .nickname(request.getNickname())
                .tag(request.getTag())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        user.setProfileImage(defaultProfileImage);
        user.assignAuthority(authority);

        userRepository.save(user);
        return new SignupResponse(user.getId());
    }
}
