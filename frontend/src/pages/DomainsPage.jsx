import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { domainsApi } from '../api/calculon';
import { getDomainMeta, domainOrder } from '../domainMeta';
import Mascot from '../components/Mascot';

export default function DomainsPage() {
  const [domains, setDomains] = useState(null);
  const [error, setError] = useState(null);
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    setError(null);
    domainsApi.list().then(setDomains).catch((err) => setError(err.message));
  }, [attempt]);

  return (
    <main className="page container">
      <div className="page-header" style={{ display: 'flex', alignItems: 'center', gap: 16, flexWrap: 'wrap' }}>
        <Mascot say="Pick your battle!" />
        <div>
          <h1 className="display">Domains</h1>
          <p>Pick an area of math to practice and earn XP.</p>
        </div>
      </div>

      {error && (
        <div>
          <p className="error-text">Couldn&apos;t load the domains: {error}</p>
          <button className="btn btn-ghost btn-sm" style={{ marginTop: 12 }} onClick={() => setAttempt((n) => n + 1)}>
            Try again
          </button>
        </div>
      )}

      {!domains && !error && (
        <div className="spinner-text">
          Loading domains… The first load can take up to a minute while the server wakes up.
        </div>
      )}

      {domains && (
        <div className="grid grid-tiles">
          {[...domains]
            .sort((a, b) => domainOrder(a.name) - domainOrder(b.name) || a.name.localeCompare(b.name))
            .map((d, i) => {
              const m = getDomainMeta(d.name, i);
              return (
                <Link
                  to={`/domains/${d.id}`}
                  className="domain-card"
                  key={d.id}
                  style={{ '--tile': m.color, '--tint': m.tint }}
                >
                  <div className={`domain-icon ${m.glyph.length > 2 ? 'glyph-sm' : ''}`} aria-hidden="true">
                    {m.glyph}
                  </div>
                  <h2 className="domain-name display">{d.name}</h2>
                  <p className="domain-desc">{m.topics || d.description}</p>
                  <div className="domain-foot">Foundation to Advanced</div>
                </Link>
              );
            })}
        </div>
      )}
    </main>
  );
}
