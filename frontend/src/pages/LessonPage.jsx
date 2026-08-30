import { useEffect, useState } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import { lessonsApi, questionsApi, progressApi } from '../api/calculon';
import { useAuth } from '../context/AuthContext';
import VisualizationPanel from '../components/visualizations/VisualizationPanel';
import Confetti from '../components/Confetti';
import Mascot from '../components/Mascot';

export default function LessonPage() {
  const { lessonId } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, updateUserSummary } = useAuth();

  const [lesson, setLesson] = useState(null);
  const [error, setError] = useState(null);

  const [qIndex, setQIndex] = useState(0);
  const [selectedOptionId, setSelectedOptionId] = useState(null);
  const [freeResponse, setFreeResponse] = useState('');
  const [result, setResult] = useState(null); // AnswerResult for current question
  const [submitting, setSubmitting] = useState(false);
  const [correctCount, setCorrectCount] = useState(0);

  const [completion, setCompletion] = useState(null); // LevelUpEvent once lesson finished

  useEffect(() => {
    lessonsApi.detail(lessonId).then(setLesson).catch((err) => setError(err.message));
  }, [lessonId]);

  if (error) return <div className="page container"><p className="error-text">{error}</p></div>;
  if (!lesson) return <div className="page container"><div className="spinner-text">Loading lesson…</div></div>;

  const questions = lesson.questions || [];
  const question = questions[qIndex];
  const isLast = qIndex === questions.length - 1;

  async function handleSubmitAnswer() {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: { pathname: `/lessons/${lessonId}` } } });
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      const res = await questionsApi.answer(question.id, selectedOptionId, freeResponse || undefined);
      setResult(res);
      if (res.correct) setCorrectCount((c) => c + 1);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function handleNext() {
    if (isLast) {
      setSubmitting(true);
      try {
        const scorePercent = Math.round((correctCount / questions.length) * 100);
        const levelUpEvent = await progressApi.completeLesson(lesson.id, scorePercent);
        const dashboard = await progressApi.dashboard();
        updateUserSummary({
          totalXp: dashboard.totalXp,
          level: dashboard.level,
          currentStreak: dashboard.currentStreak,
        });
        setCompletion({ ...levelUpEvent, scorePercent });
      } catch (err) {
        setError(err.message);
      } finally {
        setSubmitting(false);
      }
    } else {
      setQIndex((i) => i + 1);
      setSelectedOptionId(null);
      setFreeResponse('');
      setResult(null);
    }
  }

  return (
    <div className="page container" style={{ maxWidth: 720 }}>
      <Link to="#" onClick={() => navigate(-1)} style={{ color: 'var(--chalk-dim)', fontSize: '0.85rem', textDecoration: 'none' }}>
        ← Back
      </Link>

      <div className="page-header" style={{ marginTop: 10 }}>
        <span className="mono lesson-meta">{lesson.domainName}</span>
        <h1 className="display" style={{ marginTop: 4 }}>{lesson.title}</h1>
        <p>{lesson.summary}</p>
      </div>

      <VisualizationPanel domainName={lesson.domainName} />

      <div className="card" style={{ marginBottom: 28, lineHeight: 1.7 }}>
        {lesson.content}
      </div>

      {completion ? (
        <div className="card center" style={{ position: 'relative', overflow: 'visible' }}>
          <Confetti active={true} />
          <Mascot
            size="lg"
            emoji={completion.scorePercent >= 80 ? '🥳' : completion.scorePercent >= 50 ? '🙂' : '💪'}
            say={
              completion.scorePercent >= 80
                ? 'You crushed it!'
                : completion.scorePercent >= 50
                ? 'Nice work!'
                : 'Good effort — try again!'
            }
          />
          <h2 className="display" style={{ marginTop: 14, marginBottom: 10 }}>Lesson complete!</h2>
          <p style={{ color: 'var(--chalk-dim)', marginBottom: 4 }}>
            Score: {completion.scorePercent}% ({correctCount}/{questions.length} correct)
          </p>
          {completion.leveledUp && (
            <p style={{ color: 'var(--gold)', fontWeight: 700, marginTop: 10 }}>
              🎉 Level up! You're now level {completion.newLevel}.
            </p>
          )}
          {completion.newAchievements?.length > 0 && (
            <p style={{ color: 'var(--teal)', marginTop: 6 }}>
              New achievement{completion.newAchievements.length > 1 ? 's' : ''}: {completion.newAchievements.join(', ')}
            </p>
          )}
          <div style={{ marginTop: 20, display: 'flex', gap: 12, justifyContent: 'center' }}>
            <Link to="/domains" className="btn btn-ghost">More domains</Link>
            <Link to={`/domains`} className="btn btn-primary">Keep learning</Link>
          </div>
        </div>
      ) : questions.length === 0 ? (
        <p style={{ color: 'var(--chalk-dim)' }}>This lesson has no practice questions yet.</p>
      ) : (
        <div className="card">
          <div className="lesson-meta mono" style={{ marginBottom: 10 }}>
            Question {qIndex + 1} of {questions.length}
          </div>
          <h3 style={{ marginBottom: 18 }}>{question.prompt}</h3>

          {question.type === 'MULTIPLE_CHOICE' ? (
            <div>
              {question.options.map((opt) => {
                let cls = 'option-btn';
                if (result) {
                  if (opt.id === selectedOptionId && result.correct) cls += ' correct';
                  else if (opt.id === selectedOptionId && !result.correct) cls += ' incorrect';
                } else if (opt.id === selectedOptionId) {
                  cls += ' selected';
                }
                return (
                  <button
                    key={opt.id}
                    className={cls}
                    disabled={!!result}
                    onClick={() => setSelectedOptionId(opt.id)}
                    style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}
                  >
                    <span>{opt.text}</span>
                    {result && opt.id === selectedOptionId && (
                      <span style={{ color: result.correct ? 'var(--teal)' : 'var(--coral)', fontWeight: 700 }}>
                        {result.correct ? '✓' : '✗'}
                      </span>
                    )}
                  </button>
                );
              })}
            </div>
          ) : (
            <input
              type="text"
              placeholder="Type your answer…"
              value={freeResponse}
              onChange={(e) => setFreeResponse(e.target.value)}
              disabled={!!result}
              style={{ marginBottom: 6 }}
            />
          )}

          {result && (
            <div className={`feedback-box ${result.correct ? 'feedback-correct' : 'feedback-incorrect'}`}>
              <strong>{result.correct ? `Correct! +${result.xpAwarded} XP` : 'Not quite.'}</strong>
              <div style={{ marginTop: 6 }}>{result.correctAnswerExplanation}</div>
            </div>
          )}

          {error && <div className="error-text">{error}</div>}

          <div style={{ marginTop: 16 }}>
            {!result ? (
              <button
                className="btn btn-primary"
                onClick={handleSubmitAnswer}
                disabled={submitting || (question.type === 'MULTIPLE_CHOICE' ? !selectedOptionId : !freeResponse.trim())}
              >
                {isAuthenticated ? (submitting ? 'Checking…' : 'Submit answer') : 'Log in to answer'}
              </button>
            ) : (
              <button className="btn btn-primary" onClick={handleNext} disabled={submitting}>
                {submitting ? 'Saving…' : isLast ? 'Finish lesson' : 'Next question'}
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
