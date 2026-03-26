import React from "react";
import { Header } from "../component/Header.tsx";
import "../stylesheet/AccountPageStyle.scss";
import type {User} from "../types/User.ts";
import {AUTH_URL} from "../Consts.ts";
import axios from "axios";
import type {ApiResponseType} from "../types/ApiResponseType.ts"; // Certifique-se de ter os estilos que mandei antes!

// Tipagem básica do estado para o TypeScript não reclamar
interface AccountPageState {
    activeTab: "info" | "minecraft";
    user: User | null,
    minecraft: {
        activePlayers: string[]; // Lista que virá da API do servidor
        selectedPlayer: string;
        verificationCode: string;
    };
}

export class AccountPage extends React.Component<any, AccountPageState> {
    constructor(props: any) {
        super(props);
        this.state = {
            activeTab: "info", // Aba padrão
            user: null,
            minecraft: {
                // Mock de jogadores online no seu servidor
                activePlayers: [],
                selectedPlayer: "",
                verificationCode: ""
            }
        };
    }

    async componentDidMount() {
        const response = await axios.get<ApiResponseType<User>>(`${AUTH_URL}/users/me`);
        const user = response.data.data;
        // this.setState({user: {
        //         id: "5a085bb2-9204-4cee-9391-df1e20f57ba0",
        //         username: "Abacatao",
        //         roles: [],
        //         isVerified: false
        //     }});

        this.setState({user});
        // Exemplo de como você vai buscar os dados depois:
        // try {
        //     const response = await fetch(`${AUTH_URL}/users/me`, { credentials: 'include' });
        //     const userData = await response.json();
        //     this.setState({ user: userData });
        //
        //     const playersResponse = await fetch(`${MINECRAFT_API}/players/online`);
        //     const players = await playersResponse.json();
        //     this.setState(prevState => ({
        //         minecraft: { ...prevState.minecraft, activePlayers: ["Selecione...", ...players] }
        //     }));
        // } catch (e) { console.error(e) }
    }

    // Função para mudar de aba
    changeTab = (tab: "info" | "minecraft") => {
        this.setState({ activeTab: tab });
    };

    // Função para tentar vincular o Minecraft
    handleVerifyMinecraft = (e: React.FormEvent) => {
        e.preventDefault();
        const { selectedPlayer, verificationCode } = this.state.minecraft;

        if (!selectedPlayer || selectedPlayer.startsWith("Selecione")) {
            alert("Por favor, selecione um jogador na lista!");
            return;
        }

        console.log(`Enviando para API: Jogador [${selectedPlayer}], Código [${verificationCode}]`);
        alert(`Tentando vincular a conta de ${selectedPlayer} com o código ${verificationCode}...`);

        // Aqui vai o seu fetch POST para o backend validar o código!
    };

    // Renderiza o conteúdo do lado direito com base na aba ativa
    renderContent = () => {
        const { activeTab, user, minecraft } = this.state;

        if (activeTab === "info") {
            return (
                <div className="tab-content">
                    <h2>Minha Conta</h2>
                    <p className="description">Gerencie as informações básicas do seu perfil na Quintilis.</p>

                    <div className="info-group">
                        <label>Nome de Exibição</label>
                        <input
                            type="text"
                            value={user?.username || ""}
                            onChange={(e)=>{
                                this.setState(prevState => ({
                                    user: prevState.user ? {
                                        ...prevState.user, // Copia o email, id, etc. antigos
                                        username: e.target.value // Substitui apenas o username
                                    } : null
                                }));
                            }}
                            className="form-input" />
                    </div>

                    <div className="info-group">
                        <label>E-mail Vinculado</label>
                        <input type="email" readOnly value={user?.email || ""} className="form-input" />
                    </div>

                    <div className="info-group">
                        <label>ID da Conta</label>
                        <input type="text" readOnly value={user?.id} className="form-input disabled" />
                    </div>
                </div>
            );
        }

        if (activeTab === "minecraft") {
            return (
                <div className="tab-content">
                    <h2>Vincular Minecraft</h2>
                    <p className="description">
                        Entre no nosso servidor (<b>jogar.quintilis.org</b>) e digite o comando <code>/link</code> para gerar seu código de verificação.
                    </p>

                    <form onSubmit={this.handleVerifyMinecraft} className="minecraft-form">
                        <div className="form-group">
                            <label>Selecione seu Nick (Jogadores Online)</label>
                            <select
                                className="form-select"
                                value={minecraft.selectedPlayer}
                                onChange={(e) => this.setState({
                                    minecraft: { ...minecraft, selectedPlayer: e.target.value }
                                })}
                            >
                                {minecraft.activePlayers.map((player, index) => (
                                    <option key={index} value={player}>{player}</option>
                                ))}
                            </select>
                        </div>

                        <div className="form-group">
                            <label>Código de Verificação</label>
                            <p>{minecraft.verificationCode}</p>
                            {/*<input*/}
                            {/*    type="text"*/}
                            {/*    className="form-input code-input"*/}
                            {/*    placeholder="Ex: A7X9B2"*/}
                            {/*    value={minecraft.verificationCode}*/}
                            {/*    onChange={(e) => this.setState({*/}
                            {/*        minecraft: { ...minecraft, verificationCode: e.target.value.toUpperCase() }*/}
                            {/*    })}*/}
                            {/*    maxLength={6}*/}
                            {/*/>*/}
                        </div>

                        <button type="submit" className="btn-submit">
                            Verificar e Vincular Conta
                        </button>
                    </form>
                </div>
            );
        }
    };

    render() {
        const { activeTab } = this.state;

        return (
            <>
                <Header />
                <main className="account-main">

                    {/* MENU LATERAL */}
                    <div className="options">
                        <span className="category-title">Configurações de Usuário</span>
                        <button
                            className={activeTab === "info" ? "active" : ""}
                            onClick={() => this.changeTab("info")}
                        >
                            Informações Pessoais
                        </button>

                        <div className="divider"></div>

                        <span className="category-title">Integrações</span>
                        <button
                            className={activeTab === "minecraft" ? "active" : ""}
                            onClick={() => this.changeTab("minecraft")}
                        >
                            Servidor de Minecraft
                        </button>
                    </div>

                    {/* ÁREA DE CONTEÚDO */}
                    <div className="inner">
                        {this.renderContent()}
                    </div>

                </main>
            </>
        );
    }
}