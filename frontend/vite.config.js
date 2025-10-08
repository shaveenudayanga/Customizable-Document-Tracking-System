import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // User service endpoints (authentication)
      "/api/auth": {
        target: "http://localhost:8081",
        changeOrigin: true,
        secure: false,
      },
      // Document service endpoints
      "/api/documents": {
        target: "http://localhost:8082",
        changeOrigin: true,
        secure: false,
      },
      // Workflow service endpoints
      "/api/workflow": {
        target: "http://localhost:8083",
        changeOrigin: true,
        secure: false,
      },
      // Tracking service endpoints
      "/api/tracking": {
        target: "http://localhost:8084",
        changeOrigin: true,
        secure: false,
      },
    },
  },
});
