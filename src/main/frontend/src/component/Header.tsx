import {Component} from "react";
import "../stylesheet/NavBarStyle.scss"
import {AUTH_URL, FORUM_URL, FRONTEND_URL, MAP_URL} from "../Consts.ts";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faArrowRightToBracket} from "@fortawesome/free-solid-svg-icons";

export class Header extends Component<any, any>{
    private logout() {
        window.location.href = `${AUTH_URL}/logout?redirect_uri=${FRONTEND_URL}`;
    }

    render() {
        return (
            <div className="navbar" style={{padding: "1em 4em"}}>
                <div className="navbar-inner">
                    <a className="nav-logo" href={`${FRONTEND_URL}/`}>
                        <div>
                            <span className="logo-letter">Q</span>
                            <span className="logo-text">uintilis</span>
                        </div>
                    </a>

                    <div className="nav-links">
                        <a href={`${FORUM_URL}`}>Forum</a>
                        <a href={MAP_URL}>Map</a>
                    </div>

                    <div className="nav-actions">
                        <div className="user-menu">
                            <FontAwesomeIcon onClick={()=>this.logout()} icon={faArrowRightToBracket}/>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}