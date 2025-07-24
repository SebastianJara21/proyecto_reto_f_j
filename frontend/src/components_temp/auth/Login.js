import React, { useState } from 'react';
import { authService } from '../services/authService';
import '../layout/Dashboard.css';

const Login = ({ onLogin }) => {
    const [isLogin, setIsLogin] = useState(true);
    const [formData, setFormData] = useState({
        username: '',
        password: '',
        email: '',
        nombre: '',
        apellido: '',
        role: 'INVITADO'
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            console.log('🚀 Iniciando proceso de autenticación...');
            
            if (isLogin) {
                // Validación básica
                if (!formData.username.trim() || !formData.password.trim()) {
                    throw new Error('Por favor, completa todos los campos');
                }
                
                console.log('🔐 Intentando login...');
                await authService.login({
                    username: formData.username.trim(),
                    password: formData.password
                });
            } else {
                // Validación para registro
                if (!formData.username.trim() || !formData.password.trim() || 
                    !formData.email.trim() || !formData.nombre.trim() || !formData.apellido.trim()) {
                    throw new Error('Por favor, completa todos los campos');
                }
                
                console.log('📝 Intentando registro...');
                await authService.register(formData);
            }
            
            console.log('✅ Autenticación exitosa, redirigiendo...');
            onLogin();
        } catch (error) {
            console.error('❌ Error en autenticación:', error);
            setError(typeof error === 'string' ? error : error.message || 'Error de autenticación');
        } finally {
            setLoading(false);
        }
    };

    const handleGuestLogin = async () => {
        setLoading(true);
        setError('');
        
        try {
            console.log('👤 Iniciando login como invitado...');
            await authService.loginAsGuest();
            console.log('✅ Login como invitado exitoso, redirigiendo...');
            onLogin();
        } catch (error) {
            console.error('❌ Error en login como invitado:', error);
            setError(typeof error === 'string' ? error : error.message || 'Error al iniciar sesión como invitado');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <div className="login-card">
                <h2>{isLogin ? 'Iniciar Sesión' : 'Registrarse'}</h2>
                
                {error && (
                    <div className="error-message">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <input
                            type="text"
                            name="username"
                            placeholder="Nombre de usuario"
                            value={formData.username}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <input
                            type="password"
                            name="password"
                            placeholder="Contraseña"
                            value={formData.password}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    {!isLogin && (
                        <>
                            <div className="form-group">
                                <input
                                    type="email"
                                    name="email"
                                    placeholder="Correo electrónico"
                                    value={formData.email}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <input
                                    type="text"
                                    name="nombre"
                                    placeholder="Nombre"
                                    value={formData.nombre}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <input
                                    type="text"
                                    name="apellido"
                                    placeholder="Apellido"
                                    value={formData.apellido}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <select
                                    name="role"
                                    value={formData.role}
                                    onChange={handleChange}
                                    required
                                >
                                    <option value="INVITADO">Invitado</option>
                                    <option value="ESTUDIANTE">Estudiante</option>
                                    <option value="DOCENTE">Docente</option>
                                </select>
                            </div>
                        </>
                    )}

                    <button type="submit" disabled={loading} className="submit-btn">
                        {loading ? 'Procesando...' : (isLogin ? 'Iniciar Sesión' : 'Registrarse')}
                    </button>
                </form>

                <div className="auth-options">
                    <button 
                        onClick={() => setIsLogin(!isLogin)}
                        className="link-btn"
                    >
                        {isLogin ? '¿No tienes cuenta? Regístrate' : '¿Ya tienes cuenta? Inicia sesión'}
                    </button>

                    <button 
                        onClick={handleGuestLogin}
                        disabled={loading}
                        className="guest-btn"
                    >
                        Entrar como Invitado
                    </button>
                </div>

                
            </div>
        </div>
    );
};

export default Login;
