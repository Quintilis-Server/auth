import React from 'react';
import "../stylesheet/LoginPage.scss"

const LoginPage: React.FC = () => {
    const queryParams = new URLSearchParams(window.location.search);
    const hasError = queryParams.has('error');
    const hasLogout = queryParams.has('logout');
    const hasSuccess = queryParams.has('success');

    return (
        <main className="page-container">
            <div className="auth-card">
                <h2 className="auth-title">
                    <span>Q</span>
                    <span>uintilis ID</span>
                </h2>

                {hasError && <div className="alert alert-error">Usuário ou senha inválidos.</div>}
                {hasLogout && <div className="alert alert-success">Você saiu com sucesso.</div>}
                {hasSuccess && <div className="alert alert-success">Conta criada! Faça login.</div>}

                <form action="/login" method="POST" className="auth-form">
                    <div className="form-group">
                        <label htmlFor="username">Usuário ou Email</label>
                        <input type="text" id="username" name="username" className="form-input" required autoFocus />
                    </div>
                    <div className="form-group">
                        <label htmlFor="password">Senha</label>
                        <input type="password" id="password" name="password" className="form-input" required />
                    </div>
                    <button type="submit" className="btn btn-primary btn-block">Entrar</button>
                </form>

                <div className="auth-divider">
                    <span>OU ENTRE COM</span>
                </div>

                <div className="social-buttons">
                    <a href="/oauth2/authorization/google" className="btn btn-social btn-google">
                        Google
                    </a>
                    <a href="/oauth2/authorization/microsoft" className="btn btn-social btn-microsoft">
                        Microsoft (Xbox)
                    </a>
                </div>

                <p className="auth-footer">
                    Não tem conta? <a href="/register">Registre-se</a>
                </p>
            </div>
        </main>
    );
};

export default LoginPage;
