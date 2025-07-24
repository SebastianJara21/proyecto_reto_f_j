package EduData.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import EduData.service.NlqServiceMejorado;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nlq")
@CrossOrigin(origins = "*")
public class NlqControlador {
    private final NlqServiceMejorado service;

    public NlqControlador(NlqServiceMejorado service){
        this.service = service;
    }

    @PostMapping(
            path= "/pregunta",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> consulta (@RequestBody String pregunta, HttpServletRequest request){
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null) {
            requestId = "REQ-" + System.currentTimeMillis();
        }
        
        System.out.println("=== [" + requestId + "] INICIO CONSULTA NLQ ===");
        System.out.println("Timestamp: " + new java.util.Date());
        System.out.println("Pregunta: " + pregunta);
        
        long startTime = System.currentTimeMillis();
        try {
            List<Map<String, Object>> result = service.answer(pregunta);
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("=== [" + requestId + "] CONSULTA COMPLETADA EN " + duration + "ms ===");
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            System.err.println("=== [" + requestId + "] CONSULTA FALLÓ EN " + duration + "ms ===");
            System.err.println("Error: " + e.getMessage());
            throw e;
        }
    }
    
    @GetMapping("/test")
    public Map<String, Object> test() {
        return Map.of(
                "status", "OK",
                "message", "El servicio NLQ mejorado está funcionando",
                "timestamp", System.currentTimeMillis(),
                "version", "2.0"
        );
    }
    
    @GetMapping("/test-api-key")
    public Map<String, Object> testApiKey() {
        try {
            // Hacer una consulta de prueba simple
            List<Map<String, Object>> result = service.answer("test");
            return Map.of(
                    "status", "OK",
                    "message", "API Key funciona correctamente",
                    "result", result
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "ERROR",
                    "message", "Error con API Key: " + e.getMessage()
            );
        }
    }
}