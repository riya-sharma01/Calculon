import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const from = location.state?.from?.pathname || '/domains';

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login(usernameOrEmail, password);
      navigate(from, { replace: true });
    } catch (err) {
      setError(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page container">
      <div className="card auth-card">
        <h2 className="display" style={{ marginBottom: 6 }}>Welcome back</h2>
        <p style={{ color: 'var(--chalk-dim)', fontSize: '0.9rem', marginBottom: 24 }}>
          Log in to keep your streak alive.
        </p>
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="usernameOrEmail">Username or email</label>
            <input
              id="usernameOrEmail"
              value={usernameOrEmail}
              onChange={(e) => setUsernameOrEmail(e.target.value)}
              autoComplete="username"
              required
            />
          </div>
          <div className="field">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
              required
            />
          </div>
          {error && <div className="error-text">{error}</div>}
          <button className="btn btn-primary" type="submit" disabled={loading} style={{ width: '100%', marginTop: 8 }}>
            {loading ? 'Logging in…' : 'Log in'}
          </button>
        </form>
        <p className="center" style={{ marginTop: 18, fontSize: '0.88rem', color: 'var(--chalk-dim)' }}>
          No account yet? <Link to="/register" style={{ color: 'var(--teal)' }}>Sign up</Link>
        </p>
      </div>
    </div>
  );
}
