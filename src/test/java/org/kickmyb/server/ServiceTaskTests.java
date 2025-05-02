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
    void testAddTaskEmpty()  {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        try{
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

        try{
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

        try{
            serviceTask.addOne(atr, u);
            serviceTask.addOne(atr, u);
            fail("Aurait du lancer ServiceTask.Existing");
        } catch (Exception e) {
            assertEquals(ServiceTask.Existing.class, e.getClass());
        }
    }



@Test 
void supressionIdCorrect() throws Exception {
    MUser u = new MUser();
    u.username = "M. Test";
    u.password = passwordEncoder.encode("Passw0rd!");
    userRepository.saveAndFlush(u);

    AddTaskRequest art = new AddTaskRequest(); 
    art.name = "Manger";
    art.deadline = Date.from(new Date().toInstant().plusSeconds(3600));
    serviceTask.addOne(art, u);

    MUser user = userRepository.findByUsername("M. Test").get();
    Long taskId = user.tasks.get(0).id;

    assertEquals(1, serviceTask.home(user.id).size());
    serviceTask.deleteTask(taskId, user);
    assertEquals(0, serviceTask.home(user.id).size());
}

@Test
void supressionIdIncorrect() {
    
    MUser u = new MUser();
    u.username = "M. Test";
    u.password = passwordEncoder.encode("Passw0rd!");
    userRepository.saveAndFlush(u);

    Long idInexistant = 999L;

    try {
        serviceTask.deleteTask(idInexistant, u);
        fail("La suppression à échouer pour un ID inexistant");
    } catch (Exception e) {
        
        assertEquals(NoSuchElementException.class, e.getClass());
    }
}

@Test
void suppressionNonAutorisee() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing {
  
    MUser alice = new MUser();
    alice.username = "alice";
    alice.password = passwordEncoder.encode("123456");
    userRepository.saveAndFlush(alice);

    
    AddTaskRequest req = new AddTaskRequest();
    req.name = "Tâche secrète";
    req.deadline = Date.from(new Date().toInstant().plusSeconds(3600));
    serviceTask.addOne(req, alice);

    
    alice = userRepository.findByUsername("alice").get();
    Long idTache = alice.tasks.get(0).id;

    
    MUser bob = new MUser();
    bob.username = "bob";
    bob.password = passwordEncoder.encode("abcdef");
    userRepository.saveAndFlush(bob);

   
    try {
        serviceTask.deleteTask(idTache, bob);
        fail("Bob n'aurait pas dû pouvoir supprimer la tâche d'Alice !");
    } catch (RuntimeException e) {
        assertEquals("Tâche non autorisée à être supprimée.", e.getMessage());
    }
}


    
}
