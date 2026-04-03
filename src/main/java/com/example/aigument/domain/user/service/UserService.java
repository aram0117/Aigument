package com.example.aigument.domain.user.service;

import com.example.aigument.common.exception.CustomException;
import com.example.aigument.domain.user.entity.User;
import com.example.aigument.domain.user.dto.response.GetUserResponse;
import com.example.aigument.domain.user.repoistory.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.aigument.common.exception.ErrorCode.NOT_FOUND_USER;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public GetUserResponse getUser(Long id) {

        User foundUser = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        return GetUserResponse.from(foundUser);
    }
}
