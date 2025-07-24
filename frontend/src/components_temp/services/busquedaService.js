import api from './api';

// Servicio simplificado para el Dashboard
export const busquedaService = {
    // Realizar consulta en lenguaje natural
    consultarNLQ: async (pregunta) => {
        try {
            console.log('🔍 Realizando consulta NLQ:', pregunta);
            
            // Verificar que hay token antes de hacer la consulta
            const token = localStorage.getItem('token');
            if (!token) {
                throw new Error('No hay token de autenticación. Por favor, inicia sesión.');
            }
            
            const response = await api.post('/nlq/pregunta', pregunta, {
                headers: {
                    'Content-Type': 'text/plain',
                    'Authorization': `Bearer ${token}`
                },
            });
            
            console.log('✅ Respuesta NLQ recibida:', response.data);
            return response.data;
        } catch (error) {
            console.error('❌ Error en consulta NLQ:', error);
            
            if (error.response?.status === 401) {
                throw new Error('No tienes autorización para realizar esta consulta. Verifica tu sesión.');
            }
            
            const errorMessage = error.response?.data?.message || 
                               error.response?.data?.error || 
                               error.message || 
                               'Error en la consulta';
            throw new Error(`Error en la consulta: ${errorMessage}`);
        }
    },

    // Obtener estadísticas rápidas del sistema
    obtenerEstadisticas: async () => {
        try {
            console.log('📊 Cargando estadísticas del sistema...');
            
            // Verificar autenticación
            const token = localStorage.getItem('token');
            if (!token) {
                console.warn('⚠️ No hay token, devolviendo estadísticas vacías');
                return {
                    totalEstudiantes: 0,
                    totalCursos: 0,
                    totalDocentes: 0,
                    totalMatriculas: 0
                };
            }
            
            // Hacer llamadas en paralelo con timeout reducido
            const promises = [
                api.get('/estudiantes').catch(() => ({ data: [] })),
                api.get('/cursos').catch(() => ({ data: [] })),
                api.get('/docentes').catch(() => ({ data: [] })),
                api.get('/matriculas').catch(() => ({ data: [] }))
            ];
            
            const [estudiantes, cursos, docentes, matriculas] = await Promise.all(promises);
            
            const stats = {
                totalEstudiantes: estudiantes.data.length || 0,
                totalCursos: cursos.data.length || 0,
                totalDocentes: docentes.data.length || 0,
                totalMatriculas: matriculas.data.length || 0
            };
            
            console.log('📊 Estadísticas cargadas:', stats);
            return stats;
            
        } catch (error) {
            console.error('❌ Error obteniendo estadísticas:', error);
            return {
                totalEstudiantes: 0,
                totalCursos: 0,
                totalDocentes: 0,
                totalMatriculas: 0
            };
        }
    }
};

export default busquedaService;
