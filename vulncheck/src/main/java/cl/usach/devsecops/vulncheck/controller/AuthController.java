package cl.usach.devsecops.vulncheck.controller;

import cl.usach.devsecops.vulncheck.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Credenciales invalidas");
            return ResponseEntity.status(401).body(error);
        }

        String token = jwtUtil.generateToken(username);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("token", token);
        response.put("tipo", "Bearer");
        response.put("usuario", username);
        return ResponseEntity.ok(response);
    }
}
