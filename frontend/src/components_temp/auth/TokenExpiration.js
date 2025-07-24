import React, { useState, useEffect } from 'react';
import { authService } from '../services/authService';

const TokenExpiration = () => {
    const [timeRemaining, setTimeRemaining] = useState(0);
    const [showWarning, setShowWarning] = useState(false);

    useEffect(() => {
        const updateTimeRemaining = () => {
            const remaining = authService.getTokenTimeRemaining();
            setTimeRemaining(remaining);
            
            // Mostrar advertencia si quedan menos de 10 minutos
            setShowWarning(remaining > 0 && remaining < 600); // 10 minutos
            
            // Auto logout si el token expiró
            if (remaining <= 0 && authService.isAuthenticated()) {
                console.warn('⚠️ Token expirado, cerrando sesión automáticamente');
                authService.logout();
            }
        };

        // Actualizar inmediatamente
        updateTimeRemaining();
        
        // Actualizar cada minuto
        const interval = setInterval(updateTimeRemaining, 60000);
        
        return () => clearInterval(interval);
    }, []);

    const formatTime = (seconds) => {
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        return `${hours}h ${minutes}m`;
    };

    if (!authService.isAuthenticated()) {
        return null;
    }

    return (
        <div className={`token-expiration ${showWarning ? 'warning' : ''}`}>
            {showWarning && (
                <div className="alert alert-warning">
                    ⚠️ Su sesión expirará en {formatTime(timeRemaining)}
                </div>
            )}
        </div>
    );
};

export default TokenExpiration;
