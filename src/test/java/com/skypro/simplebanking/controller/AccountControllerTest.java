package com.skypro.simplebanking.controller;

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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

import java.util.List;

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
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("password1");
        user1.setAccounts(List.of());
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password2");
        User user3 = new User();
        user3.setUsername("user3");
        user3.setPassword("password3");

        userService.createUser(user1.getUsername(), user2.getPassword());
        userService.createUser(user2.getUsername(), user2.getPassword());
        userService.createUser(user3.getUsername(), user3.getPassword());

        Account account1 = new Account();
//        account1.setId(1L);
        account1.setAccountCurrency(AccountCurrency.EUR);
        account1.setAmount(1300L);
//        account1.setUser(user1);
        Account account2 = new Account();
//        account2.setId(2L);
        account2.setAccountCurrency(AccountCurrency.RUB);
        account2.setAmount(8000L);
//        account2.setUser(user2);
        Account account3 = new Account();
//        account3.setId(3L);
        account3.setAccountCurrency(AccountCurrency.USD);
        account3.setAmount(3200L);
//        account3.setUser(user3);
        List<Account> accounts = List.of(account1, account2, account3);

        accountRepository.saveAll(accounts);



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

////        createDataBase();
////        accountRepository.saveAll(getAccountForTests());
//        Long accountId = c;
//        String userName = getAccountForTests().get(0).getUser().getUsername();
//        String userPassword = getAccountForTests().get(0).getUser().getPassword();
//        String accountCorrency = getAccountForTests().get(0).getAccountCurrency().toString();
        User user = userRepository.findByUsername("user1").orElseThrow();

//        Account account = new Account();
//        account.setId(1L);
//        account.setAccountCurrency(AccountCurrency.RUB);
//        account.setAmount(100L);
//        account.setUser(user);
//        accountRepository.save(account);


        mockMvc.perform(get("/account/{id}", String.valueOf(1))
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value(AccountCurrency.USD));
    }

    @Test
    void depositToAccount() {
    }

    @Test
    void withdrawFromAccount() {
    }
}