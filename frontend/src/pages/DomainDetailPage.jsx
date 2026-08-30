import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { domainsApi, lessonsApi } from '../api/calculon';

const DIFF_BADGE = {
  BEGINNER: 'badge-beginner',
  INTERMEDIATE: 'badge-intermediate',
  ADVANCED: 'badge-advanced',
  EXPERT: 'badge-expert',
};

export default function DomainDetailPage() {
  const { domainId } = useParams();
  const [domain, setDomain] = useState(null);
  const [lessons, setLessons] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    setDomain(null);
    setLessons(null);
    setError(null);

    domainsApi.list()
      .then((all) => setDomain(all.find((d) => String(d.id) === domainId) || null))
      .catch((err) => setError(err.message));

    lessonsApi.byDomain(domainId)
      .then(setLessons)
      .catch((err) => setError(err.message));
  }, [domainId]);

  return (
    <div className="page container">
      <div className="page-header">
        <Link to="/domains" style={{ color: 'var(--chalk-dim)', fontSize: '0.85rem', textDecoration: 'none' }}>
          ← All domains
        </Link>
        <h1 className="display" style={{ marginTop: 10 }}>{domain?.name || '…'}</h1>
        {domain && <p>{domain.description}</p>}
      </div>

      {error && <div className="error-text">{error}</div>}
      {!lessons && !error && <div className="spinner-text">Loading lessons…</div>}

      {lessons && lessons.length === 0 && (
        <p style={{ color: 'var(--chalk-dim)' }}>No lessons yet in this domain — check back soon.</p>
      )}

      {lessons && lessons.length > 0 && (
        <div className="card" style={{ padding: 0 }}>
          {lessons
            .sort((a, b) => (a.sequenceOrder ?? 0) - (b.sequenceOrder ?? 0))
            .map((l) => (
              <Link to={`/lessons/${l.id}`} className="lesson-row" key={l.id}>
                <div>
                  <div className="lesson-title">{l.title}</div>
                  <div className="lesson-meta">{l.summary}</div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                  {l.progressStatus === 'COMPLETED' && <span className="badge badge-completed">Done</span>}
                  <span className={`badge ${DIFF_BADGE[l.difficulty] || ''}`}>{l.difficulty}</span>
                  <span className="mono lesson-meta">+{l.baseXpReward} XP</span>
                </div>
              </Link>
            ))}
        </div>
      )}
    </div>
  );
}
