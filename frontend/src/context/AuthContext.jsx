import React, { createContext, useState, useContext } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('linkup_session');
    return saved ? JSON.parse(saved) : null;
  });

  const login = (userData) => {
    setUser(userData);
    localStorage.setItem('linkup_session', JSON.stringify(userData));
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('linkup_session');
  };

  // Custom fetch helper that automatically attaches backend root and X-User-Id header
  const apiFetch = async (path, options = {}) => {
    const backendUrl = 'http://localhost:8080';
    const headers = { ...options.headers };
    
    if (user && user.id) {
      headers['X-User-Id'] = user.id.toString();
    }
    
    // Auto JSON Content-Type unless uploading files via Multipart FormData
    if (!(options.body instanceof FormData) && !headers['Content-Type']) {
      headers['Content-Type'] = 'application/json';
    }

    const response = await fetch(`${backendUrl}${path}`, {
      ...options,
      headers
    });

    if (!response.ok) {
      const errMsg = await response.text();
      throw new Error(errMsg || `Request failed with status ${response.status}`);
    }

    // Try parsing as JSON, fallback to text if empty/non-json
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return await response.json();
    }
    return await response.text();
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, apiFetch }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
