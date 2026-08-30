import { useEffect, useState } from 'react';
import { leaderboardApi } from '../api/calculon';

export default function LeaderboardPage() {
  const [entries, setEntries] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    leaderboardApi.top20().then(setEntries).catch((err) => setError(err.message));
  }, []);

  return (
    <div className="page container" style={{ maxWidth: 640 }}>
      <div className="page-header">
        <h1 className="display">Leaderboard</h1>
        <p>Top 20 learners by total XP.</p>
      </div>

      {error && <p className="error-text">{error}</p>}
      {!entries && !error && <div className="spinner-text">Loading…</div>}

      {entries && (
        <div className="card" style={{ padding: 0 }}>
          {entries.map((e, i) => (
            <div className="leaderboard-row" key={`${e.displayName}-${i}`}>
              <span className={`rank mono rank-${i + 1 <= 3 ? i + 1 : ''}`}>{i + 1}</span>
              <span style={{ flex: 1 }}>{e.displayName}</span>
              <span className="mono lesson-meta">Lv. {e.level}</span>
              <span className="mono lesson-meta">🔥 {e.currentStreak}</span>
              <span className="mono" style={{ color: 'var(--gold)', minWidth: 70, textAlign: 'right' }}>{e.totalXp} XP</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
