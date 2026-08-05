package org.example.coding_convention.user.repository;

import org.example.coding_convention.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    List<User> findByNicknameLikeAndIdxNot(String nickname, Integer idx);
}
