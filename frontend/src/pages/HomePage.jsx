import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Mascot from '../components/Mascot';

export default function HomePage() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="page container">
      <div style={{ maxWidth: 640, margin: '40px 0 60px' }}>
        <Mascot size="lg" say="Let's crack some problems!" />
        <h1 className="display" style={{ fontSize: '2.6rem', lineHeight: 1.15, marginTop: 20 }}>
          Advanced math, <span style={{ color: 'var(--gold)' }}>gamified</span>.
        </h1>
        <p style={{ color: 'var(--chalk-dim)', fontSize: '1.05rem', marginTop: 18, lineHeight: 1.6 }}>
          Calculus, trigonometry, linear algebra, probability, and number theory —
          taught through interactive visualizations and short, XP-earning challenges.
          Build a streak. Level up. Actually understand the math.
        </p>
        <div style={{ marginTop: 28, display: 'flex', gap: 12 }}>
          <Link to="/domains" className="btn btn-primary">
            {isAuthenticated ? 'Continue learning' : 'Explore domains'}
          </Link>
          {!isAuthenticated && (
            <Link to="/register" className="btn btn-ghost">Create an account</Link>
          )}
        </div>
      </div>

      <div className="grid grid-3">
        {[
          { icon: '∫', title: 'Calculus', desc: 'Limits, derivatives, integrals — visualized as slopes and areas.' },
          { icon: '△', title: 'Trigonometry', desc: 'The unit circle made intuitive, angle by angle.' },
          { icon: '⎡⎤', title: 'Linear Algebra', desc: 'Vectors and transformations you can actually see move.' },
          { icon: '🎲', title: 'Probability', desc: 'Distributions and expectation, built from first principles.' },
          { icon: 'ℕ', title: 'Number Theory', desc: 'Primes, divisibility, and modular arithmetic.' },
          { icon: '🏆', title: 'Gamified', desc: 'XP, levels, streaks, and achievements keep you coming back.' },
        ].map((f) => (
          <div className="card" key={f.title}>
            <div className="domain-icon">{f.icon}</div>
            <div className="domain-name display">{f.title}</div>
            <div className="domain-desc">{f.desc}</div>
          </div>
        ))}
      </div>
    </div>
  );
}
