package org.kickmyb.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kickmyb.server.account.MUser;
import org.kickmyb.server.account.MUserRepository;
import org.kickmyb.server.task.ServiceTask;
import org.kickmyb.transfer.AddTaskRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.NoSuchElementException;


import java.util.Date;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO pour celui ci on aimerait pouvoir mocker l'utilisateur pour ne pas avoir à le créer

// https://reflectoring.io/spring-boot-mock/#:~:text=This%20is%20easily%20done%20by,our%20controller%20can%20use%20it.

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = KickMyBServerApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//@ActiveProfiles("test")
class ServiceTaskTests {

    @Autowired
    private MUserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ServiceTask serviceTask;

    @Test
    void testAddTask() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "Tâche de test";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        serviceTask.addOne(atr, u);

        assertEquals(1, serviceTask.home(u.id).size());
    }

    @Test
    void testAddTaskEmpty() {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        try {
            serviceTask.addOne(atr, u);
            fail("Aurait du lancer ServiceTask.Empty");
        } catch (Exception e) {
            assertEquals(ServiceTask.Empty.class, e.getClass());
        }
    }

    @Test
    void testAddTaskTooShort() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "o";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        try {
            serviceTask.addOne(atr, u);
            fail("Aurait du lancer ServiceTask.TooShort");
        } catch (Exception e) {
            assertEquals(ServiceTask.TooShort.class, e.getClass());
        }
    }

    @Test
    void testAddTaskExisting() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "Bonne tâche";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        try {
            serviceTask.addOne(atr, u);
            serviceTask.addOne(atr, u);
            fail("Aurait du lancer ServiceTask.Existing");
        } catch (Exception e) {
            assertEquals(ServiceTask.Existing.class, e.getClass());
        }
    }


    @Test
    void testSuppressionAvecIDCorrect() throws Exception {

        MUser user = new MUser();
        user.username = "Utilisateur1";
        user.password = passwordEncoder.encode("motdepasse");
        userRepository.saveAndFlush(user);


        AddTaskRequest req = new AddTaskRequest();
        req.name = "Tâche à supprimer";
        req.deadline = Date.from(new Date().toInstant().plusSeconds(3600));
        serviceTask.addOne(req, user);


        MUser reloadedUser = userRepository.findById(user.id).orElseThrow();


        assertEquals(1, serviceTask.home(reloadedUser.id).size());
        Long taskId = serviceTask.home(reloadedUser.id).get(0).id;


        serviceTask.deleteTask(taskId, reloadedUser);


        assertEquals(0, serviceTask.home(reloadedUser.id).size());
    }


    @Test
    void testSuppressionAvecIDInexistant() {

        MUser user = new MUser();
        user.username = "Utilisateur2";
        user.password = passwordEncoder.encode("motdepasse");
        userRepository.saveAndFlush(user);


        try {
            serviceTask.deleteTask(9999L, user);
            fail("Aurait dû lancer NoSuchElementException");
        } catch (NoSuchElementException e) {

        }
    }


    @Test
    void testControleAccesEchecSuppression() throws Exception {

        MUser alice = new MUser();
        alice.username = "Alice";
        alice.password = passwordEncoder.encode("passAlice");
        userRepository.saveAndFlush(alice);


        AddTaskRequest req = new AddTaskRequest();
        req.name = "Tâche d'Alice";
        req.deadline = Date.from(new Date().toInstant().plusSeconds(3600));
        serviceTask.addOne(req, alice);

        MUser reloadedAlice = userRepository.findById(alice.id).orElseThrow();
        Long taskId = serviceTask.home(reloadedAlice.id).get(0).id;

        MUser bob = new MUser();
        bob.username = "Bob";
        bob.password = passwordEncoder.encode("passBob");
        userRepository.saveAndFlush(bob);


        try {
            serviceTask.deleteTask(taskId, bob);
            fail("Aurait dû lancer SecurityException");
        } catch (SecurityException e) {

        }
    }


    
}
