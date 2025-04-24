import React, { useEffect, useState } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import userMarkerImg from "./image/sahur.png"
// Fix icon
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: require("leaflet/dist/images/marker-icon-2x.png"),
    iconUrl: require("leaflet/dist/images/marker-icon.png"),
    shadowUrl: require("leaflet/dist/images/marker-shadow.png"),
});


const userIcon = new L.Icon({
    iconUrl: userMarkerImg,
    iconSize: [32, 32],
    iconAnchor: [16, 32],
    popupAnchor: [0, -32],
    shadowUrl: require("leaflet/dist/images/marker-shadow.png"),
    shadowSize: [41, 41],
    shadowAnchor: [12, 41],
});

// Component để di chuyển map đến vị trí user mỗi khi update
const SetViewToUserLocation = ({ position }) => {
    const map = useMap();
    useEffect(() => {
        if (position) {
            map.setView(position, 15, { animate: true });
        }
    }, [map, position]);
    return null;
};

const MapLeaflet = () => {
    const [userLocation, setUserLocation] = useState(null);

    useEffect(() => {
        if (navigator.geolocation) {
            const watcher = navigator.geolocation.watchPosition(
                (pos) => {
                    const { latitude, longitude } = pos.coords;
                    console.log("Vị trí mới:", latitude, longitude);
                    setUserLocation([latitude, longitude]);
                },
                (err) => {
                    console.error("Lỗi lấy vị trí:", err);
                    // xử lý lỗi như bạn đã làm
                },
                {
                    enableHighAccuracy: true,
                    timeout: 10000,
                    maximumAge: 0,
                }
            );

            // Clean up khi component bị unmount
            return () => navigator.geolocation.clearWatch(watcher);
        }
    }, []);



    // Khi chưa có vị trí, hiển thị loading
    if (!userLocation) {
        return <div>Đang xác định vị trí của bạn…</div>;
    }



    return (
        <MapContainer
            center={userLocation}
            zoom={15}
            style={{ height: "100vh", width: "100%" }}
        >
            <TileLayer
                attribution='© <a href="https://www.openstreetmap.org/">OpenStreetMap</a>'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />

            <SetViewToUserLocation position={userLocation} />

            <Marker position={userLocation} icon={userIcon}>
                <Popup>
                    Bạn đang ở đây:<br />
                    {userLocation[0].toFixed(6)}, {userLocation[1].toFixed(6)}
                </Popup>
            </Marker>
        </MapContainer>
    );
};

export default MapLeaflet;
