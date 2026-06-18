import { createContext, useContext, useState, useEffect } from 'react';
import { jwtDecode } from 'jwt-decode';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));

  useEffect(() => {
    if (token) {
      try {
        const decoded = jwtDecode(token);
        if (decoded.exp * 1000 < Date.now()) {
          logout();
        } else {
          setUser({ email: decoded.sub, role: decoded.role });
        }
      } catch {
        logout();
      }
    }
  }, [token]);

  const login = (newToken, userData) => {
    localStorage.setItem('token', newToken);
    setToken(newToken);
    const decoded = jwtDecode(newToken);
    setUser({ email: decoded.sub, role: decoded.role, fullName: userData?.fullName });
  };

  const logout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, login, logout, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
