package com.skypro.simplebanking.controller;

import com.skypro.simplebanking.dto.AccountDTO;
import com.skypro.simplebanking.dto.UserDTO;
import com.skypro.simplebanking.entity.AccountCurrency;
import com.skypro.simplebanking.repository.AccountRepository;
import com.skypro.simplebanking.repository.UserRepository;
import com.skypro.simplebanking.service.UserService;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import static com.skypro.simplebanking.PreparingForTests.ObjectsForTests.getAuthenticationHeader;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class TransferControllerTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withUsername("postgres")
            .withPassword("postgres");
    private UserDTO userDTO;

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
    private List<AccountDTO> accounts;

    private AccountDTO accountDTO;


    @BeforeEach

    public void createDataBase() {
        UserDTO userDTO1 = userService.createUser("user1", "password1");
        UserDTO userDTO2 = userService.createUser("user2", "password2");
    }


    private AccountDTO getRubAccount(List<AccountDTO> accounts) {
        return accounts.stream()
                .filter(accountDTO -> accountDTO.getCurrency().equals(AccountCurrency.RUB))
                .findFirst().orElse(null);
    }


    @AfterEach
    public void cleanDataBases() {
        userRepository.deleteAll();
        accountRepository.deleteAll();
    }


    @DisplayName("Перевод $")
    @Test
    void shouldTransfer_Ok() throws Exception {

        Long fromUserId = userRepository.findByUsername("user1").get().getId();
        Long toUserId = userRepository.findByUsername("user2").get().getId();
        Long fromId = getRubAccount(userService.getUser(fromUserId).getAccounts()).getId();
        Long toId = getRubAccount(userService.getUser(toUserId).getAccounts()).getId();

        JSONObject transfer = new JSONObject();
        transfer.put("fromAccountId", fromId);
        transfer.put("toUserId", toUserId);
        transfer.put("toAccountId", toId);
        transfer.put("amount", 1);

        mockMvc.perform(post("/transfer")
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1", "password1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transfer.toString()))
                .andExpect(status().isOk());
    }

    @DisplayName("Неверный Id аккаунта")
    @Test
    void shouldNoTransfer_IncorrectId() throws Exception {

        Long fromUserId = userRepository.findByUsername("user1").get().getId();
        Long toUserId = userRepository.findByUsername("user2").get().getId();
        Long fromId = getRubAccount(userService.getUser(fromUserId).getAccounts()).getId();
        Long toId = getRubAccount(userService.getUser(toUserId).getAccounts()).getId();

        JSONObject transfer = new JSONObject();
        transfer.put("fromAccountId", fromId);
        transfer.put("toUserId", toUserId);
        transfer.put("toAccountId", toId + 1);
        transfer.put("amount", 1);

        mockMvc.perform(post("/transfer")
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1", "password1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transfer.toString()))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Неверный сумма перевода")
    @Test
    void shouldNoTransfer_IncorrectAmount() throws Exception {

        Long fromUserId = userRepository.findByUsername("user1").get().getId();
        Long toUserId = userRepository.findByUsername("user2").get().getId();
        Long fromId = getRubAccount(userService.getUser(fromUserId).getAccounts()).getId();
        Long toId = getRubAccount(userService.getUser(toUserId).getAccounts()).getId();

        JSONObject transfer = new JSONObject();
        transfer.put("fromAccountId", fromId);
        transfer.put("toUserId", toUserId);
        transfer.put("toAccountId", toId);
        transfer.put("amount", 5);
        mockMvc.perform(post("/transfer")
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1", "password1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transfer.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(String.format("Cannot withdraw %d %s", 5, AccountCurrency.RUB)));

        transfer.put("amount", -1);

        mockMvc.perform(post("/transfer")
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1", "password1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transfer.toString()))
                .andExpect(status().isBadRequest());


    }
}






