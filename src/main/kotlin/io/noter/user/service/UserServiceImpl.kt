package io.noter.user.service

import io.noter.user.entity.User
import io.noter.user.repo.UserRepository
import org.springframework.stereotype.Service


@Service
class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {

    override fun save(user: User): User {
        return userRepository.save(user)
    }


    override fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }
}

