package EduData.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NlqServiceMejorado {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${server.url:https://edudata-backend.onrender.com}")
    private String serverUrl;

    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;

    public NlqServiceMejorado(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.objectMapper = new ObjectMapper();
    }

    public List<Map<String, Object>> answer(String pregunta) {
        System.out.println("=== INICIANDO CONSULTA NLQ MEJORADA ===");
        System.out.println("Pregunta recibida: " + pregunta);
        
        // Detectar si la pregunta necesita parámetros y extraerlos
        Map<String, String> parametros = extraerParametros(pregunta);
        System.out.println("Parámetros extraídos: " + parametros);
        
        String promptTemplate = """
            Eres un asistente experto que transforma preguntas en lenguaje natural a JPQL.
            
            ENTIDADES DISPONIBLES:
            
            Estudiante: id, identificacion, nombre, apellido, correo, fechaNacimiento, genero, telefono, direccion, matriculaAnio, estado, nivel, grupo
            
            Docente: id, nombre, email, especialidad
            
            Curso: id, nombre, codigo, descripcion, anio, docente (relación con Docente)
            
            Matricula: id, anio, estudiante (relación con Estudiante), curso (relación con Curso)
            
            REGLAS IMPORTANTES:
            1. USA SIEMPRE valores literales, NUNCA parámetros como :nombre
            2. Para strings, usa comillas simples: 'valor'
            3. Para LIKE, usa: LIKE '%texto%' 
            4. Para números, usa valores directos: 2024
            5. Para fechas, usa formato: '2024-01-01'
            6. Si necesitas mostrar datos relacionados, usa JOIN
            
            EJEMPLOS DE CONSULTAS CORRECTAS:
            - "cuántos estudiantes hay" → SELECT COUNT(e) FROM Estudiante e
            - "estudiantes del 2024" → SELECT e FROM Estudiante e WHERE e.matriculaAnio = 2024
            - "estudiantes masculinos" → SELECT e FROM Estudiante e WHERE e.genero = 'Masculino'
            - "cursos de matemáticas" → SELECT c FROM Curso c LEFT JOIN FETCH c.docente WHERE LOWER(c.nombre) LIKE '%matemática%' OR LOWER(c.nombre) LIKE '%matemáticas%' OR LOWER(c.nombre) LIKE '%math%'
            - "cursos de matematicas" → SELECT c FROM Curso c LEFT JOIN FETCH c.docente WHERE LOWER(c.nombre) LIKE '%matemática%' OR LOWER(c.nombre) LIKE '%matemáticas%' OR LOWER(c.nombre) LIKE '%math%'
            - "todos los cursos" → SELECT c FROM Curso c LEFT JOIN FETCH c.docente
            - "que cursos hay" → SELECT c FROM Curso c LEFT JOIN FETCH c.docente
            - "cuantos cursos hay" → SELECT COUNT(c) FROM Curso c
            - "estudiantes llamados juan" → SELECT e FROM Estudiante e WHERE LOWER(e.nombre) LIKE '%juan%'
            - "docentes de informática" → SELECT d FROM Docente d WHERE LOWER(d.especialidad) LIKE '%informática%'
            - "notas de sebastian" → SELECT e FROM Estudiante e WHERE LOWER(e.nombre) LIKE '%sebastian%'
            - "datos de sebastian figueroa" → SELECT e FROM Estudiante e WHERE LOWER(e.nombre) LIKE '%sebastian%' AND LOWER(e.apellido) LIKE '%figueroa%'
            - "matriculas del 2024" → SELECT m FROM Matricula m LEFT JOIN FETCH m.estudiante LEFT JOIN FETCH m.curso WHERE m.anio = 2024
            - "todas las matriculas" → SELECT m FROM Matricula m LEFT JOIN FETCH m.estudiante LEFT JOIN FETCH m.curso
            - "cursos con docente" → SELECT c FROM Curso c LEFT JOIN FETCH c.docente WHERE c.docente IS NOT NULL
            - "matriculas de estudiantes del 2024" → SELECT m FROM Matricula m LEFT JOIN FETCH m.estudiante e LEFT JOIN FETCH m.curso WHERE e.matriculaAnio = 2024
            - "cursos que tiene sebastian" → SELECT c FROM Matricula m JOIN m.curso c LEFT JOIN FETCH c.docente JOIN m.estudiante e WHERE LOWER(e.nombre) LIKE '%sebastian%'
            - "estudiantes en cursos de matemáticas" → SELECT DISTINCT e FROM Matricula m JOIN m.estudiante e JOIN m.curso c WHERE LOWER(c.nombre) LIKE '%matemática%' OR LOWER(c.nombre) LIKE '%matemáticas%' OR LOWER(c.nombre) LIKE '%math%'
            - "estudiantes que están en cursos" → SELECT DISTINCT e FROM Matricula m JOIN m.estudiante e
            - "qué estudiantes están en cursos" → SELECT DISTINCT e FROM Matricula m JOIN m.estudiante e
            - "estudiantes matriculados" → SELECT DISTINCT e FROM Matricula m JOIN m.estudiante e
            - "mostrar estudiantes y sus cursos" → SELECT e, c FROM Matricula m JOIN m.estudiante e JOIN m.curso c
            - "que curso tiene cada estudiante" → SELECT e, c FROM Matricula m JOIN m.estudiante e JOIN m.curso c
            - "muestrame en que curso esta cada estudiante" → SELECT e, c FROM Matricula m JOIN m.estudiante e JOIN m.curso c
            - "estudiantes con cursos" → SELECT e, c FROM Matricula m JOIN m.estudiante e JOIN m.curso c
            - "relacion estudiante curso" → SELECT e, c FROM Matricula m JOIN m.estudiante e JOIN m.curso c
            - "datos completos de sebastian" → SELECT e FROM Estudiante e WHERE LOWER(e.nombre) LIKE '%sebastian%'
            - "ver la nota de sebastian" → SELECT m FROM Matricula m JOIN m.estudiante e WHERE e.nombre LIKE '%Sebastian%'
            - "informacion de estudiante sebastian" → SELECT e FROM Estudiante e WHERE e.nombre LIKE '%Sebastian%'
            
            IMPORTANTE: Devuelve SOLO la consulta JPQL, sin explicaciones.
            
            Pregunta: """;
        
        String prompt = promptTemplate + pregunta;
        
        System.out.println("Prompt generado correctamente");

        try {
            System.out.println("=== LLAMANDO A OPENROUTER ===");
            System.out.println("Server URL configurada: " + serverUrl);
            System.out.println("API Key verificación - Existe: " + (apiKey != null));
            System.out.println("API Key verificación - No vacía: " + (apiKey != null && !apiKey.trim().isEmpty()));
            
            // Debug más detallado de la API key
            if (apiKey != null) {
                System.out.println("API Key longitud: " + apiKey.length());
                System.out.println("API Key primeros 10 caracteres: " + (apiKey.length() > 10 ? apiKey.substring(0, 10) + "..." : apiKey));
            }
            
            if (apiKey == null || apiKey.trim().isEmpty()) {
                System.err.println("ERROR: API Key no configurada");
                System.err.println("Valor actual de apiKey: '" + apiKey + "'");
                return List.of(Map.of("error", "API Key de OpenRouter no configurada. Verifica application.properties"));
            }

            // Limpiar la API key de posibles espacios o caracteres extraños
            String cleanApiKey = apiKey.trim();
            System.out.println("API Key limpia longitud: " + cleanApiKey.length());

            WebClient client = WebClient.builder()
                    .baseUrl("https://openrouter.ai")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + cleanApiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader("HTTP-Referer", serverUrl) // Referer dinámico según entorno
                    .defaultHeader("X-Title", "EduData") // Opcional pero recomendado
                    .build();

            String escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
            
            String requestBody = "{" +
                    "\"model\": \"openai/gpt-4o\"," +
                    "\"messages\": [" +
                    "{\"role\": \"user\", \"content\": \"" + escapedPrompt + "\"}" +
                    "]," +
                    "\"max_tokens\": 1000," +
                    "\"temperature\": 0.1" +
                    "}";

            System.out.println("=== DEBUG REQUEST ===");
            System.out.println("Authorization header: Bearer " + (cleanApiKey.length() > 10 ? cleanApiKey.substring(0, 10) + "..." : cleanApiKey));
            System.out.println("Request URL: https://openrouter.ai/api/v1/chat/completions");
            System.out.println("HTTP-Referer: " + serverUrl);

            String responseJson = client.post()
                    .uri("/api/v1/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> {
                                System.err.println("=== ERROR RESPONSE STATUS ===");
                                System.err.println("Status: " + response.statusCode());
                                System.err.println("Headers: " + response.headers().asHttpHeaders());
                                
                                return response.bodyToMono(String.class)
                                        .doOnNext(errorBody -> {
                                            System.err.println("=== ERROR RESPONSE BODY ===");
                                            System.err.println(errorBody);
                                        })
                                        .flatMap(errorBody -> Mono.error(new RuntimeException("Error al llamar a OpenRouter: " + errorBody)));
                            })
                    .bodyToMono(String.class)
                    .block();

            System.out.println("=== RESPUESTA DE OPENROUTER RECIBIDA ===");

            @SuppressWarnings("unchecked")
            Map<String, Object> response = (Map<String, Object>) objectMapper.readValue(responseJson, Map.class);
            @SuppressWarnings("unchecked")
            List<Map<?, ?>> choices = (List<Map<?, ?>>) response.get("choices");
            Map<?, ?> choice = choices.get(0);
            Map<?, ?> message = (Map<?, ?>) choice.get("message");
            String jpql = message.get("content").toString().trim();

            // Limpiar la respuesta de posibles marcadores de código
            jpql = jpql.replaceAll("```sql", "").replaceAll("```jpql", "").replaceAll("```", "").trim();

            System.out.println("=== JPQL GENERADO POR LA IA ===");
            System.out.println("JPQL: " + jpql);

            // Validar JPQL antes de ejecutar
            if (jpql.contains(":") && jpql.matches(".*:[a-zA-Z][a-zA-Z0-9]*.*")) {
                System.err.println("ERROR: La consulta contiene parámetros nombrados sin resolver");
                return List.of(Map.of(
                        "error", "La consulta generada contiene parámetros no resueltos.",
                        "jpql", jpql,
                        "sugerencia", "Intenta ser más específico. Ejemplo: 'estudiantes del año 2024' en lugar de 'estudiantes de un año específico'"
                ));
            }

            // Ejecutar JPQL
            return ejecutarConsulta(jpql);

        } catch (Exception e) {
            System.err.println("=== ERROR GENERAL ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            return List.of(Map.of(
                    "error", "Error en el servicio: " + e.getMessage(),
                    "sugerencia", "Intenta con preguntas más simples como '¿cuántos estudiantes hay?' o 'estudiantes del 2024'"
            ));
        }
    }

    private List<Map<String, Object>> ejecutarConsulta(String jpql) {
        try {
            System.out.println("=== EJECUTANDO JPQL ===");
            Query query = entityManager.createQuery(jpql);
            List<?> resultados = query.getResultList();
            
            System.out.println("Resultados obtenidos: " + resultados.size() + " registros");
            
            // Convertir resultados a Maps para evitar problemas de serialización
            List<Map<String, Object>> resultadosConvertidos = new ArrayList<>();
            
            for (Object resultado : resultados) {
                Map<String, Object> item = convertirResultado(resultado);
                resultadosConvertidos.add(item);
            }
            
            return resultadosConvertidos;
            
        } catch (Exception e) {
            System.err.println("=== ERROR EJECUTANDO JPQL ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            // Proporcionar mensajes de error más específicos
            String mensajeError = e.getMessage();
            String sugerencia = "Verifica que la consulta sea válida.";
            
            if (mensajeError.contains("could not resolve property")) {
                sugerencia = "La propiedad especificada no existe en la entidad. Verifica los nombres de los campos.";
            } else if (mensajeError.contains("No data found")) {
                sugerencia = "No se encontraron datos que coincidan con tu consulta. Prueba inicializando datos de prueba primero.";
            } else if (mensajeError.contains("unexpected token")) {
                sugerencia = "Error de sintaxis en la consulta. Intenta reformular tu pregunta.";
            }
            
            return List.of(Map.of(
                    "error", "Error ejecutando consulta: " + mensajeError,
                    "jpql", jpql,
                    "sugerencia", sugerencia,
                    "ayuda", "Si no tienes datos, usa el botón 'Inicializar Datos de Prueba' para crear información de ejemplo."
            ));
        }
    }

    private Map<String, Object> convertirResultado(Object resultado) {
        Map<String, Object> item = new HashMap<>();
        
        if (resultado instanceof Number) {
            // Para consultas COUNT, SUM, etc.
            return Map.of("resultado", resultado);
        } else if (resultado instanceof String) {
            // Para consultas SELECT campo
            return Map.of("valor", resultado);
        } else if (resultado instanceof Object[]) {
            // Para consultas que devuelven múltiples campos como SELECT e.nombre, c.nombre
            Object[] array = (Object[]) resultado;
            Map<String, Object> fila = new HashMap<>();
            
            // Mapear los valores del array a nombres descriptivos
            for (int i = 0; i < array.length; i++) {
                if (array[i] != null) {
                    // Intentar determinar el tipo de dato y asignar nombre apropiado
                    if (array[i].getClass().getSimpleName().equals("Estudiante")) {
                        var estudiante = array[i];
                        fila.put("estudiante_id", getFieldValue(estudiante, "id"));
                        fila.put("estudiante_nombre", getFieldValue(estudiante, "nombre"));
                        fila.put("estudiante_apellido", getFieldValue(estudiante, "apellido"));
                        fila.put("estudiante_correo", getFieldValue(estudiante, "correo"));
                        fila.put("estudiante_identificacion", getFieldValue(estudiante, "identificacion"));
                    } else if (array[i].getClass().getSimpleName().equals("Curso")) {
                        var curso = array[i];
                        fila.put("curso_id", getFieldValue(curso, "id"));
                        fila.put("curso_nombre", getFieldValue(curso, "nombre"));
                        fila.put("curso_codigo", getFieldValue(curso, "codigo"));
                        fila.put("curso_descripcion", getFieldValue(curso, "descripcion"));
                    } else if (array[i].getClass().getSimpleName().equals("Matricula")) {
                        var matricula = array[i];
                        fila.put("matricula_id", getFieldValue(matricula, "id"));
                        fila.put("matricula_anio", getFieldValue(matricula, "anio"));
                    } else {
                        // Para otros tipos de datos
                        fila.put("campo_" + i, array[i].toString());
                    }
                } else {
                    fila.put("campo_" + i, "N/A");
                }
            }
            return fila;
        } else if (resultado.getClass().getSimpleName().equals("Estudiante")) {
            // Convertir Estudiante a Map
            try {
                var estudiante = resultado;
                item.put("id", getFieldValue(estudiante, "id"));
                item.put("identificacion", getFieldValue(estudiante, "identificacion"));
                item.put("nombre", getFieldValue(estudiante, "nombre"));
                item.put("apellido", getFieldValue(estudiante, "apellido"));
                item.put("correo", getFieldValue(estudiante, "correo"));
                item.put("genero", getFieldValue(estudiante, "genero"));
                item.put("matriculaAnio", getFieldValue(estudiante, "matriculaAnio"));
                item.put("estado", getFieldValue(estudiante, "estado"));
            } catch (Exception e) {
                item.put("error", "Error convirtiendo estudiante: " + e.getMessage());
            }
        } else if (resultado.getClass().getSimpleName().equals("Docente")) {
            // Convertir Docente a Map
            try {
                var docente = resultado;
                item.put("id", getFieldValue(docente, "id"));
                item.put("nombre", getFieldValue(docente, "nombre"));
                item.put("email", getFieldValue(docente, "email"));
                item.put("especialidad", getFieldValue(docente, "especialidad"));
            } catch (Exception e) {
                item.put("error", "Error convirtiendo docente: " + e.getMessage());
            }
        } else if (resultado.getClass().getSimpleName().equals("Curso")) {
            // Convertir Curso a Map
            try {
                var curso = resultado;
                item.put("id", getFieldValue(curso, "id"));
                item.put("nombre", getFieldValue(curso, "nombre"));
                item.put("codigo", getFieldValue(curso, "codigo"));
                item.put("descripcion", getFieldValue(curso, "descripcion"));
                item.put("anio", getFieldValue(curso, "anio"));
                
                // Manejar la relación con docente de forma más segura
                try {
                    Object docente = getFieldValue(curso, "docente");
                    if (docente != null && !docente.toString().equals("N/A") && !docente.toString().contains("Relación no cargada")) {
                        Object nombreDocente = getFieldValue(docente, "nombre");
                        Object emailDocente = getFieldValue(docente, "email");
                        if (nombreDocente != null && !nombreDocente.equals("N/A")) {
                            item.put("docente", nombreDocente);
                        }
                        if (emailDocente != null && !emailDocente.equals("N/A")) {
                            item.put("docenteEmail", emailDocente);
                        }
                    } else {
                        item.put("docente", "Sin asignar");
                    }
                } catch (Exception docenteException) {
                    item.put("docente", "Error al cargar docente");
                }
                
            } catch (Exception e) {
                item.put("error", "Error convirtiendo curso: " + e.getMessage());
            }
        } else if (resultado.getClass().getSimpleName().equals("Matricula")) {
            // Convertir Matricula a Map
            try {
                var matricula = resultado;
                item.put("id", getFieldValue(matricula, "id"));
                item.put("anio", getFieldValue(matricula, "anio"));
                
                // Manejar las relaciones de forma más segura
                try {
                    Object estudiante = getFieldValue(matricula, "estudiante");
                    if (estudiante != null && !estudiante.toString().equals("N/A") && !estudiante.toString().contains("Relación no cargada")) {
                        Object nombreEst = getFieldValue(estudiante, "nombre");
                        Object apellidoEst = getFieldValue(estudiante, "apellido");
                        Object idEst = getFieldValue(estudiante, "id");
                        
                        if (nombreEst != null && apellidoEst != null && !nombreEst.equals("N/A") && !apellidoEst.equals("N/A")) {
                            item.put("estudiante", nombreEst + " " + apellidoEst);
                        }
                        if (idEst != null && !idEst.equals("N/A")) {
                            item.put("estudianteId", idEst);
                        }
                    }
                } catch (Exception estException) {
                    item.put("estudiante", "Error al cargar estudiante");
                }
                
                try {
                    Object curso = getFieldValue(matricula, "curso");
                    if (curso != null && !curso.toString().equals("N/A") && !curso.toString().contains("Relación no cargada")) {
                        Object nombreCurso = getFieldValue(curso, "nombre");
                        Object idCurso = getFieldValue(curso, "id");
                        
                        if (nombreCurso != null && !nombreCurso.equals("N/A")) {
                            item.put("curso", nombreCurso);
                        }
                        if (idCurso != null && !idCurso.equals("N/A")) {
                            item.put("cursoId", idCurso);
                        }
                    }
                } catch (Exception cursoException) {
                    item.put("curso", "Error al cargar curso");
                }
                
            } catch (Exception e) {
                item.put("error", "Error convirtiendo matricula: " + e.getMessage());
            }
        } else {
            // Para otros tipos de resultados
            item.put("valor", resultado.toString());
            item.put("tipo", resultado.getClass().getSimpleName());
        }
        
        return item;
    }

    private Object getFieldValue(Object obj, String fieldName) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(obj);
            
            // Verificar si es una proxy de Hibernate no inicializada
            if (value != null && value.getClass().getName().contains("HibernateProxy")) {
                try {
                    // Intentar acceder a una propiedad simple para forzar la inicialización
                    value.toString();
                } catch (Exception hibernateException) {
                    // Si falla la inicialización lazy, devolver información básica
                    return "N/A (Relación no cargada)";
                }
            }
            
            return value;
        } catch (Exception e) {
            return "N/A";
        }
    }

    private Map<String, String> extraerParametros(String pregunta) {
        Map<String, String> parametros = new HashMap<>();
        
        // Patrones para extraer información común
        Pattern nombrePattern = Pattern.compile("(llamado|nombre|se llama)\\s+([a-zA-ZáéíóúÁÉÍÓÚñÑ]+)", Pattern.CASE_INSENSITIVE);
        Pattern anioPattern = Pattern.compile("(año|del)\\s+(\\d{4})", Pattern.CASE_INSENSITIVE);
        Pattern generoPattern = Pattern.compile("(masculino|femenino|hombre|mujer)", Pattern.CASE_INSENSITIVE);
        
        Matcher nombreMatcher = nombrePattern.matcher(pregunta);
        if (nombreMatcher.find()) {
            parametros.put("nombre", nombreMatcher.group(2));
        }
        
        Matcher anioMatcher = anioPattern.matcher(pregunta);
        if (anioMatcher.find()) {
            parametros.put("anio", anioMatcher.group(2));
        }
        
        Matcher generoMatcher = generoPattern.matcher(pregunta);
        if (generoMatcher.find()) {
            String genero = generoMatcher.group(1).toLowerCase();
            if (genero.contains("masculino") || genero.contains("hombre")) {
                parametros.put("genero", "Masculino");
            } else if (genero.contains("femenino") || genero.contains("mujer")) {
                parametros.put("genero", "Femenino");
            }
        }
        
        return parametros;
    }
}
