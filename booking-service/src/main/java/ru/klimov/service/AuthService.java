package ru.klimov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.klimov.controller.payload.UserPayload;
import ru.klimov.controller.payload.UserShortPayload;
import ru.klimov.dto.TokenResponseDto;
import ru.klimov.dto.UserResponseDto;
import ru.klimov.entity.Role;
import ru.klimov.entity.User;
import ru.klimov.exception.RegistrationException;
import ru.klimov.exception.UserNotFoundException;
import ru.klimov.mapper.UserMapper;
import ru.klimov.repository.UserRepository;
import ru.klimov.security.JwtUtil;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDto register(UserPayload userRequestDto) {
        String role = userRequestDto.getRole();

        if (!(role.equals(Role.ADMIN.toString()) || role.equals(Role.USER.toString()))) {
            throw new RegistrationException("Only one of two roles is available for installation: "
                    + Role.ADMIN + " or " + Role.USER + ".");
        }

        User newUser = userMapper.userPayloadToUser(userRequestDto);

        return userMapper.userToUserResponseDto(userRepository.save(newUser));
    }

    public TokenResponseDto login(UserShortPayload userShortPayload) {
        Optional<User> optionalUser = userRepository.findByUsername(userShortPayload.getUsername());

        if (optionalUser.isEmpty() || !passwordEncoder.matches(userShortPayload.getPassword(),
                optionalUser.get().getPassword())) {

            throw new UserNotFoundException("The user with the specified data was not found.");
        }

        return TokenResponseDto.builder()
                .token(jwtUtil.generateTokenForUser(optionalUser.get()))
                .build();
    }
}
