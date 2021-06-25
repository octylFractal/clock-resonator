import React, {ReactNode} from "react";
import {NavLink, NavLinkProps} from "react-router-dom";

export interface RoutedNavLinkProps extends NavLinkProps {
    children?: ReactNode
}

const RoutedNavLink: React.FC<RoutedNavLinkProps> = ({children, ...props}) => {
    return <NavLink className="routed nav-link nav-item m-1" activeClassName="active" {...props}>
        {children}
    </NavLink>;
};

export default RoutedNavLink;
