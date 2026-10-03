import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { progressApi } from '../api/calculon';

export default function DashboardPage() {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    progressApi.dashboard().then(setData).catch((err) => setError(err.message));
  }, []);

  if (error) return <div className="page container"><p className="error-text">{error}</p></div>;
  if (!data) return <div className="page container"><div className="spinner-text">Loading your progress…</div></div>;

  const pct = data.xpNeededForNextLevel > 0
    ? Math.min(100, Math.round((data.xpIntoCurrentLevel / data.xpNeededForNextLevel) * 100))
    : 100;

  return (
    <div className="page container" style={{ maxWidth: 720 }}>
      <div className="page-header">
        <h1 className="display">Your progress</h1>
      </div>

      <div className="card" style={{ marginBottom: 20 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 10 }}>
          <span className="display" style={{ fontSize: '1.3rem' }}>Level {data.level}</span>
          <span className="mono lesson-meta">{data.xpIntoCurrentLevel} / {data.xpNeededForNextLevel} XP</span>
        </div>
        <div className="progress-track">
          <div className="progress-fill" style={{ width: `${pct}%` }} />
        </div>
      </div>

      <div className="grid grid-3" style={{ marginBottom: 20 }}>
        <div className="card center">
          <div className="mono" style={{ fontSize: '1.6rem', color: 'var(--gold)' }}>{data.totalXp}</div>
          <div className="lesson-meta">Total XP</div>
        </div>
        <div className="card center">
          <div className="mono" style={{ fontSize: '1.6rem', color: 'var(--coral)' }}>🔥 {data.currentStreak}</div>
          <div className="lesson-meta">Current streak</div>
        </div>
        <div className="card center">
          <div className="mono" style={{ fontSize: '1.6rem', color: 'var(--teal)' }}>{data.lessonsCompleted}</div>
          <div className="lesson-meta">Lessons completed</div>
        </div>
      </div>

      <div className="card">
        <h3 style={{ marginBottom: 12 }}>Recent achievements</h3>
        {data.recentAchievements?.length ? (
          <ul style={{ margin: 0, paddingLeft: 20, lineHeight: 1.8 }}>
            {data.recentAchievements.map((a) => <li key={a}>{a}</li>)}
          </ul>
        ) : (
          <p className="lesson-meta">Complete a lesson to earn your first achievement.</p>
        )}
      </div>

      <div style={{ marginTop: 24 }}>
        <Link to="/domains" className="btn btn-primary">Keep learning</Link>
      </div>
    </div>
  );
}
