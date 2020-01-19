import firebase from "firebase/app";
import "firebase/analytics";
import "firebase/auth";
import "firebase/firestore";
import {removeReduxInfo, setupReduxUserHooks} from "./redux-init";

const firebaseConfig = {
    apiKey: "AIzaSyCrNDjfel9oRRqvNpfVRfjjlE3z8Va3s6o",
    authDomain: "clock-resonator.firebaseapp.com",
    databaseURL: "https://clock-resonator.firebaseio.com",
    projectId: "clock-resonator",
    storageBucket: "clock-resonator.appspot.com",
    messagingSenderId: "933400210274",
    appId: "1:933400210274:web:fdb8e6d20469343a951a3b",
    measurementId: "G-W5TL9B2Y9B"
};

export const firebaseApp = firebase.initializeApp(firebaseConfig);
firebase.analytics();

const auth = firebaseApp.auth();
let tearDown: () => void = () => {
};
auth.onAuthStateChanged(user => {
    tearDown();
    tearDown = () => {
    };
    if (user) {
        tearDown = setupReduxUserHooks(user);
    } else {
        removeReduxInfo();
    }
}, error => console.log(error));
auth.getRedirectResult()
    .then(creds => {
        if (creds.user === null) {
            return;
        }
        return auth.updateCurrentUser(creds.user);
    })
    .catch(err => {
        console.info(err);
    });

