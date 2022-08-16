package com.group.libraryapp.service.user

import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanStatus
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
) {

    @AfterEach
    fun afterEach() {
        userRepository.deleteAll()
    }

    @Test
    fun saveUserTest() {
        // given
        val userCreateRequest = UserCreateRequest("류욱상", null)

        // when
        userService.saveUser(userCreateRequest)

        // then
        val findAll = userRepository.findAll()
        assertThat(findAll).hasSize(1)
        assertThat(findAll[0].name).isEqualTo("류욱상")
        assertThat(findAll[0].age).isNull()
    }

    @Test
    fun getUsersTest() {
        // given
        userRepository.saveAll(listOf(
            User("A", 20),
            User("B", null)
        ))

        // when
        val users = userService.getUsers()

        // then
        assertThat(users).hasSize(2)
        assertThat(users).extracting("name").containsExactlyInAnyOrder("A", "B")
        assertThat(users).extracting("age").containsExactlyInAnyOrder(20, null)
    }

    @Test
    fun updateUserNameTest() {
        // given
        val savedUser = userRepository.save(User("A", null))
        val userUpdateRequest = UserUpdateRequest(savedUser.id!!, "B")

        // when
        userService.updateUserName(userUpdateRequest)

        // then
        val users = userService.getUsers()
        assertThat(users).extracting("name").containsExactlyInAnyOrder("B")
    }

    @Test
    fun deleteUserTest() {
        // given
        val user = userRepository.save(User("A", 20))

        // when
        userService.deleteUser(user.name)

        // then
        val users = userService.getUsers()
        assertThat(users).hasSize(0)
    }

    @Test
    @DisplayName("대출 기록이 없는 사용자도 응답에 포함되는지 검증")
    fun getUserLoanHistoriesTest1() {
        // given
        userRepository.save(User("A", 33))

        // when
        val results = userService.getUserLoanHistories()

        // then
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("A")
        assertThat(results[0].books).hasSize(0)
    }

    @Test
    @DisplayName("대출 기록이 많은 사용자의 응답 정상 검증")
    fun getUserLoanHistoriesTest2() {
        // given
        val savedUser = userRepository.save(User("A", 33))

        userLoanHistoryRepository.saveAll(listOf(
            UserLoanHistory.fixture(savedUser, "책1", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(savedUser, "책2", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(savedUser, "책3", UserLoanStatus.RETURNED),
        ))

        // when
        val results = userService.getUserLoanHistories()

        // then
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("A")
        assertThat(results[0].books).hasSize(3)
        assertThat(results[0].books).extracting("status").containsExactlyInAnyOrder(
            UserLoanStatus.LOANED,
            UserLoanStatus.LOANED,
            UserLoanStatus.RETURNED,
        )
    }

    @Test
    @DisplayName("대출 기록이 많은 사용자와 없는 사용자의 응답 정상 검증")
    fun getUserLoanHistoriesTest3() {
        // given
        val savedUser = userRepository.saveAll(listOf(
            User("A", 100),
            User("B", 200),
        ))

        userLoanHistoryRepository.saveAll(listOf(
            UserLoanHistory.fixture(savedUser[0], "책1", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(savedUser[0], "책2", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(savedUser[0], "책3", UserLoanStatus.RETURNED),
        ))

        // when
        val results = userService.getUserLoanHistories()

        // then
        val userAResult = results.first { it.name == "A" }

        assertThat(results).hasSize(2)
        assertThat(userAResult.name).isEqualTo("A")
        assertThat(userAResult.books).hasSize(3)
        assertThat(userAResult.books).extracting("status").containsExactlyInAnyOrder(
            UserLoanStatus.LOANED,
            UserLoanStatus.LOANED,
            UserLoanStatus.RETURNED,
        )

        val userBResult = results.first { it.name == "B" }
        assertThat(userBResult.books).isEmpty()
    }
}