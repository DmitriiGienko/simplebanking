package com.skypro.simplebanking.PreparingForTests;

import com.skypro.simplebanking.entity.User;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class ObjectsForTests {


    // создание нового пользователя
    public static JSONObject createNewUser() throws JSONException {
        JSONObject jsonUser = new JSONObject();
        jsonUser.put("id", "5");
        jsonUser.put("username", "newUser");
        jsonUser.put("password", "newPassword");
        return jsonUser;
    }

    // настройка аутентификатора
    public static String getAuthenticationHeader(String username, String password) {

        String encoding = Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoding;
    }


    public static List<User> getUsersForTests() {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("password1");

        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password2");

        User user3 = new User();
        user3.setUsername("user3");
        user3.setPassword("password3");
        User user4 = new User();

        user4.setUsername("user4");
        user4.setPassword("password4");

        return List.of(user1, user2, user3, user4);
    }


}
