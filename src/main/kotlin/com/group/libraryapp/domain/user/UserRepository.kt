package com.group.libraryapp.domain.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository: JpaRepository<User, Long> {

    fun findByName(name: String): User?

    @Query("SELECT DISTINCT U FROM User U LEFT JOIN FETCH U.userLoanHistories")
    fun findAllWithHistories(): List<User>
}