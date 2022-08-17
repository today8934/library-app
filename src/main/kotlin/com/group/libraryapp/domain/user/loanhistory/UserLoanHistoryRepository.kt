package com.group.libraryapp.domain.user.loanhistory

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserLoanHistoryRepository: JpaRepository<UserLoanHistory, Long> {

    fun findByBookNameAndStatus(bookName: String, status: UserLoanStatus): UserLoanHistory?

    fun findAllByStatus(status: UserLoanStatus): List<UserLoanHistory>

}