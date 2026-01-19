package com.shcho.myBlog.user.repository;

import com.shcho.myBlog.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
