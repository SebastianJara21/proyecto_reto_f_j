import React, { useState, useEffect } from "react";
import "./Dashboard.css";
import { Link } from "react-router-dom";
import busquedaService from "../services/busquedaService";
import { authService } from "../services/authService";

export default function Dashboard() {
    const [pregunta, setPregunta] = useState('');
    const [resultados, setResultados] = useState([]);
    const [jpqlGenerado, setJpqlGenerado] = useState('');
    const [cargando, setCargando] = useState(false);
    const [error, setError] = useState('');

    // Cargar estadísticas al montar el componente
    useEffect(() => {
        cargarEstadisticas();
    }, []);

    const cargarEstadisticas = async () => {
        try {
            await busquedaService.obtenerEstadisticas();
        } catch (error) {
            console.error('Error cargando estadísticas:', error);
        }
    };

    const ejemplosPregunta = [
        "¿Cuántos estudiantes hay en total?",
        "Estudiantes del año 2024",
        "Estudiantes de género masculino",
        "Cursos de matemáticas"
    ];

    const realizarBusqueda = async () => {
        if (!pregunta.trim()) {
            setError('Por favor ingresa una pregunta');
            return;
        }

        setCargando(true);
        setError('');
        setResultados([]);
        setJpqlGenerado('');

        try {
            const data = await busquedaService.consultarNLQ(pregunta);

            // Si hay error en la respuesta
            if (data && data.length > 0 && data[0].error) {
                setError(data[0].error);
                if (data[0].jpql) {
                    setJpqlGenerado(data[0].jpql);
                }
            } else {
                setResultados(data);
            }
        } catch (err) {
            setError(err.message);
        } finally {
            setCargando(false);
        }
    };

    const usarEjemplo = (ejemplo) => {
        setPregunta(ejemplo);
    };

    const handleLogout = () => {
        authService.logout();
    };

    const currentUser = authService.getCurrentUser();

    const renderResultados = () => {
        if (resultados.length === 0) return null;

        // Si es un conteo simple o resultado numérico
        if (resultados.length === 1) {
            const resultado = resultados[0];
            
            // Verificar si es un resultado numérico
            if (resultado.resultado !== undefined) {
                return (
                    <div className="resultado-conteo-dashboard">
                        <h3>Resultado:</h3>
                        <div className="numero-resultado-dashboard">{resultado.resultado}</div>
                    </div>
                );
            }
            
            // Verificar si es un valor simple
            if (resultado.valor !== undefined) {
                return (
                    <div className="resultado-simple-dashboard">
                        <h3>Resultado:</h3>
                        <div className="valor-resultado-dashboard">{resultado.valor}</div>
                    </div>
                );
            }
        }

        // Si son objetos de estudiantes o entidades
        if (resultados.length > 0 && typeof resultados[0] === 'object') {
            // Obtener todas las claves únicas de todos los resultados
            const todasLasClaves = new Set();
            resultados.forEach(item => {
                Object.keys(item).forEach(key => {
                    const value = item[key];
                    // Solo incluir claves con valores válidos
                    if (value !== null && 
                        value !== undefined && 
                        value !== 'N/A' && 
                        !key.includes('password') &&
                        !key.includes('token') &&
                        !key.includes('error')) {
                        todasLasClaves.add(key);
                    }
                });
            });

            const keys = Array.from(todasLasClaves);
            
            if (keys.length > 0) {
                return (
                    <div className="tabla-resultados-dashboard">
                        <h3>Resultados encontrados: {resultados.length}</h3>
                        <div className="tabla-container-dashboard">
                            <table className="tabla-dashboard">
                                <thead>
                                    <tr>
                                        {keys.map(key => (
                                            <th key={key}>{formatearNombreColumna(key)}</th>
                                        ))}
                                    </tr>
                                </thead>
                                <tbody>
                                    {resultados.map((item, index) => (
                                        <tr key={index}>
                                            {keys.map(key => (
                                                <td key={key}>
                                                    {formatearValorCelda(item[key])}
                                                </td>
                                            ))}
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                );
            }
        }

        return <div className="sin-resultados-dashboard">No se encontraron resultados válidos</div>;
    };

    const formatearNombreColumna = (key) => {
        const nombres = {
            'id': 'ID',
            'nombre': 'Nombre',
            'apellido': 'Apellido',
            'correo': 'Correo',
            'identificacion': 'Identificación',
            'genero': 'Género',
            'matriculaAnio': 'Año Matrícula',
            'estado': 'Estado',
            'codigo': 'Código',
            'descripcion': 'Descripción',
            'anio': 'Año',
            'docente': 'Docente',
            'estudiante': 'Estudiante',
            'curso': 'Curso',
            'estudiante_nombre': 'Nombre Estudiante',
            'estudiante_apellido': 'Apellido Estudiante',
            'estudiante_correo': 'Correo Estudiante',
            'estudiante_identificacion': 'ID Estudiante',
            'curso_nombre': 'Nombre Curso',
            'curso_codigo': 'Código Curso',
            'curso_descripcion': 'Descripción Curso',
            'matricula_anio': 'Año Matrícula'
        };
        return nombres[key] || key.charAt(0).toUpperCase() + key.slice(1);
    };

    const formatearValorCelda = (valor) => {
        if (valor === null || valor === undefined) {
            return '-';
        }
        if (typeof valor === 'boolean') {
            return valor ? 'Sí' : 'No';
        }
        if (typeof valor === 'string' && valor.includes('@')) {
            return <a href={`mailto:${valor}`}>{valor}</a>;
        }
        if (typeof valor === 'object') {
            // Si es un objeto, intentar mostrar información útil
            if (valor.nombre) return valor.nombre;
            if (valor.id) return `ID: ${valor.id}`;
            return JSON.stringify(valor);
        }
        return String(valor);
    };
    return (
        <div className="dashboard">
            <header className="dashboard-header">
                <div className="header-title">
                    <h1>EduData</h1>
                    <p>Sistema Académico Inteligente</p>
                </div>
                <div className="header-user">
                    {currentUser && (
                        <span>Bienvenido, {currentUser.username} ({currentUser.role})</span>
                    )}
                    <button onClick={handleLogout} className="btn-logout">
                        Cerrar Sesión
                    </button>
                </div>
                <nav>
                    <Link to="/">Inicio</Link>
                    <Link to="/estudiantes">Estudiantes</Link>
                    <Link to="/docentes">Docentes</Link>
                    <Link to="/cursos">Cursos</Link>
                    <Link to="/matriculas">Matrículas</Link>
                    <Link to="/asistencias">Asistencia</Link>
                    <Link to="/calificaciones">Calificaciones</Link>
                </nav>
            </header>

            <main className="dashboard-main">
                

                {/* Sección de búsqueda inteligente */}
                <section className="busqueda-section-dashboard">
                    <h2>🤖 Búsqueda Inteligente</h2>
                    <div className="busqueda-container-dashboard">
                        <div className="input-group-dashboard">
                            <textarea
                                value={pregunta}
                                onChange={(e) => setPregunta(e.target.value)}
                                placeholder="Haz una pregunta sobre estudiantes, cursos, etc..."
                                className="pregunta-input-dashboard"
                                rows="2"
                            />
                            <button 
                                onClick={realizarBusqueda}
                                disabled={cargando}
                                className="buscar-btn-dashboard"
                            >
                                {cargando ? '🔄' : '🚀'}
                            </button>
                        </div>

                        <div className="ejemplos-dashboard">
                            {ejemplosPregunta.map((ejemplo, index) => (
                                <button
                                    key={index}
                                    onClick={() => usarEjemplo(ejemplo)}
                                    className="ejemplo-btn-dashboard"
                                >
                                    {ejemplo}
                                </button>
                            ))}
                        </div>

                        {error && (
                            <div className="error-dashboard">
                                <p>⚠️ {error}</p>
                                {jpqlGenerado && (
                                    <div className="jpql-dashboard">
                                        <small>JPQL: {jpqlGenerado}</small>
                                    </div>
                                )}
                            </div>
                        )}

                        {cargando && (
                            <div className="loading-dashboard">
                                <div className="loader-dashboard"></div>
                                <p>Procesando pregunta...</p>
                            </div>
                        )}

                        {renderResultados()}
                    </div>
                </section>

                <section className="dashboard-section">
                    <h2>Módulos Principales</h2>
                    <div className="card-container">
                        <div className="card">
                            <div className="card-icon">📚</div>
                            <h3>Estudiantes</h3>
                            <p>Gestor completo de estudiantes, perfiles y datos académicos.</p>
                            <Link to="/estudiantes">
                                <button>Acceder</button>
                            </Link>
                        </div>
                        <div className="card">
                            <div className="card-icon">📝</div>
                            <h3>Asistencia</h3>
                            <p>Control detallado de la asistencia diaria y reportes.</p>
                            <Link to="/asistencias">
                                <button>Acceder</button>
                            </Link>
                        </div>
                        <div className="card">
                            <div className="card-icon">💯</div>
                            <h3>Calificaciones</h3>
                            <p>Registro y consulta de notas, promedios y desempeño.</p>
                            <Link to="/calificaciones">
                                <button>Acceder</button>
                            </Link>
                        </div>
                        <div className="card">
                            <div className="card-icon">🏫</div>
                            <h3>Matrículas</h3>
                            <p>Administración de periodos lectivos, cursos y asignaciones.</p>
                            <Link to="/matriculas">
                                <button>Acceder</button>
                            </Link>
                        </div>
                    </div>
                </section>

                <section className="dashboard-section admin-section">
                    <h2>Gestión Administrativa</h2>
                    <div className="card-container">
                        <div className="card">
                            <div className="card-icon">👨‍🏫</div>
                            <h3>Docentes</h3>
                            <p>Administra el personal docente, sus asignaciones y horarios.</p>
                            <Link to="/docentes">
                                <button>Acceder</button>
                            </Link>
                        </div>
                        <div className="card">
                            <div className="card-icon">📚</div>
                            <h3>Cursos y Materias</h3>
                            <p>Configura y organiza los cursos, materias y currículums.</p>
                            <Link to="/cursos">
                                <button>Gestionar</button>
                            </Link>
                        </div>
                    </div>
                </section>
            </main>

            <footer className="dashboard-footer">
                <p>© 2025 EduData. Todos los derechos reservados.</p>
            </footer>
        </div>
    );
}
