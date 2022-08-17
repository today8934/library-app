package com.group.libraryapp.domain.book

import com.group.libraryapp.dto.book.response.BookStatResponse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BookRepository: JpaRepository<Book, Long> {
    fun findByName(bookName: String): Book?

    @Query("""
        SELECT NEW com.group.libraryapp.dto.book.response.BookStatResponse(B.type, COUNT(B.id)) 
        FROM Book B 
        GROUP BY B.type
    """)
    fun getStats(): List<BookStatResponse>
}