import {faPlus} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import React, {useState} from "react";
import {Collapse, Nav, Navbar, NavbarBrand, NavbarToggler} from "reactstrap";
import RoutedNavLink from "../compat/RoutedNavLink";
import {LocalUserState} from "../LocalUserState";

export const ClockResonatorNavbar: React.FC = () => {
    const [isOpen, setOpen] = useState(false);

    function toggle() {
        setOpen(!isOpen);
    }

    return <Navbar color="dark" dark expand="md">
        <NavbarBrand href="/" className="py-0">
            <h1 className="font-face-geo m-0">Clock Resonator</h1>
        </NavbarBrand>
        <NavbarToggler onClick={toggle}/>
        <Collapse isOpen={isOpen} navbar>
            <Nav className="mr-auto" navbar pills>
                <RoutedNavLink exact to="/">Home</RoutedNavLink>
                <RoutedNavLink exact to="/entries/add">
                    <FontAwesomeIcon icon={faPlus}/> Add Entry
                </RoutedNavLink>
            </Nav>
            <Nav className="ml-auto" navbar>
                <LocalUserState/>
            </Nav>
        </Collapse>
    </Navbar>;
};