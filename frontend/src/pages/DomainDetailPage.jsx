import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { domainsApi, lessonsApi } from '../api/calculon';
import { getDomainMeta, levelIndex, LEVELS } from '../domainMeta';

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

  const meta = getDomainMeta(domain?.name);

  return (
    <main className="page container">
      <Link to="/domains" className="back-link">← All domains</Link>

      <div className="domain-hero" style={{ '--tile': meta.color, '--tint': meta.tint }}>
        <div className={`domain-icon ${meta.glyph.length > 2 ? 'glyph-sm' : ''}`} aria-hidden="true">
          {meta.glyph}
        </div>
        <div>
          <h1 className="display">{domain?.name || '…'}</h1>
          {domain && <p>{meta.topics || domain.description}</p>}
        </div>
      </div>

      {error && <div className="error-text">{error}</div>}
      {!lessons && !error && <div className="spinner-text">Loading levels…</div>}

      {lessons && lessons.length === 0 && (
        <p className="lesson-meta">No levels in this domain yet. Check back soon.</p>
      )}

      {lessons && lessons.length > 0 && (
        <div className="card lesson-list" style={{ '--tile': meta.color }}>
          {[...lessons]
            .sort((a, b) => (a.sequenceOrder ?? 0) - (b.sequenceOrder ?? 0))
            .map((l, i) => {
              const level = levelIndex(l.difficulty);
              return (
                <Link to={`/lessons/${l.id}`} className="lesson-row" key={l.id}>
                  <span className="lesson-step" aria-hidden="true">{i + 1}</span>
                  <div className="lesson-main">
                    <div className="lesson-title">{l.title}</div>
                    <div className="lesson-meta">{l.summary}</div>
                  </div>
                  <div className="lesson-side">
                    {l.progressStatus === 'COMPLETED' && <span className="badge badge-completed">Done</span>}
                    <span className="badge">
                      <span className="level-dots" aria-hidden="true">
                        {LEVELS.map((_, d) => <i key={d} className={d <= level ? 'on' : ''} />)}
                      </span>
                      {LEVELS[level].label}
                    </span>
                    <span className="mono lesson-meta">+{l.baseXpReward} XP</span>
                  </div>
                </Link>
              );
            })}
        </div>
      )}
    </main>
  );
}
