import { useState } from 'react';
import { Link } from 'react-router-dom';
import { authApi } from '../api/calculon';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [error, setError] = useState(null);
  const [sent, setSent] = useState(false);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await authApi.forgotPassword(email.trim());
      setSent(true);
    } catch (err) {
      setError(err.message || 'Something went wrong. Please try again.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page container">
      <div className="card auth-card">
        <h2 className="display" style={{ marginBottom: 6 }}>Forgot your password?</h2>
        {sent ? (
          <>
            <div className="feedback-box feedback-correct" style={{ marginTop: 16 }}>
              If an account exists for <strong>{email}</strong>, we have sent a link to reset the password.
              It works once and expires in 30 minutes. Check your spam folder too.
            </div>
            <p className="center" style={{ marginTop: 18, fontSize: '0.88rem' }}>
              <Link to="/login" style={{ color: 'var(--teal)' }}>Back to log in</Link>
            </p>
          </>
        ) : (
          <>
            <p style={{ color: 'var(--chalk-dim)', fontSize: '0.9rem', marginBottom: 24 }}>
              Enter the email you signed up with and we will send you a reset link.
            </p>
            <form onSubmit={handleSubmit}>
              <div className="field">
                <label htmlFor="email">Email</label>
                <input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  autoComplete="email"
                  required
                />
              </div>
              {error && <div className="error-text">{error}</div>}
              <button className="btn btn-primary" type="submit" disabled={loading} style={{ width: '100%', marginTop: 8 }}>
                {loading ? 'Sending… the server may be waking up' : 'Send reset link'}
              </button>
            </form>
            <p className="center" style={{ marginTop: 18, fontSize: '0.88rem' }}>
              <Link to="/login" style={{ color: 'var(--teal)' }}>Back to log in</Link>
            </p>
          </>
        )}
      </div>
    </div>
  );
}
