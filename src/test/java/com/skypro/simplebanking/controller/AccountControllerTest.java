package com.skypro.simplebanking.controller;

import com.skypro.simplebanking.dto.AccountDTO;
import com.skypro.simplebanking.dto.UserDTO;
import com.skypro.simplebanking.entity.Account;
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
import java.util.Optional;

import static com.skypro.simplebanking.PreparingForTests.ObjectsForTests.getAuthenticationHeader;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    private String username = "user1";
    private String password = "password1";
    private UserDTO userDTO;
    private List<AccountDTO> accounts;

    @BeforeEach
    public void createDataBase() {
        userDTO = userService.createUser(username, password);
        accounts = userDTO.getAccounts();

        Optional<Account> accountByUser_idAndId = accountRepository.getAccountByUser_IdAndId(
                userDTO.getId(), accounts.get(2).getId());
        accountByUser_idAndId.ifPresent(account -> {
            account.setAmount(1000L);
            accountRepository.save(account);
        });
    }

    @AfterEach
    public void cleanDataBases() {
        userRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @DisplayName("Получение данных по id аккаунта и id пользователя ")
    @Test
    void shouldGetUserAccount_Ok() throws Exception {
        mockMvc.perform(get("/account/{id}", getRubAccount().getId()
                )
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value(AccountCurrency.RUB.name()))
                .andExpect(jsonPath("$.amount").value(1000));
    }

    @DisplayName("Ошибка получения данных по id аккаунта и id пользователя ")
    @Test
    void shouldGetUserAccount_ErrorUserName() throws Exception {
        mockMvc.perform(get("/account/{id}", getRubAccount().getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("username13", password)))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Ошибка получения данных - не верный id аккаунта")
    @Test
    void shouldGetUserAccount_ErrorAccountId() throws Exception {
        mockMvc.perform(get("/account/{id}", getRubAccount().getId() + 1)
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader(username, password)))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Пополнение счета")
    @Test
    void shouldDepositToAccount_Ok() throws Exception {

        JSONObject balanceChange = new JSONObject();
        balanceChange.put("amount", 500L);

        mockMvc.perform(post("/account/deposit/{id}", getRubAccount().getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader(username, password))
                        .content(balanceChange.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1500));
    }

    @DisplayName("Недопустимая сумма пополнения")
    @Test
    void shouldNotDepositToAccount_invalidAmount() throws Exception {

        JSONObject balanceChange = new JSONObject();
        balanceChange.put("amount", -15L);

        mockMvc.perform(post("/account/deposit/{id}", getRubAccount().getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader(username, password))
                        .content(balanceChange.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Amount should be more than 0"));
    }

    @DisplayName("Списание средств")
    @Test
    void shouldWithdrawFromAccount_Oк() throws Exception {

        JSONObject balanceChange = new JSONObject();
        balanceChange.put("amount", 500L);

        mockMvc.perform(post("/account/withdraw/{id}", getRubAccount().getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader(username, password))
                        .content(balanceChange.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(500));
    }

    @DisplayName("Недопустимая сумма списания")
    @Test
    void shouldNotWithdrawFromAccount_invalidAmount() throws Exception {

        JSONObject balanceChange = new JSONObject();
        balanceChange.put("amount", -100L);

        mockMvc.perform(post("/account/withdraw/{id}", getRubAccount().getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader(username, password))
                        .content(balanceChange.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Amount should be more than 0"));
    }

    @DisplayName("Превышена сумма списания")
    @Test
    void shouldNotWithdrawFromAccount_exceededAmount() throws Exception {

        long amount = 1500L;

        JSONObject balanceChange = new JSONObject();
        balanceChange.put("amount", amount);

        mockMvc.perform(post("/account/withdraw/{id}", getRubAccount().getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader(username, password))
                        .content(balanceChange.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(String.format("Cannot withdraw %d %s", amount, AccountCurrency.RUB)));
    }

    @DisplayName("Списание невозможно - ошибка данных аккаунта/пользователя")
    @Test
    void shouldNotWithdrawFromAccount_InputsDataError() throws Exception {
        mockMvc.perform(get("/account/{id}", getRubAccount().getId() + 1)
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader(username, password)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/account/{id}", getRubAccount().getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("username1", password)))
                .andExpect(status().isUnauthorized());

    }

    private AccountDTO getRubAccount() {
        return accounts.stream()
                .filter(accountDTO -> accountDTO.getCurrency().equals(AccountCurrency.RUB))
                .findFirst().orElse(null);
    }
}