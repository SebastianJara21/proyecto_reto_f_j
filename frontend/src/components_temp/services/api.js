import axios from "axios";

// Configuración de la URL base según el entorno
const getBaseURL = () => {
    // En producción, usar la variable de entorno o la URL de Render
    if (process.env.NODE_ENV === 'production') {
        return process.env.REACT_APP_API_URL || 'https://edudata-backend.onrender.com/api';
    }
    // En desarrollo, usar localhost
    return process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
};

const api = axios.create({
    baseURL: getBaseURL(),
    timeout: 10000, // 10 segundos de timeout
    headers: {
        'Content-Type': 'application/json',
    }
});

// Interceptor para agregar token de autenticación
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            // Verificar si el token está expirado antes de enviarlo
            try {
                const payload = JSON.parse(atob(token.split('.')[1]));
                const currentTime = Date.now() / 1000;
                
                if (payload.exp && payload.exp < currentTime) {
                    console.warn('⚠️ Token expirado, eliminando del localStorage');
                    localStorage.removeItem('token');
                    localStorage.removeItem('user');
                    window.location.href = '/login';
                    return Promise.reject(new Error('Token expirado'));
                }
            } catch (e) {
                console.warn('⚠️ Token malformado, eliminando del localStorage');
                localStorage.removeItem('token');
                localStorage.removeItem('user');
                window.location.href = '/login';
                return Promise.reject(new Error('Token malformado'));
            }
            
            config.headers.Authorization = `Bearer ${token}`;
        }
        console.log('🔧 API Request:', config.method?.toUpperCase(), config.url, 
                    token ? '✅ Con token' : '❌ Sin token',
                    'RequestID:', Math.random().toString(36).substr(2, 9));
        return config;
    },
    (error) => {
        console.error('❌ Error en request interceptor:', error);
        return Promise.reject(error);
    }
);

// Interceptor para manejar respuestas de error
api.interceptors.response.use(
    (response) => {
        console.log('✅ API Response:', response.status, response.config.url);
        return response;
    },
    (error) => {
        console.error('❌ API Error:', error.response?.status, error.response?.data);
        
        // Manejar errores de autenticación y tokens expirados
        if (error.response?.status === 401 || error.response?.status === 403) {
            console.warn('🔒 Token inválido o expirado, redirigiendo al login');
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            
            // Solo redirigir si no estamos ya en la página de login
            if (window.location.pathname !== '/login') {
                window.location.href = '/login';
            }
        }
        
        // Mejorar el manejo de errores
        const errorMessage = error.response?.data?.message || 
                           error.response?.data?.error || 
                           error.message || 
                           'Error desconocido';
        
        return Promise.reject(new Error(errorMessage));
    }
);

export default api;
