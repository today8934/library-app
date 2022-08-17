package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.book.BookType
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanStatus
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import com.group.libraryapp.dto.book.response.BookStatResponse
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BookServiceTest @Autowired constructor(
    private val bookService: BookService,
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
) {

    @AfterEach
    fun clean() {
        bookRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("책 정상 등록 검증")
    fun saveBookTest() {
        // given
        val bookRequest = BookRequest("이펙티브 자바", BookType.COMPUTER)

        // when
        bookService.saveBook(bookRequest)

        // then
        val allBooks = bookRepository.findAll()
        assertThat(allBooks).hasSize(1)
        assertThat(allBooks[0].name).isEqualTo("이펙티브 자바")
        assertThat(allBooks[0].type).isEqualTo(BookType.COMPUTER)
    }

    @Test
    @DisplayName("책 대여 검증")
    fun loanBookTest() {
        // given
        val savedUser = userRepository.save(User("류욱상", 33))
        val savedBook = bookRepository.save(Book.fixture("이펙티브 자바"))
        val bookLoanRequest = BookLoanRequest("류욱상", "이펙티브 자바")

        // when
        bookService.loanBook(bookLoanRequest)
        val findAll = userLoanHistoryRepository.findAll()

        // then
        assertThat(findAll).hasSize(1)
        assertThat(findAll[0].bookName).isEqualTo(savedBook.name)
        assertThat(findAll[0].user.name).isEqualTo(savedUser.name)
        assertThat(findAll[0].status).isEqualTo(UserLoanStatus.LOANED)
    }

    @Test
    @DisplayName("이미 대출되어있는 책에대한 검증")
    fun loanBookFailTest() {
        // given
        userRepository.save(User("류욱상", 33))
        bookRepository.save(Book.fixture("이펙티브 자바"))
        val bookLoanRequest = BookLoanRequest("류욱상", "이펙티브 자바")
        bookService.loanBook(bookLoanRequest)

        // when & then
        val message = assertThrows<IllegalArgumentException> {
            bookService.loanBook(bookLoanRequest)
        }.message

        assertThat(message).isEqualTo("진작 대출되어 있는 책입니다")
    }

    @Test
    @DisplayName("책 반납이 정상 동작하는 경우에 대한 검증")
    fun returnBookTest() {
        // given
        val savedUser = userRepository.save(User("류욱상", 33))
        val savedBook = bookRepository.save(Book.fixture("이펙티브 자바"))
        val bookLoanRequest = BookLoanRequest("류욱상", "이펙티브 자바")
        bookService.loanBook(bookLoanRequest)
        val bookReturnRequest = BookReturnRequest(savedUser.name, savedBook.name)

        // when
        bookService.returnBook(bookReturnRequest)

        // then
        val findAll = userLoanHistoryRepository.findAll()
        assertThat(findAll[0].status).isEqualTo(UserLoanStatus.RETURNED)
    }

    @Test
    @DisplayName("책 대여 권수 정상 검증")
    fun countLoanedBookTest() {
        // given
        val savedUser = userRepository.save(User("류욱상", 33))
        userLoanHistoryRepository.saveAll(listOf(
            UserLoanHistory.fixture(savedUser, "A"),
            UserLoanHistory.fixture(savedUser, "B", UserLoanStatus.RETURNED),
            UserLoanHistory.fixture(savedUser, "C", UserLoanStatus.RETURNED),
        ))

        // when
        val count = bookService.countLoanedBook()

        // then
        assertThat(count).isEqualTo(1)
    }

    @Test
    @DisplayName("분야별 책 권수 검증")
    fun getBookStatisticsTest() {
        // given
        bookRepository.saveAll(listOf(
            Book.fixture("A", BookType.COMPUTER),
            Book.fixture("B", BookType.COMPUTER),
            Book.fixture("C", BookType.ECONOMY),
            Book.fixture("D", BookType.SCIENCE),
            Book.fixture("E", BookType.SOCIETY),
        ))

        // when
        val books = bookService.getBookStatistics()

        // then
        assertThat(books).hasSize(4)
        assertThat(books).extracting("type").containsExactlyInAnyOrder(
            BookType.COMPUTER,
            BookType.ECONOMY,
            BookType.SOCIETY,
            BookType.SCIENCE,
        )

        assertCount(books, BookType.COMPUTER, 2)
        assertCount(books, BookType.SCIENCE, 1)
        assertCount(books, BookType.SOCIETY, 1)
        assertCount(books, BookType.ECONOMY, 1)
    }

    private fun assertCount(books: List<BookStatResponse>, type: BookType, count: Long) {
        assertThat(books.first { book -> book.type == type }.count).isEqualTo(count)
    }
}