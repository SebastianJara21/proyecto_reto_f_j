package EduData.service;

import EduData.entity.Curso;
import EduData.entity.Docente;
import EduData.entity.Estudiante;
import EduData.entity.Matricula;
import EduData.repository.CursoRepositorio;
import EduData.repository.DocenteRepositorio;
import EduData.repository.EstudianteRepositorio;
import EduData.repository.MatriculaRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SampleDataService {

    private final EstudianteRepositorio estudianteRepositorio;
    private final DocenteRepositorio docenteRepositorio;
    private final CursoRepositorio cursoRepositorio;
    private final MatriculaRepositorio matriculaRepositorio;

    @Transactional
    public void inicializarDatosPrueba() {
        try {
            log.info("=== INICIANDO CREACIÓN DE DATOS DE PRUEBA ===");
            
            // 1. Crear docentes
            crearDocentes();
            
            // 2. Crear estudiantes
            crearEstudiantes();
            
            // 3. Crear cursos
            crearCursos();
            
            // 4. Crear matrículas
            crearMatriculas();
            
            log.info("=== DATOS DE PRUEBA CREADOS EXITOSAMENTE ===");
            
        } catch (Exception e) {
            log.error("Error al crear datos de prueba: ", e);
        }
    }

    private void crearDocentes() {
        if (docenteRepositorio.count() == 0) {
            log.info("Creando docentes de prueba...");
            
            Docente docente1 = new Docente();
            docente1.setNombre("Dr. María García");
            docente1.setEmail("maria.garcia@edu.com");
            docente1.setEspecialidad("Matemáticas");
            docenteRepositorio.save(docente1);
            
            Docente docente2 = new Docente();
            docente2.setNombre("Ing. Carlos López");
            docente2.setEmail("carlos.lopez@edu.com");
            docente2.setEspecialidad("Informática");
            docenteRepositorio.save(docente2);
            
            Docente docente3 = new Docente();
            docente3.setNombre("Lic. Ana Martínez");
            docente3.setEmail("ana.martinez@edu.com");
            docente3.setEspecialidad("Ciencias Naturales");
            docenteRepositorio.save(docente3);
            
            log.info("Docentes creados: {}", docenteRepositorio.count());
        }
    }

    private void crearEstudiantes() {
        if (estudianteRepositorio.count() == 0) {
            log.info("Creando estudiantes de prueba...");
            
            // Estudiante 1
            Estudiante est1 = new Estudiante();
            est1.setIdentificacion("12345678");
            est1.setNombre("Juan");
            est1.setApellido("Pérez");
            est1.setCorreo("juan.perez@estudiante.com");
            est1.setFechaNacimiento(LocalDate.of(2000, 5, 15));
            est1.setGenero("Masculino");
            est1.setTelefono("300-123-4567");
            est1.setDireccion("Calle 123 #45-67");
            est1.setMatriculaAnio(2024);
            est1.setEstado("Activo");
            est1.setNivel("Universitario");
            est1.setGrupo("A");
            estudianteRepositorio.save(est1);
            
            // Estudiante 2
            Estudiante est2 = new Estudiante();
            est2.setIdentificacion("87654321");
            est2.setNombre("María");
            est2.setApellido("González");
            est2.setCorreo("maria.gonzalez@estudiante.com");
            est2.setFechaNacimiento(LocalDate.of(1999, 8, 22));
            est2.setGenero("Femenino");
            est2.setTelefono("300-987-6543");
            est2.setDireccion("Avenida 456 #78-90");
            est2.setMatriculaAnio(2024);
            est2.setEstado("Activo");
            est2.setNivel("Universitario");
            est2.setGrupo("B");
            estudianteRepositorio.save(est2);
            
            // Estudiante 3
            Estudiante est3 = new Estudiante();
            est3.setIdentificacion("11111111");
            est3.setNombre("Carlos");
            est3.setApellido("Rodríguez");
            est3.setCorreo("carlos.rodriguez@estudiante.com");
            est3.setFechaNacimiento(LocalDate.of(2001, 2, 10));
            est3.setGenero("Masculino");
            est3.setTelefono("300-111-2222");
            est3.setDireccion("Carrera 789 #12-34");
            est3.setMatriculaAnio(2024);
            est3.setEstado("Activo");
            est3.setNivel("Universitario");
            est3.setGrupo("A");
            estudianteRepositorio.save(est3);
            
            // Estudiante 4
            Estudiante est4 = new Estudiante();
            est4.setIdentificacion("22222222");
            est4.setNombre("Sebastian");
            est4.setApellido("Figueroa");
            est4.setCorreo("sebastian.figueroa@estudiante.com");
            est4.setFechaNacimiento(LocalDate.of(2000, 11, 3));
            est4.setGenero("Masculino");
            est4.setTelefono("300-333-4444");
            est4.setDireccion("Diagonal 555 #66-77");
            est4.setMatriculaAnio(2024);
            est4.setEstado("Activo");
            est4.setNivel("Universitario");
            est4.setGrupo("B");
            estudianteRepositorio.save(est4);
            
            log.info("Estudiantes creados: {}", estudianteRepositorio.count());
        }
    }

    private void crearCursos() {
        if (cursoRepositorio.count() == 0) {
            log.info("Creando cursos de prueba...");
            
            List<Docente> docentes = docenteRepositorio.findAll();
            if (docentes.isEmpty()) {
                log.error("No hay docentes para asignar a los cursos");
                return;
            }
            
            // Curso de Matemáticas
            Curso curso1 = new Curso();
            curso1.setNombre("Matemáticas Básicas");
            curso1.setCodigo("MAT001");
            curso1.setDescripcion("Curso introductorio de matemáticas");
            curso1.setAnio(2024);
            curso1.setDocente(docentes.get(0)); // Dr. María García
            cursoRepositorio.save(curso1);
            
            // Curso de Matemáticas Avanzadas
            Curso curso2 = new Curso();
            curso2.setNombre("Matemáticas Avanzadas");
            curso2.setCodigo("MAT002");
            curso2.setDescripcion("Curso avanzado de matemáticas");
            curso2.setAnio(2024);
            curso2.setDocente(docentes.get(0)); // Dr. María García
            cursoRepositorio.save(curso2);
            
            // Curso de Informática
            Curso curso3 = new Curso();
            curso3.setNombre("Programación I");
            curso3.setCodigo("INF001");
            curso3.setDescripcion("Introducción a la programación");
            curso3.setAnio(2024);
            if (docentes.size() > 1) {
                curso3.setDocente(docentes.get(1)); // Ing. Carlos López
            }
            cursoRepositorio.save(curso3);
            
            // Curso de Ciencias
            Curso curso4 = new Curso();
            curso4.setNombre("Física General");
            curso4.setCodigo("FIS001");
            curso4.setDescripcion("Conceptos básicos de física");
            curso4.setAnio(2024);
            if (docentes.size() > 2) {
                curso4.setDocente(docentes.get(2)); // Lic. Ana Martínez
            }
            cursoRepositorio.save(curso4);
            
            log.info("Cursos creados: {}", cursoRepositorio.count());
        }
    }

    private void crearMatriculas() {
        if (matriculaRepositorio.count() == 0) {
            log.info("Creando matrículas de prueba...");
            
            List<Estudiante> estudiantes = estudianteRepositorio.findAll();
            List<Curso> cursos = cursoRepositorio.findAll();
            
            if (estudiantes.isEmpty() || cursos.isEmpty()) {
                log.error("No hay estudiantes o cursos para crear matrículas");
                return;
            }
            
            // Matricular estudiantes en cursos
            for (int i = 0; i < estudiantes.size(); i++) {
                Estudiante estudiante = estudiantes.get(i);
                
                // Cada estudiante se matricula en 2-3 cursos
                for (int j = 0; j < Math.min(3, cursos.size()); j++) {
                    // Evitar duplicados usando módulo
                    int cursoIndex = (i + j) % cursos.size();
                    Curso curso = cursos.get(cursoIndex);
                    
                    Matricula matricula = new Matricula();
                    matricula.setAnio(2024);
                    matricula.setEstudiante(estudiante);
                    matricula.setCurso(curso);
                    matriculaRepositorio.save(matricula);
                }
            }
            
            log.info("Matrículas creadas: {}", matriculaRepositorio.count());
        }
    }

    public void mostrarEstadisticas() {
        log.info("=== ESTADÍSTICAS DE DATOS ===");
        log.info("Estudiantes: {}", estudianteRepositorio.count());
        log.info("Docentes: {}", docenteRepositorio.count());
        log.info("Cursos: {}", cursoRepositorio.count());
        log.info("Matrículas: {}", matriculaRepositorio.count());
        log.info("============================");
    }
}
