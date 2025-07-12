package io.noter.note.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.util.*
import java.time.Instant

@Entity
@Table(name = "notes")
data class Note(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? =null,

    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false)
    var title: String,

    var content: String?,

    @CreationTimestamp
    var createdOn: Instant? =null,

    @UpdateTimestamp
    var updatedOn: Instant? =null,

    var expiresAt: Instant? = null
)
