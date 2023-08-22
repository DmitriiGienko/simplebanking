package com.skypro.simplebanking.PreparingForTests;

import com.skypro.simplebanking.dto.BankingUserDetails;
import com.skypro.simplebanking.entity.Account;
import com.skypro.simplebanking.entity.AccountCurrency;
import com.skypro.simplebanking.entity.User;
import com.skypro.simplebanking.repository.UserRepository;
import com.skypro.simplebanking.service.UserService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ObjectsForTests {

    private UserRepository userRepository;

    public ObjectsForTests(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
//        user1.setId(1L);
        user1.setUsername("user1");
        user1.setPassword("password1");
        User user2 = new User();
//        user1.setId(2L);
        user2.setUsername("user2");
        user2.setPassword("password2");
        User user3 = new User();
//        user1.setId(3L);
        user3.setUsername("user3");
        user3.setPassword("password3");
        User user4 = new User();
//        user1.setId(4L);
        user4.setUsername("user4");
        user4.setPassword("password4");

        return List.of(user1, user2, user3, user4);
    }

    public static List<Account> getAccountForTests() {
        Account account1 = new Account();
//        account1.setId(1L);
        account1.setAccountCurrency(AccountCurrency.EUR);
        account1.setAmount(1300L);
        account1.setUser(getUsersForTests().get(0));
        Account account2 = new Account();
//        account1.setId(1L);
        account2.setAccountCurrency(AccountCurrency.RUB);
        account2.setAmount(8000L);
        account2.setUser(getUsersForTests().get(1));

        Account account3 = new Account();
//        account1.setId(1L);
        account3.setAccountCurrency(AccountCurrency.USD);
        account3.setAmount(3200L);
        account3.setUser(getUsersForTests().get(2));

        return List.of(account1, account2, account3);
    }



}
