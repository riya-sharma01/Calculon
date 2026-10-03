import { createContext, useContext, useState, useCallback, useMemo } from 'react';
import { authApi } from '../api/calculon';
import { setToken, getToken } from '../api/client';

const AuthContext = createContext(null);

const USER_KEY = 'calculon_user';

function loadStoredUser() {
  try {
    const raw = sessionStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => (getToken() ? loadStoredUser() : null));

  const persist = useCallback((authResponse) => {
    setToken(authResponse.token);
    sessionStorage.setItem(USER_KEY, JSON.stringify(authResponse.user));
    setUser(authResponse.user);
  }, []);

  const login = useCallback(async (usernameOrEmail, password) => {
    const res = await authApi.login(usernameOrEmail, password);
    persist(res);
    return res.user;
  }, [persist]);

  const register = useCallback(async (username, email, password, displayName) => {
    const res = await authApi.register(username, email, password, displayName);
    persist(res);
    return res.user;
  }, [persist]);

  const logout = useCallback(() => {
    setToken(null);
    sessionStorage.removeItem(USER_KEY);
    setUser(null);
  }, []);

  const updateUserSummary = useCallback((partial) => {
    setUser((prev) => {
      if (!prev) return prev;
      const next = { ...prev, ...partial };
      sessionStorage.setItem(USER_KEY, JSON.stringify(next));
      return next;
    });
  }, []);

  const value = useMemo(
    () => ({ user, login, register, logout, updateUserSummary, isAuthenticated: !!user }),
    [user, login, register, logout, updateUserSummary]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
