import axios from "axios";

// import.meta.env.VITE_API_URL lee el "http://host.docker.internal:8080" de tu compose
export default axios.create({
  baseURL: `${import.meta.env.VITE_API_URL}/api/auth`,
  headers: {
    "Content-type": "application/json"
  }
});