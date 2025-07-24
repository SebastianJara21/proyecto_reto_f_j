import api from './api';

export const authService = {
    login: async (credentials) => {
        try {
            console.log('🔐 Intentando login para:', credentials.username);
            const response = await api.post('/auth/login', credentials);
            const { token, username, role } = response.data;
            
            if (!token) {
                throw new Error('No se recibió token del servidor');
            }
            
            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify({ username, role }));
            
            console.log('✅ Login exitoso para:', username, 'con rol:', role);
            return response.data;
        } catch (error) {
            console.error('❌ Error en login:', error);
            const errorMessage = error.response?.data?.message || 
                               error.response?.data?.error || 
                               error.message || 
                               'Error al iniciar sesión';
            throw errorMessage;
        }
    },

    register: async (userData) => {
        try {
            console.log('📝 Intentando registro para:', userData.username);
            const response = await api.post('/auth/register', userData);
            const { token, username, role } = response.data;
            
            if (!token) {
                throw new Error('No se recibió token del servidor');
            }
            
            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify({ username, role }));
            
            console.log('✅ Registro exitoso para:', username, 'con rol:', role);
            return response.data;
        } catch (error) {
            console.error('❌ Error en registro:', error);
            const errorMessage = error.response?.data?.message || 
                               error.response?.data?.error || 
                               error.message || 
                               'Error al registrarse';
            throw errorMessage;
        }
    },

    loginAsGuest: async () => {
        try {
            console.log('👤 Intentando login como invitado');
            const response = await api.post('/auth/guest');
            const { token, username, role } = response.data;
            
            if (!token) {
                throw new Error('No se recibió token del servidor');
            }
            
            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify({ username, role }));
            
            console.log('✅ Login como invitado exitoso:', username, 'con rol:', role);
            return response.data;
        } catch (error) {
            console.error('❌ Error en login como invitado:', error);
            const errorMessage = error.response?.data?.message || 
                               error.response?.data?.error || 
                               error.message || 
                               'Error al iniciar sesión como invitado';
            throw errorMessage;
        }
    },

    logout: () => {
        console.log('🚪 Cerrando sesión y limpiando datos...');
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        
        // Limpiar cualquier timeout o interval que pueda estar ejecutándose
        if (window.tokenCheckInterval) {
            clearInterval(window.tokenCheckInterval);
        }
        
        window.location.href = '/login';
    },

    getCurrentUser: () => {
        const user = localStorage.getItem('user');
        return user ? JSON.parse(user) : null;
    },

    isAuthenticated: () => {
        const token = localStorage.getItem('token');
        if (!token) return false;
        
        // Verificar si el token está expirado
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const currentTime = Date.now() / 1000;
            
            if (payload.exp && payload.exp < currentTime) {
                console.warn('⚠️ Token expirado detectado en isAuthenticated');
                localStorage.removeItem('token');
                localStorage.removeItem('user');
                return false;
            }
            return true;
        } catch (e) {
            console.error('❌ Error al verificar token:', e);
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            return false;
        }
    },

    getToken: () => {
        return localStorage.getItem('token');
    },

    // Función para verificar periódicamente la expiración del token
    startTokenExpirationCheck: () => {
        // Verificar cada 5 minutos si el token está expirado
        window.tokenCheckInterval = setInterval(() => {
            if (!authService.isAuthenticated()) {
                console.warn('⚠️ Token expirado detectado en verificación periódica');
                authService.logout();
            }
        }, 5 * 60 * 1000); // 5 minutos
    },

    // Función para obtener el tiempo restante del token
    getTokenTimeRemaining: () => {
        const token = localStorage.getItem('token');
        if (!token) return 0;
        
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const currentTime = Date.now() / 1000;
            
            if (payload.exp) {
                const remaining = payload.exp - currentTime;
                return Math.max(0, remaining);
            }
        } catch (e) {
            console.error('❌ Error al obtener tiempo restante del token:', e);
        }
        return 0;
    }
};
