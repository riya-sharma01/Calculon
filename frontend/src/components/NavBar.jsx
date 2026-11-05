import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function NavBar() {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <Link to="/" className="brand display">
          Calculon<span className="brand-dot">.</span>
        </Link>

        <nav className="nav-links">
          <NavLink to="/domains" className={({ isActive }) => (isActive ? 'active' : undefined)}>Domains</NavLink>
          {isAuthenticated && (
            <NavLink to="/dashboard" className={({ isActive }) => (isActive ? 'active' : undefined)}>Dashboard</NavLink>
          )}
          <NavLink to="/leaderboard" className={({ isActive }) => (isActive ? 'active' : undefined)}>Leaderboard</NavLink>
        </nav>

        <div className="nav-right">
          {isAuthenticated ? (
            <>
              <div className="stat-pill mono" title="Total XP">
                <span className="stat-icon">✦</span> {user.totalXp}
              </div>
              <div className="stat-pill mono" title="Level">
                Lv. {user.level}
              </div>
              <div className="stat-pill mono" title="Current streak">
                <span className="flame">🔥</span> {user.currentStreak}
              </div>
              <button
                className="btn btn-ghost btn-sm"
                onClick={() => {
                  logout();
                  navigate('/');
                }}
              >
                Log out
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-ghost btn-sm">Log in</Link>
              <Link to="/register" className="btn btn-primary btn-sm">Sign up</Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
