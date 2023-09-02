package com.petit.toon.service.user;

import com.petit.toon.entity.user.Authority;
import com.petit.toon.entity.user.AuthorityType;
import com.petit.toon.entity.user.ProfileImage;
import com.petit.toon.entity.user.User;
import com.petit.toon.exception.badrequest.EmailAlreadyRegisteredException;
import com.petit.toon.exception.internalservererror.AuthorityNotExistException;
import com.petit.toon.exception.internalservererror.DefaultProfileImageNotExistException;
import com.petit.toon.exception.notfound.UserNotFoundException;
import com.petit.toon.repository.user.AuthorityRepository;
import com.petit.toon.repository.user.ProfileImageRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.user.request.SignupServiceRequest;
import com.petit.toon.service.user.request.UserUpdateServiceRequest;
import com.petit.toon.service.user.response.SignupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
            throw new EmailAlreadyRegisteredException();
        }

        Authority authority = authorityRepository.findOneByAuthorityName(AuthorityType.USER)
                .orElseThrow(AuthorityNotExistException::new);

        ProfileImage defaultProfileImage = profileImageRepository.findById(DEFAULT_PROFILE_IMAGE_ID)
                .orElseThrow(DefaultProfileImageNotExistException::new);

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

    @Transactional
    public void updateUserProfile(UserUpdateServiceRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(UserNotFoundException::new);

        if (StringUtils.hasText(request.getPassword())) {
            request.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updateForm = User.builder()
                .nickname(request.getNickname())
                .tag(request.getTag())
                .password(request.getPassword())
                .statusMessage(request.getStatusMessage())
                .build();

        user.updateProfile(updateForm);
    }
}
