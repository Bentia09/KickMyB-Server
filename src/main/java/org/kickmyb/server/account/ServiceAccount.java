package org.kickmyb.server.account;

import org.kickmyb.transfer.SignupRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

// extends UserDetailsService which is one of the Spring Security entry points
public interface ServiceAccount extends UserDetailsService {

    class UsernameTooShort extends Exception {
 public UsernameTooShort() {
            super("Le nom d'utilisateur est trop court.");
        }
        
    }
    class UsernameAlreadyTaken extends Exception {
          public UsernameAlreadyTaken() {
            super("Ce nom d'utilisateur est déjà pris.");
        }
    }
    class PasswordTooShort extends Exception {
        public PasswordTooShort() {
            super("Le mot de passe est trop court.");
        }
    }
    void signup(SignupRequest req) throws BadCredentialsException, UsernameTooShort, PasswordTooShort, UsernameAlreadyTaken;

}
