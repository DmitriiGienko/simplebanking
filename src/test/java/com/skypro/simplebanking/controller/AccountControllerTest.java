package com.skypro.simplebanking.controller;

import com.skypro.simplebanking.dto.UserDTO;
import com.skypro.simplebanking.entity.Account;
import com.skypro.simplebanking.entity.AccountCurrency;
import com.skypro.simplebanking.entity.User;
import com.skypro.simplebanking.repository.AccountRepository;
import com.skypro.simplebanking.repository.UserRepository;
import com.skypro.simplebanking.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

import java.util.List;
import java.util.stream.Collectors;

import static com.skypro.simplebanking.PreparingForTests.ObjectsForTests.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class AccountControllerTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private DataSource dataSource;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void createDataBase() {

//        userService.createUser("user1", "password1");
//        userService.createUser("user2", "password2");
//        userService.createUser("user3", "password3");

        List<User> users = getUsersForTests();
        List<UserDTO> userDTOList = users.stream().map(user ->
                userService.createUser(user.getUsername(), user.getPassword())

        ).collect(Collectors.toList());
        userRepository.saveAll(getUsersForTests());


    }

    @AfterEach
    public void cleanDataBases() {
        userRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @DisplayName("Получение данных по id аккаунта и id пользователя ")
    @Test
    @WithMockUser(roles = "USER")
    void getUserAccount() throws Exception {

//
//        Account account = new Account();
////        account.setId(1L);
//        account.setAccountCurrency(AccountCurrency.RUB);
//        account.setAmount(100L);
//        User userForTest = userRepository.findByUsername("user1").orElseThrow();
//        account.setUser(userForTest);
//        accountRepository.save(account);

        accountRepository.saveAll(getAccountForTests());


        mockMvc.perform(get("/account/{id}", String.valueOf(accountRepository.findAll().get(0)))
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value(AccountCurrency.RUB));
    }

    @Test
    void depositToAccount() {
    }

    @Test
    void withdrawFromAccount() {
    }
}