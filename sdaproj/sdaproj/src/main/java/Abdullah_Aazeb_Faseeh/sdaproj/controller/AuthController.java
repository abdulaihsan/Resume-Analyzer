package Abdullah_Aazeb_Faseeh.sdaproj.controller;

import Abdullah_Aazeb_Faseeh.sdaproj.application.User;
import Abdullah_Aazeb_Faseeh.sdaproj.persistence.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

// DESIGN PATTERN: Controller (MVC)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    // DESIGN PATTERN: Dependency Injection
    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // DESIGN PATTERN: Data Transfer Object (DTO)
    public record LoginRequest(String username, String password) {
    }

    public record RegisterRequest(String username, String password, String email, String role) {
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepository.findByUsername(req.username()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        User user = new User(req.username(), req.password(), req.role(), req.email());
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Optional<User> user = userRepository.findByUsername(req.username());

        if (user.isPresent() && user.get().getPassword().equals(req.password())) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }
}
