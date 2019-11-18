package io.joshatron.bgt.server.logic;

import io.joshatron.bgt.server.logic.utils.AccountUtils;
import io.joshatron.bgt.server.logic.utils.User;
import io.joshatron.bgt.server.logic.utils.UserInfo;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AccountTest extends BaseTest {

    //Register User
    @Test(groups = {"parallel"})
    public void registerUser_OneUser_204UserCreated() throws Exception {
        String test = getTest();
        AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
    }

    @Test(groups = {"parallel"})
    public void registerUser_MultipleUsers_204UsersCreated() throws Exception {
        String test = getTest();
        AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.addUser(test, "03", "drowssap", client, HttpStatus.SC_NO_CONTENT);
    }

    @Test(groups = {"parallel"})
    public void registerUser_UserAlreadyCreated_403OnlyOneUserCreated() throws Exception {
        String test = getTest();
        AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_FORBIDDEN);
        AccountUtils.addUser(test, "01", "drowssap", client, HttpStatus.SC_FORBIDDEN);
    }

    @Test(groups = {"parallel"})
    public void registerUser_TryToRegisterWithDifferentCase_403OnlyOneUserCreated() throws Exception {
        String test = getTest();
        AccountUtils.addUser(test, "aa", "password", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.addUser(test, "AA", "password", client, HttpStatus.SC_FORBIDDEN);
    }

    @Test(groups = {"parallel"})
    public void registerUser_BlankUsername_400UserNotCreated() throws Exception {
        String test = getTest();
        AccountUtils.addUser("", "", "password", client, HttpStatus.SC_BAD_REQUEST);
        AccountUtils.addUser(null, null, "password", client, HttpStatus.SC_BAD_REQUEST);
    }

    @Test(groups = {"parallel"})
    public void registerUser_BlankPassword_400UserNotCreated() throws Exception {
        String test = getTest();
        AccountUtils.addUser(test, "01", "", client, HttpStatus.SC_BAD_REQUEST);
        AccountUtils.addUser(test, "02", null, client, HttpStatus.SC_BAD_REQUEST);
    }

    @Test(groups = {"parallel"})
    public void registerUser_ColonInUsername_400UserNotCreated() throws Exception {
        String test = getTest();
        AccountUtils.addUser(test, ":01", "password", client, HttpStatus.SC_BAD_REQUEST);
        AccountUtils.addUser(test, "01:", "password", client, HttpStatus.SC_BAD_REQUEST);
    }

    @Test(groups = {"parallel"})
    public void registerUser_NonAlphanumericUsername_400UserNotCreated() throws Exception {
        String test = getTest();
        AccountUtils.addUser(test, "01!", "password", client, HttpStatus.SC_BAD_REQUEST);
        AccountUtils.addUser(test, "01,", "password", client, HttpStatus.SC_BAD_REQUEST);
    }

    @Test(groups = {"parallel"})
    public void registerUser_ColonInPassword_204UsersCreated() throws Exception {
        String test = getTest();
        AccountUtils.addUser(test, "01", ":password", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.addUser(test, "02", "pass:word", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.addUser(test, "03", "password:", client, HttpStatus.SC_NO_CONTENT);
    }

    @Test(groups = {"parallel"})
    public void registerUser_CheckInitialRating_200Rating1000() throws Exception {
        String test = getTest();
        AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        UserInfo info = AccountUtils.seachUsers(test + "01", null, client, HttpStatus.SC_OK);
        Assert.assertEquals(info.getRating(), 1000);
    }

    //Change Username
    @Test(groups = {"parallel"})
    public void changeUsername_OneUser_204UsernameChanged() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.changeUsername(user, test + "02", client, HttpStatus.SC_NO_CONTENT);
    }

    @Test(groups = {"parallel"})
    public void changeUsername_WrongPassword_401UsernameNotChanged() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        user.setPassword("pass");
        AccountUtils.changeUsername(user, test + "02", client, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(groups = {"parallel"})
    public void changeUsername_InvalidUser_401UsernameNotChanged() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        user.setUsername(test + "02");
        AccountUtils.changeUsername(user, test + "03", client, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(groups = {"parallel"})
    public void changeUsername_BlankFields_400UsernameNotChanged() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.changeUsername(user, "", client, HttpStatus.SC_BAD_REQUEST);
        AccountUtils.changeUsername(user, null, client, HttpStatus.SC_BAD_REQUEST);
    }

    @Test(groups = {"parallel"})
    public void changeUsername_MultipleUsers_204UsernamesChanged() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "12345678", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.changeUsername(user1, test + "03", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.changePassword(user2, test + "04", client, HttpStatus.SC_NO_CONTENT);
    }

    @Test(groups = {"parallel"})
    public void changeUsername_OtherUserUsername_403UsernameNotChanged() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "12345678", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.changeUsername(user1, user2.getUsername(), client, HttpStatus.SC_FORBIDDEN);
    }

    //Change Password
    @Test(groups = {"parallel"})
    public void changePassword_OneUser_204PasswordChanged() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.changePassword(user, "drowssap", client, HttpStatus.SC_NO_CONTENT);
    }

    @Test(groups = {"parallel"})
    public void changePassword_WrongPassword_401PasswordNotChanged() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        user.setPassword("pass");
        AccountUtils.changePassword(user, "drowssap", client, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(groups = {"parallel"})
    public void changePassword_InvalidUser_401PasswordNotChanged() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        user.setUsername(test + "02");
        AccountUtils.changePassword(user, "drowssap", client, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(groups = {"parallel"})
    public void changePassword_BlankFields_400PasswordNotChanged() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.changePassword(user, "", client, HttpStatus.SC_BAD_REQUEST);
        AccountUtils.changePassword(user, null, client, HttpStatus.SC_BAD_REQUEST);
    }

    @Test(groups = {"parallel"})
    public void changePassword_MultipleUsers_204PasswordsChanged() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "12345678", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.changePassword(user1, "drowssap", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.changePassword(user2, "87654321", client, HttpStatus.SC_NO_CONTENT);
    }

    @Test(groups = {"parallel"})
    public void changePassword_OtherUserPassword_401PasswordsNotChanged() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "12345678", client, HttpStatus.SC_NO_CONTENT);
        user1.setPassword("12345678");
        AccountUtils.changePassword(user1, "drowssap", client, HttpStatus.SC_UNAUTHORIZED);
    }

    //Authenticate User
    @Test(groups = {"parallel"})
    public void authenticate_Valid_204() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.authenticate(user, client, HttpStatus.SC_NO_CONTENT);
    }

    @Test(groups = {"parallel"})
    public void authenticate_WrongPassword_401() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        user.setPassword("drowssap");
        AccountUtils.authenticate(user, client, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(groups = {"parallel"})
    public void authenticate_InvalidUser_401() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        user.setUsername(test + "02");
        AccountUtils.authenticate(user, client, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(groups = {"parallel"})
    public void authenticate_BlankFields_400() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User blankName = new User("", "password", "000000000000000");
        User blankPass = new User(user.getUsername(), "", "000000000000000");
        AccountUtils.authenticate(blankName, client, HttpStatus.SC_BAD_REQUEST);
        AccountUtils.authenticate(blankPass, client, HttpStatus.SC_BAD_REQUEST);
    }

    @Test(groups = {"parallel"})
    public void authenticate_lockedOut_403() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        user.setPassword("drowssap");
        for(int i = 0; i < AccountUtils.TRIES_TO_LOCK; i++) {
            AccountUtils.authenticate(user, client, HttpStatus.SC_UNAUTHORIZED);
        }
        AccountUtils.authenticate(user, client, HttpStatus.SC_FORBIDDEN, "LOCKED");
        user.setPassword("password");
        AccountUtils.authenticate(user, client, HttpStatus.SC_FORBIDDEN, "LOCKED");
    }

    //Search User
    @Test(groups = {"parallel"})
    public void searchUser_ExistingUserFromUsername_200ExpectedUser() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        UserInfo info = AccountUtils.seachUsers(user.getUsername(), null, client, HttpStatus.SC_OK);
        Assert.assertEquals(info.getUsername(), user.getUsername());
    }

    @Test(groups = {"parallel"})
    public void searchUser_ExistingUserFromUserId_200ExpectedUser() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        UserInfo info = AccountUtils.seachUsers(user.getUsername(), null, client, HttpStatus.SC_OK);
        UserInfo info2 = AccountUtils.seachUsers(null, info.getUserId(), client, HttpStatus.SC_OK);
        Assert.assertEquals(info2.getUsername(), user.getUsername());
    }

    @Test(groups = {"parallel"})
    public void searchUser_invalidUsername_404() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.seachUsers(test + "02", null, client, HttpStatus.SC_NOT_FOUND);
    }

    @Test(groups = {"parallel"})
    public void searchUser_invalidUserId_404() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.seachUsers(null, ZERO_ID, client, HttpStatus.SC_NOT_FOUND);
    }

    @Test(groups = {"parallel"})
    public void searchUser_invalidUserIdLength_400() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.seachUsers(null, "0000000", client, HttpStatus.SC_BAD_REQUEST);
    }

    @Test(groups = {"parallel"})
    public void searchUser_BlankUsername_400() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.seachUsers("", null, client, HttpStatus.SC_BAD_REQUEST);
    }

    @Test(groups = {"parallel"})
    public void searchUser_bothNull_400() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        AccountUtils.seachUsers(null, null, client, HttpStatus.SC_BAD_REQUEST);
    }

    @Test(groups = {"parallel"})
    public void searchUser_bothFilled_400() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        UserInfo info = AccountUtils.seachUsers(user.getUsername(), null, client, HttpStatus.SC_OK);
        AccountUtils.seachUsers(info.getUsername(), info.getUserId(), client, HttpStatus.SC_BAD_REQUEST);
    }
}
