import React from "react";
import {Header} from "../component/Header.tsx";
import "../stylesheet/AccountPageStyle.scss"



export class AccountPage extends React.Component<any, any>{
    // async componentDidMount() {
    //     // const response = await fetch(`${AUTH_URL}/users/me`)
    //     // const user = await response.json();
    //     // console.log(user);
    // }

    render() {
        return (
            <>
                <Header/>
                <main className="account-main">
                    <div className="options">

                    </div>
                    <div className="inner">

                    </div>
                </main>
            </>
        )
    }
}