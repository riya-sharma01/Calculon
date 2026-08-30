import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', email: '', password: '', displayName: '' });
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  function update(field) {
    return (e) => setForm((f) => ({ ...f, [field]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await register(form.username, form.email, form.password, form.displayName || form.username);
      navigate('/domains', { replace: true });
    } catch (err) {
      setError(err.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page container">
      <div className="card auth-card">
        <h2 className="display" style={{ marginBottom: 6 }}>Create your account</h2>
        <p style={{ color: 'var(--chalk-dim)', fontSize: '0.9rem', marginBottom: 24 }}>
          Start earning XP today.
        </p>
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="username">Username</label>
            <input id="username" value={form.username} onChange={update('username')} required minLength={3} maxLength={30} />
          </div>
          <div className="field">
            <label htmlFor="displayName">Display name (optional)</label>
            <input id="displayName" value={form.displayName} onChange={update('displayName')} />
          </div>
          <div className="field">
            <label htmlFor="email">Email</label>
            <input id="email" type="email" value={form.email} onChange={update('email')} required />
          </div>
          <div className="field">
            <label htmlFor="password">Password</label>
            <input id="password" type="password" value={form.password} onChange={update('password')} required minLength={8} />
          </div>
          {error && <div className="error-text">{error}</div>}
          <button className="btn btn-primary" type="submit" disabled={loading} style={{ width: '100%', marginTop: 8 }}>
            {loading ? 'Creating account…' : 'Sign up'}
          </button>
        </form>
        <p className="center" style={{ marginTop: 18, fontSize: '0.88rem', color: 'var(--chalk-dim)' }}>
          Already have an account? <Link to="/login" style={{ color: 'var(--teal)' }}>Log in</Link>
        </p>
      </div>
    </div>
  );
}
