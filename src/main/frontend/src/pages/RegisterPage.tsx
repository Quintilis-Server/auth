import React from 'react';
import '../stylesheet/BaseStyle.scss';

const RegisterPage: React.FC = () => {
    return (
        <div className="page-container">
            <div className="auth-card">
                <h2 className="auth-title">Criar Conta</h2>

                <form action="/register" method="POST" className="auth-form">
                    <div className="form-group">
                        <label htmlFor="username">Usuário</label>
                        <input type="text" id="username" name="username" className="form-input" required />
                    </div>
                    <div className="form-group">
                        <label htmlFor="email">Email</label>
                        <input type="email" id="email" name="email" className="form-input" required />
                    </div>
                    <div className="form-group">
                        <label htmlFor="password">Senha</label>
                        <input type="password" id="password" name="password" className="form-input" required />
                    </div>
                    <button type="submit" className="btn btn-primary btn-block">Registrar</button>
                </form>

                <p className="auth-footer">
                    Já tem conta? <a href="/login">Faça Login</a>
                </p>
            </div>
        </div>
    );
};

export default RegisterPage;
