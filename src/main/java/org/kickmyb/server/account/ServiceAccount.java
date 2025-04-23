package org.kickmyb.server.account;

import org.kickmyb.transfer.SignupRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

// extends UserDetailsService which is one of the Spring Security entry points
public interface ServiceAccount extends UserDetailsService {

  public class UsernameTooShort extends Exception {
    public UsernameTooShort(String lang) {
        super(lang != null && lang.startsWith("en")
            ? "Username is too short."
            : "Le nom d'utilisateur est trop court.");
    }
}

  public class UsernameAlreadyTaken extends Exception {
    public UsernameAlreadyTaken(String lang) {
        super(lang != null && lang.startsWith("en")
            ? "This username is already taken."
            : "Ce nom d'utilisateur est déjà pris.");
    }
}

   public class PasswordTooShort extends Exception {
    public PasswordTooShort(String lang) {
        super(lang != null && lang.startsWith("en")
            ? "Password is too short."
            : "Le mot de passe est trop court.");
    }
}

 void signup(SignupRequest req, String lang) throws BadCredentialsException, UsernameTooShort, PasswordTooShort, UsernameAlreadyTaken;

}
