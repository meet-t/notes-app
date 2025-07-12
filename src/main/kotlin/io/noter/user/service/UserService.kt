package io.noter.user.service

import io.noter.user.entity.User
import org.springframework.stereotype.Service

@Service
interface UserService {

    fun save(user: User): User

    fun findByEmail(email: String): User?
}