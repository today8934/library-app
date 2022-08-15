package com.group.libraryapp.service.user

import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val userService: UserService
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
}