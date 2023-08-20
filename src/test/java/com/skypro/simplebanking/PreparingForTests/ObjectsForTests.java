package com.skypro.simplebanking.PreparingForTests;

import com.skypro.simplebanking.entity.User;
import org.json.JSONException;
import org.json.JSONObject;

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

//    public static List<JSONObject> createListOfUsers() throws JSONException {
//        JSONObject jsonUser1 = new JSONObject();
//        jsonUser1.put("id", "1");
//        jsonUser1.put("username", "user1");
//        jsonUser1.put("password", "password1");
//        JSONObject jsonUser2 = new JSONObject();
//        jsonUser1.put("id", "2");
//        jsonUser1.put("username", "user2");
//        jsonUser1.put("password", "password2");
//        JSONObject jsonUser3 = new JSONObject();
//        jsonUser1.put("id", "3");
//        jsonUser1.put("username", "user3");
//        jsonUser1.put("password", "password3");
//        return List.of(jsonUser1, jsonUser2, jsonUser3);
//    }

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
        return List.of(user1, user2, user3);

    }


}
