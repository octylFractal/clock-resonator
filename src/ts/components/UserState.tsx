import {faUser} from "@fortawesome/free-regular-svg-icons";
import {faSignInAlt, faSpinner} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import React from "react";
import {Button, DropdownItem, DropdownMenu, DropdownToggle, NavItem, UncontrolledButtonDropdown} from "reactstrap";
import {runGoogleAuth} from "../firebase/auth";
import {firebaseApp} from "../firebase/setup";
import {UserInfoRecord} from "../redux/reducer";

export const UserState: React.FC<{ userInfo: UserInfoRecord }> = ({userInfo: {isKnown, name}}) => {
    if (!isKnown) {
        return <NavItem>
            <div className="navbar-text">
                <FontAwesomeIcon spin icon={faSpinner}/>
            </div>
        </NavItem>;
    }
    if (typeof name == 'undefined') {
        function signIn() {
            runGoogleAuth(firebaseApp).catch(err => console.error(err));
        }

        return <NavItem>
            <Button onClick={signIn}>
                <FontAwesomeIcon icon={faSignInAlt}/> Sign In
            </Button>
        </NavItem>;
    }

    function signOut() {
        firebaseApp.auth().signOut()
            .catch(err => console.error(err));
    }

    return <UncontrolledButtonDropdown nav inNavbar>
        <DropdownToggle nav caret>
            <FontAwesomeIcon icon={faUser}/> {name}
        </DropdownToggle>
        <DropdownMenu right>
            <DropdownItem onClick={signOut}>
                Sign Out
            </DropdownItem>
        </DropdownMenu>
    </UncontrolledButtonDropdown>
};