import firebase from "firebase/app";

export async function runGoogleAuth(firebaseApp: ReturnType<typeof firebase.app>): Promise<void> {
    const provider = new firebase.auth.GoogleAuthProvider();
    await firebaseApp.auth().setPersistence(firebase.auth.Auth.Persistence.LOCAL);
    firebaseApp.auth().useDeviceLanguage();
    await firebaseApp.auth().signInWithRedirect(provider);
}
