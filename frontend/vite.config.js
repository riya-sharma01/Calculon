import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true, // fail instead of silently picking a different port (which would break backend CORS)
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
});
