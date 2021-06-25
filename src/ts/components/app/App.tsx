import loadable from "@loadable/component";
import React from "react";
import {hot} from "react-hot-loader/root";
import {Provider} from "react-redux";
import {BrowserRouter as Router, Route, Switch} from "react-router-dom";
import {Container} from "reactstrap";
import {store} from "../../redux/store";
import ScrollToTop from "../compat/ScrollToTop";
import {SimpleErrorBoundary} from "../SimpleErrorBoundary";
import {ClockResonatorNavbar} from "./ClockResonatorNavbar";

const AddNewEntryLazy = loadable(() => import("./AddNewEntry"));
const UserTimeCardsLazy = loadable(() => import("../UserTimeCards"));

const HotPortion = hot(() => {
    return <SimpleErrorBoundary context="the application root">
        <ClockResonatorNavbar/>
        <Container fluid={true} className="p-3">
            <Switch>
                <Route path="/entries/add">
                    <AddNewEntryLazy/>
                </Route>
                <Route path="/">
                    <UserTimeCardsLazy/>
                </Route>
            </Switch>
        </Container>
    </SimpleErrorBoundary>;
});

export const App = () => {
    return <Router>
        <ScrollToTop/>
        <Provider store={store}>
            <HotPortion/>
        </Provider>
    </Router>;
};