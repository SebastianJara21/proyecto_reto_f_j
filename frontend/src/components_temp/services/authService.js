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
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login';
    },

    getCurrentUser: () => {
        const user = localStorage.getItem('user');
        return user ? JSON.parse(user) : null;
    },

    isAuthenticated: () => {
        const token = localStorage.getItem('token');
        return !!token;
    },

    getToken: () => {
        return localStorage.getItem('token');
    }
};
