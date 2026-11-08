import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { authApi } from '../api/calculon';

export default function ResetPasswordPage() {
  const [params] = useSearchParams();
  const token = params.get('token') || '';
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [error, setError] = useState(null);
  const [done, setDone] = useState(false);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    if (password.length < 8) {
      setError('Password must be at least 8 characters.');
      return;
    }
    if (password !== confirm) {
      setError('The two passwords do not match.');
      return;
    }
    setLoading(true);
    try {
      await authApi.resetPassword(token, password);
      setDone(true);
    } catch (err) {
      setError(err.message || 'Could not reset the password. Please try again.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page container">
      <div className="card auth-card">
        <h2 className="display" style={{ marginBottom: 6 }}>Choose a new password</h2>
        {!token ? (
          <>
            <div className="error-text" style={{ marginTop: 16 }}>
              This page needs the link from your reset email. Please request a new link.
            </div>
            <p className="center" style={{ marginTop: 18, fontSize: '0.88rem' }}>
              <Link to="/forgot-password" style={{ color: 'var(--teal)' }}>Request a reset link</Link>
            </p>
          </>
        ) : done ? (
          <>
            <div className="feedback-box feedback-correct" style={{ marginTop: 16 }}>
              Your password has been changed. You can log in now.
            </div>
            <Link className="btn btn-primary" to="/login" style={{ width: '100%', marginTop: 16, textAlign: 'center' }}>
              Go to log in
            </Link>
          </>
        ) : (
          <>
            <p style={{ color: 'var(--chalk-dim)', fontSize: '0.9rem', marginBottom: 24 }}>
              Pick something at least 8 characters long.
            </p>
            <form onSubmit={handleSubmit}>
              <div className="field">
                <label htmlFor="newPassword">New password</label>
                <input
                  id="newPassword"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  autoComplete="new-password"
                  required
                />
              </div>
              <div className="field">
                <label htmlFor="confirmPassword">Confirm new password</label>
                <input
                  id="confirmPassword"
                  type="password"
                  value={confirm}
                  onChange={(e) => setConfirm(e.target.value)}
                  autoComplete="new-password"
                  required
                />
              </div>
              {error && <div className="error-text">{error}</div>}
              <button className="btn btn-primary" type="submit" disabled={loading} style={{ width: '100%', marginTop: 8 }}>
                {loading ? 'Saving…' : 'Change password'}
              </button>
            </form>
          </>
        )}
      </div>
    </div>
  );
}
