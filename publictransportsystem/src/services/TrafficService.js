import {
    collection,
    addDoc,
    getDoc,
    getDocs,
    deleteDoc,
    doc,
    query,
    where,
    orderBy,
    serverTimestamp,
    onSnapshot,
    limit,
    updateDoc
} from "firebase/firestore";
import { getStorage, ref, uploadBytes, getDownloadURL } from "firebase/storage";
import { db } from "../firebase/firebase";

// Lấy các thông tin tình trạng giao thông gần đây (1 lần)
export const getTrafficConditions = async () => {
    try {
        const q = query(
            collection(db, "trafficConditions"),
            orderBy("timestamp", "desc"),
            limit(20)
        );

        const querySnapshot = await getDocs(q);
        const trafficData = [];

        querySnapshot.forEach((doc) => {
            trafficData.push({
                id: doc.id,
                ...doc.data(),
                timestamp: doc.data().timestamp?.toDate()
            });
        });

        return querySnapshot.docs.map(doc => {
            const data = doc.data();
            return {
                id: doc.id,
                ...data,
                // Kiểm tra và chuyển đổi timestamp nếu cần
                timestamp: data.timestamp
                    ? (data.timestamp.toDate ? data.timestamp : new Date(data.timestamp.seconds * 1000))
                    : new Date()
            };
        });
    } catch (error) {
        console.error("Lỗi khi lấy thông tin tình trạng giao thông:", error);
        throw error;
    }
};

// Đăng ký lắng nghe thông tin tình trạng giao thông theo thời gian thực
export const subscribeToTrafficConditions = (callback) => {    const q = query(
        collection(db, "trafficConditions"),
        orderBy("timestamp", "desc"),
        limit(20)
    );

    return onSnapshot(q, (querySnapshot) => {
        const conditions = querySnapshot.docs.map(doc => {
            const data = doc.data();
            return {
                id: doc.id,
                ...data,
                // Kiểm tra và chuyển đổi timestamp nếu cần
                timestamp: data.timestamp
                    ? (data.timestamp.toDate ? data.timestamp : new Date(data.timestamp.seconds * 1000))
                    : new Date()
            };
        });
        callback(conditions);
    });
};

// Thêm tình trạng giao thông mới
export const addTrafficCondition = async (trafficData) => {
    try {
        // Đảm bảo có thời gian và trạng thái active
        const dataToAdd = {
            ...trafficData,
            timestamp: serverTimestamp(),
            status: "active"
        };

        const docRef = await addDoc(collection(db, "trafficConditions"), dataToAdd);
        return docRef.id;
    } catch (error) {
        console.error("Lỗi khi thêm tình trạng giao thông:", error);
        throw error;
    }
};

// Cập nhật tình trạng giao thông
export const updateTrafficCondition = async (id, trafficData) => {
    try {
        const trafficRef = doc(db, "trafficConditions", id);
        await updateDoc(trafficRef, {
            ...trafficData,
            updatedAt: serverTimestamp()
        });
        return true;
    } catch (error) {
        console.error("Lỗi khi cập nhật tình trạng giao thông:", error);
        throw error;
    }
};

// Xóa tình trạng giao thông (hoặc đánh dấu là không active)
export const deleteTrafficCondition = async (id) => {
    try {
        // Xóa hoàn toàn document
        const trafficRef = doc(db, "trafficConditions", id);
        await deleteDoc(trafficRef);

        return true;
    } catch (error) {
        console.error("Lỗi khi xóa tình trạng giao thông:", error);
        throw error;
    }
};
export const reportTrafficCondition = async (reportData, imageFile) => {
    try {
        let imageUrl = null;

        // Nếu có file hình, tải lên Storage trước
        if (imageFile) {
            const storage = getStorage();
            const timestamp = new Date().getTime();
            const storageRef = ref(storage, `traffic-reports/${timestamp}_${imageFile.name}`);

            // Upload file
            const uploadResult = await uploadBytes(storageRef, imageFile);

            // Lấy URL download
            imageUrl = await getDownloadURL(uploadResult.ref);
        }

        // Thêm dữ liệu vào Firestore với URL hình ảnh (nếu có)
        const docRef = await addDoc(collection(db, "trafficConditions"), {
            ...reportData,
            imageUrl,
            timestamp: serverTimestamp(),
            status: 'active',
            reportedBy: reportData.userId || 'anonymous'
        });

        return {
            id: docRef.id,
            ...reportData,
            imageUrl,
            timestamp: new Date(),
        };
    } catch (error) {
        console.error("Lỗi khi báo cáo tình trạng giao thông:", error);
        throw error;
    }
};
