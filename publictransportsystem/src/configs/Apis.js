import axios from "axios";

const BASE_URL = 'http://localhost:8080/PublicTransportSystem/api/';

export const endpoints = {

}

export default axios.create({
    baseURL: BASE_URL
});