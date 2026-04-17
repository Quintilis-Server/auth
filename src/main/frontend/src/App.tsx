import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import './stylesheet/BaseStyle.scss';
import {AccountPage} from "./pages/AccountPage.tsx";

const App: React.FC = () => {
    return (
        <Router>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                {/* Rota padrão redireciona para login ou home */}
                <Route path="/account" element={<AccountPage />} />
                <Route path="/" element={<LoginPage />} />
            </Routes>
        </Router>
    );
};

export default App;
