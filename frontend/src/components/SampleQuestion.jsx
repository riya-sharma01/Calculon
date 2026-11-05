import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { getDomainMeta, domainOrder } from '../domainMeta';

// One quick question per domain so a visitor can try the product before signing up.
// These are examples for the home page only; they are not part of the question bank.
const SAMPLES = [
  {
    domain: 'Algebra',
    prompt: 'If x + 1/x = 3, what is x² + 1/x²?',
    options: ['5', '7', '9', '11'],
    correct: 1,
    explanation: 'Square both sides: x² + 2 + 1/x² = 9, so x² + 1/x² = 7.',
  },
  {
    domain: 'Calculus',
    prompt: 'What is the derivative of x² sin x?',
    options: ['2x sin x + x² cos x', '2x cos x', 'x² cos x', '2x sin x − x² cos x'],
    correct: 0,
    explanation: 'Product rule: (x²)′ sin x + x² (sin x)′ = 2x sin x + x² cos x.',
  },
  {
    domain: 'Trigonometry',
    prompt: 'What is sin 30° + cos 60°?',
    options: ['1/2', '1', '√3/2', '√3'],
    correct: 1,
    explanation: 'sin 30° = 1/2 and cos 60° = 1/2, so the sum is 1.',
  },
  {
    domain: 'Coordinate Geometry',
    prompt: 'What is the distance between the points (1, 2) and (4, 6)?',
    options: ['3', '4', '5', '7'],
    correct: 2,
    explanation: '√((4 − 1)² + (6 − 2)²) = √(9 + 16) = 5.',
  },
  {
    domain: 'Vectors, 3D Geometry & Matrices',
    prompt: 'What is the dot product of (1, 2, 3) and (4, −5, 6)?',
    options: ['8', '12', '20', '28'],
    correct: 1,
    explanation: '1·4 + 2·(−5) + 3·6 = 4 − 10 + 18 = 12.',
  },
  {
    domain: 'Probability & Statistics',
    prompt: 'Two fair dice are rolled. What is the probability that the sum is 7?',
    options: ['1/12', '1/9', '1/6', '5/36'],
    correct: 2,
    explanation: '6 of the 36 equally likely outcomes add up to 7, so the probability is 6/36 = 1/6.',
  },
];

// Cycle through the domains in the same order the rest of the site lists them.
const ORDERED = [...SAMPLES].sort((a, b) => domainOrder(a.domain) - domainOrder(b.domain));

const KEYS = ['A', 'B', 'C', 'D'];

export default function SampleQuestion() {
  const { isAuthenticated } = useAuth();
  const [index, setIndex] = useState(0);
  const [picked, setPicked] = useState(null);

  const q = ORDERED[index];
  const meta = getDomainMeta(q.domain);
  const answered = picked !== null;
  const isCorrect = picked === q.correct;

  function next() {
    setIndex((i) => (i + 1) % ORDERED.length);
    setPicked(null);
  }

  return (
    <div className="try-card" style={{ '--tile': meta.color, '--tint': meta.tint }}>
      <div className="try-head">
        <span className="try-label">Try a question</span>
        <span className="chip">{q.domain}</span>
      </div>

      <p className="try-prompt">{q.prompt}</p>

      <div>
        {q.options.map((text, i) => {
          let cls = 'option-btn';
          if (answered) {
            if (i === q.correct) cls += ' correct';
            else if (i === picked) cls += ' incorrect';
            else cls += ' dim';
          }
          return (
            <button key={text} className={cls} disabled={answered} onClick={() => setPicked(i)}>
              <span className="opt-key">{KEYS[i]}</span>
              {text}
            </button>
          );
        })}
      </div>

      <div role="status" aria-live="polite">
        {answered && (
          <div className={`feedback-box ${isCorrect ? 'feedback-correct' : 'feedback-incorrect'}`}>
            <strong>{isCorrect ? 'Correct.' : 'Not quite.'}</strong>
            <div style={{ marginTop: 4 }}>{q.explanation}</div>
          </div>
        )}
      </div>

      <div className="try-foot">
        <p className="try-note">
          {isAuthenticated ? 'Real questions earn XP.' : 'Create an account to earn XP on real questions.'}
        </p>
        {answered && (
          <button className="btn btn-ghost btn-sm" onClick={next}>
            Another question
          </button>
        )}
      </div>
    </div>
  );
}
