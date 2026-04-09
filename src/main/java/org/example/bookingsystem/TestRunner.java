package org.example.bookingsystem;

import org.example.bookingsystem.entity.User;
import org.example.bookingsystem.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class TestRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TestRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        //создаем админа
        if (!userRepository.existsByLogin("admin")) {
            User admin = new User(
                    "admin",
                    passwordEncoder.encode("admin123"),
                    "Администраторов",
                    "Администратор",
                    null,
                    "+70000000001",
                    "admin@example.com"
            );
            admin.setRole("admin");
            userRepository.save(admin);
            System.out.println("Создан ADMIN: \nЛогин: admin\nПароль: admin123");
        }

//        // Создаём MANAGER
//        if (!userRepository.existsByLogin("manager")) {
//            User manager = new User(
//                    "manager",
//                    passwordEncoder.encode("manager123"),
//                    "Менеджер",
//                    "Пупкин",
//                    null,
//                    "+70000000002",
//                    "manager@example.com"
//            );
//            manager.setRole("manager");
//            userRepository.save(manager);
//            System.out.println("Создан MANAGER: \nЛогин: manager\n Пароль: manager123");
//        }
//
//        // Создаём обычного CLIENT
//        if (!userRepository.existsByLogin("client")) {
//            User client = new User(
//                    "client",
//                    passwordEncoder.encode("client123"),
//                    "Клиентов",
//                    "Клиент",
//                    null,
//                    "+70000000003",
//                    "client@example.com"
//            );
//            client.setRole("client");
//            userRepository.save(client);
//            System.out.println("Создан CLIENT: client");
//        }
    }
}