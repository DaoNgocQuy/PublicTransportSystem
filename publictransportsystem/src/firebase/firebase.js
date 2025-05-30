import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";
import { getStorage } from "firebase/storage";
import { getMessaging } from "firebase/messaging";

const firebaseConfig = {
    apiKey: "AIzaSyAabboFplnhdRMixjx9G3RS9tzxmxhc79s",
    authDomain: "publictransportsystem-55f26.firebaseapp.com",
    projectId: "publictransportsystem-55f26",
    storageBucket: "publictransportsystem-55f26.firebasestorage.app",
    messagingSenderId: "857872116236",
    appId: "1:857872116236:web:8eb0a558045bf761461c29",
    measurementId: "G-J136GRR4CL"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db = getFirestore(app);
const storage = getStorage(app);
const messaging = getMessaging(app);

export { app, auth, db, storage, messaging };