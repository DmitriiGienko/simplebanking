package com.skypro.simplebanking.controller;

import com.skypro.simplebanking.dto.AccountDTO;
import com.skypro.simplebanking.dto.UserDTO;
import com.skypro.simplebanking.entity.Account;
import com.skypro.simplebanking.entity.AccountCurrency;
import com.skypro.simplebanking.repository.AccountRepository;
import com.skypro.simplebanking.repository.UserRepository;
import com.skypro.simplebanking.service.UserService;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

import java.util.List;
import java.util.Optional;

import static com.skypro.simplebanking.PreparingForTests.ObjectsForTests.getAuthenticationHeader;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class TransferControllerTest {

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

    private String username1 = "user1";
    private String username2 = "user2";
    private String password1 = "password1";
    private String password2 = "password2";
    private UserDTO userDTO;
    private List<AccountDTO> accounts;

    @BeforeEach
    public void createDataBase() {
        userDTO = userService.createUser(username1, password1);
        accounts = userDTO.getAccounts();


        Optional<Account> accountByUser_idAndId = accountRepository.getAccountByUser_IdAndId(
                userDTO.getId(), accounts.get(2).getId());
        accountByUser_idAndId.ifPresent(account -> {
            account.setAmount(1000L);
            accountRepository.save(account);
        });
        userDTO = userService.createUser(username2, password2);
    }

    private AccountDTO getRubAccount() {
        return accounts.stream()
                .filter(accountDTO -> accountDTO.getCurrency().equals(AccountCurrency.RUB))
                .findFirst().orElse(null);
    }

    @Test
    void transfer() throws Exception {

        JSONObject transfer = new JSONObject();
        transfer.put("fromAccountId", 1);
        transfer.put("toUserId", 2);
        transfer.put("toAccountId", getRubAccount().getId());
        transfer.put("amount", 500);

        mockMvc.perform(post("/transfer")
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader(username1, password1))
                        .content(transfer.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

}