import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { domainsApi } from '../api/calculon';
import Mascot from '../components/Mascot';

const ICONS = {
  'function-grapher': '📈',
  'unit-circle': '🌀',
  'vector-space': '➡️',
  'distribution-plot': '🎲',
  'number-line': '🔢',
};

const ACCENTS = {
  'function-grapher': { bg: 'rgba(255,201,60,0.18)', fg: '#ffc93c' },
  'unit-circle': { bg: 'rgba(45,226,201,0.18)', fg: '#2de2c9' },
  'vector-space': { bg: 'rgba(167,139,250,0.18)', fg: '#a78bfa' },
  'distribution-plot': { bg: 'rgba(255,107,139,0.18)', fg: '#ff6b8b' },
  'number-line': { bg: 'rgba(163,255,95,0.18)', fg: '#a3ff5f' },
};

export default function DomainsPage() {
  const [domains, setDomains] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    domainsApi.list().then(setDomains).catch((err) => setError(err.message));
  }, []);

  return (
    <div className="page container">
      <div className="page-header" style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
        <Mascot say="Pick your battle!" />
        <div>
          <h1 className="display">Domains</h1>
          <p>Pick an area of mathematics to start earning XP.</p>
        </div>
      </div>

      {error && <div className="error-text">{error}</div>}
      {!domains && !error && <div className="spinner-text">Loading domains…</div>}

      {domains && (
        <div className="grid grid-3">
          {domains
            .sort((a, b) => (a.displayOrder ?? 0) - (b.displayOrder ?? 0))
            .map((d) => {
              const accent = ACCENTS[d.visualizationType] || { bg: 'rgba(255,255,255,0.08)', fg: 'var(--chalk)' };
              return (
                <Link to={`/domains/${d.id}`} className="card domain-card" key={d.id}>
                  <div className="domain-icon" style={{ background: accent.bg, color: accent.fg }}>
                    {ICONS[d.visualizationType] || '✦'}
                  </div>
                  <div className="domain-name display">{d.name}</div>
                  <div className="domain-desc">{d.description}</div>
                </Link>
              );
            })}
        </div>
      )}
    </div>
  );
}

