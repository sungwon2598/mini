package com.swhong.mini.service;

import com.swhong.mini.dto.UserRequest;
import com.swhong.mini.dto.UserResponse;
import com.swhong.mini.entity.User;
import com.swhong.mini.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    // 캐시 적용 - 조회
    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("DB에서 조회: User ID = {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .age(request.getAge())
                .build();

        User saved = userRepository.save(user);
        log.info("User 생성: {}", saved.getId());
        return UserResponse.from(saved);
    }

    // 캐시 업데이트
    @CachePut(value = "users", key = "#id")
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setAge(request.getAge());

        User updated = userRepository.save(user);
        log.info("User 업데이트 및 캐시 갱신: {}", id);
        return UserResponse.from(updated);
    }

    // 캐시 삭제
    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found: " + id);
        }
        userRepository.deleteById(id);
        log.info("User 삭제 및 캐시 제거: {}", id);
    }
}