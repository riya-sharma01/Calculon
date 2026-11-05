import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Mascot from '../components/Mascot';
import SampleQuestion from '../components/SampleQuestion';
import { DOMAIN_META, LEVELS } from '../domainMeta';

export default function HomePage() {
  const { isAuthenticated } = useAuth();

  return (
    <main className="page container">
      <section className="hero">
        <div>
          <Mascot size="lg" say="Let's crack some problems!" />
          <h1 className="display hero-title">Practice math, one level at a time.</h1>
          <p className="lede">
            Questions across six areas of math, from Foundation to Advanced. Earn XP, keep your streak
            going, and read why each answer is right.
          </p>
          <div className="hero-actions">
            <Link to="/domains" className="btn btn-primary">
              {isAuthenticated ? 'Continue practicing' : 'Browse domains'}
            </Link>
            {!isAuthenticated && (
              <Link to="/register" className="btn btn-ghost">Create an account</Link>
            )}
          </div>
        </div>

        <SampleQuestion />
      </section>

      <section className="section">
        <h2 className="display">Six domains to practice</h2>
        <p className="section-sub">Each one has its own color and its own five levels.</p>
        <div className="grid grid-tiles">
          {DOMAIN_META.map((m) => (
            <Link
              to="/domains"
              className="domain-card"
              key={m.name}
              style={{ '--tile': m.color, '--tint': m.tint }}
            >
              <div className={`domain-icon ${m.glyph.length > 2 ? 'glyph-sm' : ''}`} aria-hidden="true">
                {m.glyph}
              </div>
              <h3 className="domain-name">{m.name}</h3>
              <p className="domain-desc">{m.topics}</p>
            </Link>
          ))}
        </div>
      </section>

      <section className="section">
        <h2 className="display">Five levels in every domain</h2>
        <p className="section-sub">Start at Foundation and work your way up as the questions get harder.</p>
        <div className="ladder">
          {LEVELS.map((level, i) => (
            <div className="ladder-step" key={level.key}>
              <span className="level-dots" aria-hidden="true">
                {LEVELS.map((_, d) => <i key={d} className={d <= i ? 'on' : ''} />)}
              </span>
              <span className="ladder-name">{level.label}</span>
              <p className="ladder-blurb">{level.blurb}</p>
            </div>
          ))}
        </div>
      </section>

      {!isAuthenticated && (
        <section className="cta-band">
          <div>
            <h2 className="display">Keep your XP and streak</h2>
            <p>Create a free account and your progress is saved every time you answer.</p>
          </div>
          <Link to="/register" className="btn btn-ghost">Create an account</Link>
        </section>
      )}
    </main>
  );
}
