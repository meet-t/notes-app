package io.noter.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name="users")
data class User(
    @Id
    var id: UUID? = UUID.randomUUID(),

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var password: String,

    @CreationTimestamp
    var createdOn: LocalDateTime?=null,

    @UpdateTimestamp
    var updatedOn: LocalDateTime?=null,
) {
    constructor(name: String, email: String, password: String) : this(
        id = null,
        name = name,
        email = email,
        password = password,
        createdOn = null,
        updatedOn = null,
    )

    @PrePersist
    fun prePersist() {
        if (id == null) {
            id = UUID.randomUUID() // Ensure that the UUID is generated before persisting
        }
    }
}
