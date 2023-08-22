package com.skypro.simplebanking.controller;

import com.skypro.simplebanking.repository.UserRepository;
import com.skypro.simplebanking.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;

import static com.skypro.simplebanking.PreparingForTests.ObjectsForTests.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class UserControllerTest {

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
    MockMvc mockMvc;
    @Autowired
    private UserService userService;

    @AfterEach
    public void cleanUserDataBase() {
        userRepository.deleteAll();
    }

//     Заполнение БД для аутентификация
    public void createDataBase() {
        userService.createUser("user1", "password1");
        userService.createUser("user2", "password2");
        userService.createUser("user3", "password3");
    }

//    private static String getAuthenticationHeader(String username, String password) {
//
//        String encoding = Base64.getEncoder()
//                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
//        return "Basic " + encoding;
//    }

    @Test
    void testPostgresql() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            assertThat(conn).isNotNull();
        }
    }

    @DisplayName("Создание нового пользователя")
    @Test
    @WithMockUser(roles = "USER")
    void shouldCreateNewUser_Ok() throws Exception {

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNewUser().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("newUser")));
    }

    @DisplayName("Создание нового пользователя Админом")
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateNewUserByAdmin_Ok() throws Exception {

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNewUser().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("newUser")));
    }


    @DisplayName("Получение всех пользователей")
    @Test
    @WithMockUser(roles = "USER")
    void shouldGetAllUser_Ok() throws Exception {
        mockMvc.perform(get("/user/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        userRepository.saveAll(getUsersForTests());

        mockMvc.perform(get("/user/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @DisplayName("Получение всех пользователей Админом")
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldNotGetAllUser_NotForbiddenForAdmin() throws Exception {
        mockMvc.perform(get("/user/list"))
                .andExpect(status().isForbidden());
    }

    @DisplayName("Получение профиля пользователя")
    @Test
    @WithMockUser(roles = "USER")
    void shouldGetUserProfile_Ok() throws Exception {

        createDataBase();

        mockMvc.perform(get("/user/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"));
    }


    @DisplayName("Пользователь не прошел аутентификацию")
    @Test
    @WithMockUser(roles = "USER")
    void shouldNotGetUserProfile_Unauthorized() throws Exception {

        createDataBase();

        mockMvc.perform(get("/user/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user5", "password5")))
                .andExpect(status().is4xxClientError());
    }


}