import {UserInfo} from "firebase";
import {userInfo} from "../redux/reducer";
import {store} from "../redux/store";
import {firebaseApp} from "./setup";

export function setupReduxUserHooks(user: UserInfo): () => void {
    const firestore = firebaseApp.firestore();
    store.dispatch(userInfo.login({
        name: user.displayName || user.email || "?????",
        uid: user.uid
    }));
    const unsubs: (() => void)[] = [];
    unsubs.push(firestore.collection("timeCardEntries")
        .where("owner", "==", user.uid)
        .onSnapshot(() => {

        }));
    return () => {
        for (const unsub of unsubs) {
            unsub();
        }
    };
}

export function removeReduxInfo() {
    store.dispatch(userInfo.logout());
}
