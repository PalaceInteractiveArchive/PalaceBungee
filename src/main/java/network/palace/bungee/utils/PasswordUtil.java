package network.palace.bungee.utils;

import network.palace.bungee.utils.passwords.BCrypt;

public class PasswordUtil {
    public String hashPassword(String password, String salt) {
        return BCrypt.hashpw(password, salt);
    }

    public boolean validPassword(String plain, String hashed) {
        return BCrypt.checkpw(plain, hashed);
    }

    public String getNewSalt() {
        return BCrypt.gensalt();
    }

    public boolean isStrongEnough(String plain) {
        return plain.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$") && plain.length() >= 8;
    }
}