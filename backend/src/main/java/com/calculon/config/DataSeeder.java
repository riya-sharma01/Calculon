package com.calculon.config;

import com.calculon.entity.*;
import com.calculon.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Locale;

/** Seeds domains, lessons, and questions so the app has real content on first run (dev profile only). */
@Component
public class DataSeeder implements CommandLineRunner {

    private final MathDomainRepository domainRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final AchievementRepository achievementRepository;

    public DataSeeder(MathDomainRepository domainRepository, LessonRepository lessonRepository,
                       QuestionRepository questionRepository, AchievementRepository achievementRepository) {
        this.domainRepository = domainRepository;
        this.lessonRepository = lessonRepository;
        this.questionRepository = questionRepository;
        this.achievementRepository = achievementRepository;
    }

    @Override
    public void run(String... args) {
        if (domainRepository.count() > 0) return;

        seedCalculus();
        seedTrigonometry();
        seedLinearAlgebra();
        seedProbability();
        seedNumberTheory();
        seedAchievements();
    }

    // ---------------------------------------------------------------- Calculus

    private void seedCalculus() {
        MathDomain calculus = domain("Calculus", "calculus", "Limits, derivatives, and integrals.", "function-grapher");

        Lesson derivatives = lesson(calculus, "Introduction to Derivatives",
                "What a derivative means, geometrically and algebraically.",
                "A derivative measures how a function's output changes as its input changes — the slope of the "
                        + "tangent line at a point. For f(x) = x^n, the power rule gives f'(x) = n*x^(n-1). "
                        + "Drag the point on the graph above to watch the tangent line's slope change.",
                Lesson.Difficulty.BEGINNER, 1, 50);

        question(derivatives, "What does the derivative of a function represent at a point?",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "The derivative is the instantaneous rate of change — the slope of the tangent line.", 10,
                opt("The area under the curve", false),
                opt("The slope of the tangent line", true),
                opt("The function's maximum value", false));

        question(derivatives, "Using the power rule, what is d/dx of x^2? (format: coefficient·x^power, no spaces, e.g. 4x^3)",
                Question.QuestionType.EXPRESSION, "2x",
                "Power rule: d/dx x^n = n*x^(n-1), so d/dx x^2 = 2x.", 10);

        question(derivatives, "Using the power rule, what is d/dx of x^3? (format: coefficient·x^power, no spaces, e.g. 4x^3)",
                Question.QuestionType.EXPRESSION, "3x^2",
                "Power rule: d/dx x^3 = 3*x^(3-1) = 3x^2.", 10);

        question(derivatives, "What is the derivative of any constant, e.g. d/dx of 7?",
                Question.QuestionType.NUMERIC, "0",
                "A constant never changes, so its rate of change — its derivative — is always 0.", 10);

        question(derivatives, "If f(x) = 5x, what is f'(x)?",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "For a linear function f(x) = mx, the slope is constant and equal to m, so f'(x) = 5.", 10,
                opt("5", true),
                opt("5x", false),
                opt("x", false));

        Lesson integrals = lesson(calculus, "Introduction to Integrals",
                "The reverse of differentiation, and the area under a curve.",
                "An integral accumulates infinitesimal quantities — geometrically, it's the area between a curve "
                        + "and the x-axis. Integration and differentiation are inverse operations: the power rule for "
                        + "integrals is the integral of x^n = x^(n+1)/(n+1) + C.",
                Lesson.Difficulty.INTERMEDIATE, 2, 60);

        question(integrals, "What does a definite integral of f(x) from a to b represent geometrically?",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "A definite integral computes the signed area between the curve and the x-axis over [a, b].", 12,
                opt("The signed area under the curve between a and b", true),
                opt("The slope of the curve at x = a", false),
                opt("The maximum value of the function on [a, b]", false));

        question(integrals, "What is the integral of x with respect to x? (format: a fraction like x^n/n, no spaces)",
                Question.QuestionType.EXPRESSION, "x^2/2",
                "Reverse power rule: integral of x^1 dx = x^2/2 + C (omitting the constant here).", 12);

        question(integrals, "What is the value of the integral of 1 dx from 0 to 5?",
                Question.QuestionType.NUMERIC, "5",
                "Integrating a constant 1 over [0, 5] gives the area of a 5-by-1 rectangle: 5.", 12);

        question(integrals, "Differentiation and integration are best described as:",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "By the Fundamental Theorem of Calculus, integration and differentiation undo one another.", 10,
                opt("Inverse operations of one another", true),
                opt("Unrelated operations", false),
                opt("The same operation applied twice", false));

        question(integrals, "What is the integral of x^2 dx from 0 to 3?",
                Question.QuestionType.NUMERIC, "9",
                "Integral of x^2 is x^3/3. Evaluated from 0 to 3: 27/3 - 0 = 9.", 15);

        Lesson chainRule = lesson(calculus, "Chain Rule & Optimization",
                "Differentiating composite functions, and finding maxima and minima.",
                "The chain rule handles composite functions: d/dx f(g(x)) = f'(g(x)) × g'(x). Optimization "
                        + "problems use derivatives to find where a function is largest or smallest — critical "
                        + "points occur where f'(x) = 0.",
                Lesson.Difficulty.ADVANCED, 3, 80);

        question(chainRule, "Using the chain rule, what is d/dx of (x^2 + 1)^3? (format: coefficient·x·(...)^power, no spaces)",
                Question.QuestionType.EXPRESSION, "6x(x^2+1)^2",
                "Chain rule: d/dx (x^2+1)^3 = 3(x^2+1)^2 × 2x = 6x(x^2+1)^2.", 15);

        question(chainRule, "What is d/dx of sin(2x)? (format: coefficient·trig(expression), no spaces)",
                Question.QuestionType.EXPRESSION, "2cos(2x)",
                "Chain rule: the derivative of sin(u) is cos(u)·u'. With u = 2x, u' = 2, so d/dx sin(2x) = 2cos(2x).", 15);

        question(chainRule, "At a local maximum or minimum of a differentiable function, f'(x) equals:",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "Critical points where the tangent line is flat — where f'(x) = 0 — are candidates for local extrema.", 12,
                opt("0", true),
                opt("1", false),
                opt("Undefined, always", false));

        question(chainRule, "For f(x) = x^2 - 4x + 3, at what x-value is the minimum?",
                Question.QuestionType.NUMERIC, "2",
                "f'(x) = 2x - 4. Setting f'(x) = 0 gives x = 2, which is the vertex/minimum of this upward parabola.", 18);

        question(chainRule, "The second derivative f''(x) at a critical point being positive indicates:",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "A positive second derivative means the function curves upward there — a local minimum (concave up).", 15,
                opt("A local minimum", true),
                opt("A local maximum", false),
                opt("An inflection point", false));

        question(chainRule, "What is d/dx of e^(3x)? (format: coefficient·e^(expression), no spaces)",
                Question.QuestionType.EXPRESSION, "3e^(3x)",
                "Chain rule with u = 3x: d/dx e^u = e^u · u' = 3e^(3x).", 18);

        // ---- Practice Lab: large auto-generated drill set ----
        Lesson calcLab = lesson(calculus, "Practice Lab: Derivatives Drill",
                "A large set of power-rule and tangent-slope questions for repeated practice.",
                "This practice lab pulls from the power rule to build fluency: differentiating x^n for many "
                        + "values of n, and computing tangent slopes at many points on x^2. There's no penalty "
                        + "for retrying — work through as many as you like.",
                Lesson.Difficulty.EXPERT, 4, 10);

        for (int n = 2; n <= 19; n++) {
            String correctDeriv = n + "x^" + (n - 1);
            question(calcLab, "Which expression is the derivative of x^" + n + "?",
                    Question.QuestionType.MULTIPLE_CHOICE, null,
                    "Power rule: d/dx x^n = n·x^(n-1), so d/dx x^" + n + " = " + correctDeriv + ".", 8,
                    opt(correctDeriv, true),
                    opt("x^" + (n - 1), false),
                    opt((n - 1) + "x^" + n, false));
        }

        for (int n = 2; n <= 19; n++) {
            String correctDeriv = n + "x^" + (n - 1);
            question(calcLab, "What is d/dx of x^" + n + "? (format: coefficient·x^power, e.g. 5x^4 — no spaces)",
                    Question.QuestionType.EXPRESSION, correctDeriv,
                    "Power rule: d/dx x^n = n·x^(n-1), so d/dx x^" + n + " = " + correctDeriv + ".", 8);
        }

        for (int k = 1; k <= 17; k++) {
            int slope = 2 * k;
            question(calcLab, "What is the slope of the tangent to f(x) = x^2 at x = " + k + "? (i.e., f'(" + k + "))",
                    Question.QuestionType.NUMERIC, String.valueOf(slope),
                    "f'(x) = 2x, so at x = " + k + ", the slope is 2×" + k + " = " + slope + ".", 8);
        }

        // ---- JEE Practice: genuinely exam-caliber, multi-step calculus ----
        Lesson calcJee = lesson(calculus, "JEE Practice: Limits, Derivatives & Integrals",
                "Multi-step problems using real JEE Main/Advanced techniques — not just plug-and-chug.",
                "These require combining techniques the way JEE actually does: implicit differentiation with "
                        + "the product rule, related rates using a changing right triangle, optimization by "
                        + "solving V'(x)=0 for a physical constraint, integration by parts, and integration by "
                        + "substitution. Each needs at least two steps of real reasoning, not just one formula.",
                Lesson.Difficulty.EXPERT, 5, 100);

        // A. Implicit differentiation: x^2*y + x*y^2 = c, find dy/dx at (x0,y0)
        for (int i = 1; i <= 20; i++) {
            int x0 = (i % 5) + 2;
            int y0 = (i % 4) + 1;
            long c = (long) x0 * x0 * y0 + (long) x0 * y0 * y0;
            long num = -(2L * x0 * y0 + (long) y0 * y0);
            long den = (long) x0 * x0 + 2L * x0 * y0;
            long g = gcd((int) num, (int) den);
            long n2 = num / g, d2 = den / g;
            if (d2 < 0) { n2 = -n2; d2 = -d2; }
            String ans = n2 + "/" + d2;
            question(calcJee, "The curve x²y + xy² = " + c + " passes through (" + x0 + ", " + y0
                            + "). Using implicit differentiation, what is dy/dx at that point? (enter as a reduced fraction)",
                    Question.QuestionType.EXPRESSION, ans,
                    "Differentiating implicitly: 2xy + x²(dy/dx) + y² + 2xy(dy/dx) = 0, so dy/dx = "
                            + "-(2xy+y²)/(x²+2xy). At (" + x0 + "," + y0 + "), that's " + ans + ".", 20);
        }

        // B. Related rates: ladder sliding down a wall (Pythagorean triples for clean numbers)
        int[][] triples = {{3,4,5},{6,8,10},{5,12,13},{8,15,17},{7,24,25},{9,12,15},{20,21,29},{12,16,20},{9,40,41},{12,35,37}};
        for (int i = 1; i <= 20; i++) {
            int[] t = triples[i % triples.length];
            int xL = t[0], yL = t[1], L = t[2];
            int r = (i % 4) + 1;
            long num = -(long) xL * r;
            long den = yL;
            long g = gcd((int) num, (int) den);
            long n2 = num / g, d2 = den / g;
            if (d2 < 0) { n2 = -n2; d2 = -d2; }
            String ans = n2 + "/" + d2;
            question(calcJee, "A " + L + "-meter ladder leans against a wall. Its base is " + xL
                            + " m from the wall and slides away at " + r + " m/s. At this instant, how fast "
                            + "is the top of the ladder sliding down? (dy/dt, enter as a reduced fraction of m/s, negative = moving down)",
                    Question.QuestionType.EXPRESSION, ans,
                    "x²+y²=L² gives 2x(dx/dt)+2y(dy/dt)=0, so dy/dt = -(x/y)(dx/dt). With x=" + xL + ", y="
                            + yL + " (since " + xL + "²+" + yL + "²=" + L + "²), and dx/dt=" + r
                            + ", dy/dt = " + ans + " m/s.", 20);
        }

        // C. Optimization: open-top box from a p×q sheet, maximize volume
        for (int i = 1; i <= 20; i++) {
            int p = (i % 6) + 8;
            int q = (i % 5) + 6;
            double disc = p * p - (double) p * q + q * q;
            double xcrit = (p + q) / 6.0 - Math.sqrt(disc) / 6.0;
            String ans = String.format(Locale.US, "%.2f", xcrit);
            question(calcJee, "A rectangular sheet of metal is " + p + " cm by " + q + " cm. Equal squares of "
                            + "side x are cut from each corner and the sides folded up to form an open box. "
                            + "What value of x (in cm, rounded to 2 decimal places) maximizes the box's volume?",
                    Question.QuestionType.NUMERIC, ans,
                    "V(x) = x(" + p + "-2x)(" + q + "-2x). Setting V'(x)=0 gives 12x² - 4(" + p + "+" + q
                            + ")x + " + p + "×" + q + " = 0. The root in the valid range is x ≈ " + ans + " cm.", 22);
        }

        // D. Integration by parts: ∫ x·e^(kx) dx from 0 to a
        for (int i = 1; i <= 20; i++) {
            double k = 0.2 + (i % 5) * 0.15;
            int a = (i % 4) + 1;
            double val = ((a * k - 1) * Math.exp(a * k) + 1) / (k * k);
            String ans = String.format(Locale.US, "%.2f", val);
            String kStr = String.format(Locale.US, "%.2f", k);
            question(calcJee, "Using integration by parts, evaluate ∫₀^" + a + " x·e^(" + kStr
                            + "x) dx, rounded to 2 decimal places",
                    Question.QuestionType.NUMERIC, ans,
                    "With u=x, dv=e^(kx)dx: ∫x·e^(kx)dx = (x/k)e^(kx) - (1/k²)e^(kx). Evaluating from 0 to "
                            + a + " with k=" + kStr + " gives ≈ " + ans + ".", 22);
        }

        // E. Substitution: ∫ x/(x²+b²) dx from 0 to a
        for (int i = 1; i <= 20; i++) {
            int a = (i % 5) + 2;
            int b = (i % 4) + 1;
            double val = 0.5 * Math.log((double) (a * a + b * b) / (b * b));
            String ans = String.format(Locale.US, "%.4f", val);
            question(calcJee, "Using the substitution u = x²+" + (b * b) + ", evaluate ∫₀^" + a + " x/(x²+"
                            + (b * b) + ") dx, rounded to 4 decimal places",
                    Question.QuestionType.NUMERIC, ans,
                    "With u=x²+b², du=2x dx: the integral becomes (1/2)∫du/u = (1/2)ln(u). Evaluating from "
                            + "x=0 to x=" + a + " (with b=" + b + ") gives (1/2)ln((" + a + "²+" + b + "²)/"
                            + b + "²) ≈ " + ans + ".", 22);
        }
    }

    // ---------------------------------------------------------------- Trigonometry

    private void seedTrigonometry() {
        MathDomain trig = domain("Trigonometry", "trigonometry", "Angles, the unit circle, and periodic functions.", "unit-circle");

        Lesson unitCircle = lesson(trig, "The Unit Circle",
                "How sine and cosine fall out of a circle of radius 1.",
                "The unit circle links angles to (cos, sin) coordinates, giving trig functions a geometric home. "
                        + "For any angle θ, the point on the circle is (cos θ, sin θ). Drag the slider above to see "
                        + "how the point moves as θ changes.",
                Lesson.Difficulty.BEGINNER, 1, 50);

        question(unitCircle, "On the unit circle, what is sin(90°)?",
                Question.QuestionType.NUMERIC, "1",
                "At 90 degrees the point on the unit circle is (0, 1), so sin(90°) = 1.", 10);

        question(unitCircle, "On the unit circle, what is cos(0°)?",
                Question.QuestionType.NUMERIC, "1",
                "At 0 degrees the point on the unit circle is (1, 0), so cos(0°) = 1.", 10);

        question(unitCircle, "What is sin(180°)?",
                Question.QuestionType.NUMERIC, "0",
                "At 180 degrees the point on the unit circle is (-1, 0), so sin(180°) = 0.", 10);

        question(unitCircle, "Which identity always holds for any angle θ on the unit circle?",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "Since (cos θ, sin θ) lies on a circle of radius 1, cos²θ + sin²θ = 1 by the Pythagorean theorem.", 10,
                opt("cos²θ + sin²θ = 1", true),
                opt("cos θ + sin θ = 1", false),
                opt("cos θ × sin θ = 1", false));

        question(unitCircle, "In which quadrant is an angle of 200° located?",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "200° is between 180° and 270°, placing it in the third quadrant where both sine and cosine are negative.", 10,
                opt("Quadrant I", false),
                opt("Quadrant II", false),
                opt("Quadrant III", true));

        question(unitCircle, "What are the coordinates of the point on the unit circle at θ = 0°? (format: (x,y), no spaces)",
                Question.QuestionType.EXPRESSION, "(1,0)",
                "At θ = 0°, cos(0°) = 1 and sin(0°) = 0, so the point is (1, 0).", 10);

        question(unitCircle, "What is tan(45°)?",
                Question.QuestionType.NUMERIC, "1",
                "tan(θ) = sin(θ)/cos(θ). At 45°, sin and cos are equal, so tan(45°) = 1.", 12);

        Lesson identities = lesson(trig, "Trig Identities & Periodicity",
                "Core identities and why trig functions repeat forever.",
                "Sine and cosine are periodic with period 2π (360°): sin(θ + 360°) = sin(θ). Key identities like "
                        + "sin(-θ) = -sin(θ) and cos(-θ) = cos(θ) follow directly from the symmetry of the circle.",
                Lesson.Difficulty.INTERMEDIATE, 2, 60);

        question(identities, "What is the period of sin(θ), in degrees?",
                Question.QuestionType.NUMERIC, "360",
                "sin(θ) completes one full cycle every 360°, so its period is 360°.", 10);

        question(identities, "Is sine an even or odd function? (sin(-θ) = ?, enter as -sin(θ))",
                Question.QuestionType.EXPRESSION, "-sin(θ)",
                "Sine is an odd function: reflecting the angle across the x-axis flips the y-coordinate, so sin(-θ) = -sin(θ).", 12);

        question(identities, "What is cos(-θ) in terms of cos(θ)? (express your answer in terms of cos(θ), no spaces)",
                Question.QuestionType.EXPRESSION, "cos(θ)",
                "Cosine is an even function: cos(-θ) = cos(θ), since reflecting the angle doesn't change the x-coordinate.", 12);

        question(identities, "Which of these is a Pythagorean-style identity involving tangent?",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "Dividing cos²θ + sin²θ = 1 by cos²θ gives 1 + tan²θ = sec²θ.", 12,
                opt("1 + tan²θ = sec²θ", true),
                opt("tan²θ - 1 = sec²θ", false),
                opt("tan θ = sin θ + cos θ", false));

        Lesson triangles = lesson(trig, "Law of Sines & Cosines",
                "Solving triangles that aren't right triangles.",
                "For any triangle with sides a, b, c and opposite angles A, B, C: the Law of Sines states "
                        + "a/sin(A) = b/sin(B) = c/sin(C), and the Law of Cosines states c² = a² + b² - 2ab·cos(C) — "
                        + "a generalization of the Pythagorean theorem.",
                Lesson.Difficulty.ADVANCED, 3, 80);

        question(triangles, "The Law of Cosines generalizes which theorem?",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "When C = 90°, cos(C) = 0 and the Law of Cosines reduces exactly to c² = a² + b², the Pythagorean theorem.", 12,
                opt("The Pythagorean theorem", true),
                opt("The Fundamental Theorem of Calculus", false),
                opt("The Binomial theorem", false));

        question(triangles, "In a triangle where C = 90°, cos(C) equals:",
                Question.QuestionType.NUMERIC, "0",
                "cos(90°) = 0, which is exactly why the Law of Cosines collapses to the Pythagorean theorem at a right angle.", 10);

        question(triangles, "Using the Law of Sines, if a = 10, A = 30°, and B = 45°, set up the ratio for b. (format: a/sin(A)=b/sin(B), using the given values, no spaces)",
                Question.QuestionType.EXPRESSION, "10/sin(30)=b/sin(45)",
                "The Law of Sines keeps the ratio side/sin(opposite angle) constant across the triangle: a/sin(A) = b/sin(B).", 18);

        question(triangles, "For a triangle with sides a=7, b=24, c=25, is it a right triangle? (enter as yes or no)",
                Question.QuestionType.EXPRESSION, "yes",
                "7² + 24² = 49 + 576 = 625 = 25², so by the converse of the Pythagorean theorem this is a right triangle.", 15);

        question(triangles, "The Law of Sines is most useful when you know:",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "The Law of Sines directly relates angles to their opposite sides, so it's ideal when you have an angle-side pair plus one more piece of information.", 12,
                opt("Two angles and one side (AAS or ASA)", true),
                opt("Three sides only (SSS)", false),
                opt("Two sides and the included angle only (SAS)", false));

        // ---- Practice Lab: large auto-generated drill set ----
        Lesson trigLabBasic = lesson(trig, "Practice Lab: Angles & Identities Drill",
                "A large set of quadrant, sine-value, and identity questions for repeated practice.",
                "This practice lab drills angle location, sine values, and the odd symmetry of sine across "
                        + "many angles. There's no penalty for retrying — work through as many as you like.",
                Lesson.Difficulty.EXPERT, 4, 10);

        String[] quadrantLabels = {"Quadrant I", "Quadrant II", "Quadrant III", "Quadrant IV"};
        for (int deg = 10; deg <= 350; deg += 20) {
            if (deg % 90 == 0) continue;
            int q = quadrantOf(deg);
            int w1 = q % 4 + 1;
            int w2 = (q + 1) % 4 + 1;
            question(trigLabBasic, "In which quadrant does an angle of " + deg + "° lie?",
                    Question.QuestionType.MULTIPLE_CHOICE, null,
                    "An angle of " + deg + "° falls between " + ((q - 1) * 90) + "° and " + (q * 90)
                            + "°, placing it in " + quadrantLabels[q - 1] + ".", 8,
                    opt(quadrantLabels[q - 1], true),
                    opt(quadrantLabels[w1 - 1], false),
                    opt(quadrantLabels[w2 - 1], false));
        }

        for (int deg = 0; deg <= 240; deg += 15) {
            String ans = String.format(Locale.US, "%.2f", Math.sin(Math.toRadians(deg)));
            question(trigLabBasic, "What is sin(" + deg + "°), rounded to 2 decimal places?",
                    Question.QuestionType.NUMERIC, ans,
                    "sin(" + deg + "°) ≈ " + ans + " (rounded to 2 decimal places).", 8);
        }

        for (int deg = 10; deg <= 170; deg += 10) {
            question(trigLabBasic, "What is sin(-" + deg + "°) in terms of sin(" + deg + "°)?",
                    Question.QuestionType.EXPRESSION, "-sin(" + deg + "°)",
                    "Sine is an odd function: sin(-θ) = -sin(θ), so sin(-" + deg + "°) = -sin(" + deg + "°).", 8);
        }

        // ---- JEE Practice: genuinely exam-caliber trigonometry ----
        Lesson trigJee = lesson(trig, "JEE Practice: Advanced Techniques",
                "Multi-step problems using real JEE Main/Advanced trig techniques.",
                "These cover the auxiliary-angle (R-method) technique, solving for an unknown angle in a "
                        + "triangle via the Law of Cosines, reasoning about how many solutions a trig equation "
                        + "has, summing a series of angles in arithmetic progression, and inverse-trig identities "
                        + "— all genuinely multi-step JEE staples, not single-formula lookups.",
                Lesson.Difficulty.EXPERT, 5, 100);

        // A. R-method: a cos(theta) + b sin(theta) = R cos(theta - phi), R = sqrt(a^2+b^2)
        for (int i = 1; i <= 20; i++) {
            int a = (i % 5) + 3;
            int b = (i % 4) + 2;
            String ans = String.format(Locale.US, "%.2f", Math.sqrt(a * a + b * b));
            question(trigJee, "Express " + a + "cos(θ) + " + b + "sin(θ) in the form R·cos(θ-φ). What is R, "
                            + "rounded to 2 decimal places?",
                    Question.QuestionType.NUMERIC, ans,
                    "For a·cosθ + b·sinθ = R·cos(θ-φ), the amplitude is R = √(a²+b²) = √(" + a + "²+" + b
                            + "²) ≈ " + ans + ".", 15);
        }

        // B. Law of cosines: find an angle given 3 sides
        for (int i = 1; i <= 20; i++) {
            int a = (i % 6) + 5;
            int b = (i % 5) + 6;
            int c = (i % 4) + 7;
            if (!(a + b > c && b + c > a && a + c > b)) c = a + b - 1;
            double cosC = (a * a + b * b - (double) c * c) / (2.0 * a * b);
            cosC = Math.max(-1, Math.min(1, cosC));
            double angC = Math.toDegrees(Math.acos(cosC));
            String ans = String.format(Locale.US, "%.2f", angC);
            question(trigJee, "A triangle has sides a=" + a + ", b=" + b + ", c=" + c
                            + ". Using the Law of Cosines, find angle C (opposite side c), in degrees, rounded to 2 decimal places",
                    Question.QuestionType.NUMERIC, ans,
                    "Law of Cosines: c² = a²+b²-2ab·cosC, so cosC = (a²+b²-c²)/(2ab). With a=" + a + ", b="
                            + b + ", c=" + c + ", angle C ≈ " + ans + "°.", 18);
        }

        // C. Number of solutions to sin(x) = k in [0, 2π)
        for (int i = 1; i <= 20; i++) {
            double kval = Math.round((-1.2 + (i % 13) * 0.2) * 100.0) / 100.0;
            int nSol;
            if (Math.abs(kval) > 1.001) nSol = 0;
            else if (Math.abs(Math.abs(kval) - 1) < 1e-9) nSol = 1;
            else nSol = 2;
            String kStr = String.format(Locale.US, "%.2f", kval);
            question(trigJee, "How many solutions does sin(x) = " + kStr + " have for x in [0, 2π)?",
                    Question.QuestionType.MULTIPLE_CHOICE, null,
                    "sin(x) has range [-1,1]. If |k|>1 there are 0 solutions; if |k|=1 there's exactly 1 "
                            + "(at the peak/trough); otherwise sin(x)=k has exactly 2 solutions in one period. "
                            + "Here k=" + kStr + ", giving " + nSol + " solution(s).", 15,
                    opt("0", nSol == 0),
                    opt("1", nSol == 1),
                    opt("2", nSol == 2));
        }

        // D. Sum of a sine series in arithmetic progression of angles
        for (int i = 1; i <= 20; i++) {
            int thetaDeg = (i % 6) * 10 + 10;
            int phiDeg = (i % 5) * 8 + 10;
            int n = (i % 4) + 2;
            double theta = Math.toRadians(thetaDeg);
            double phi = Math.toRadians(phiDeg);
            double val = Math.sin(n * phi / 2) / Math.sin(phi / 2) * Math.sin(theta + (n - 1) * phi / 2);
            String ans = String.format(Locale.US, "%.2f", val);
            question(trigJee, "Evaluate sin(" + thetaDeg + "°) + sin(" + thetaDeg + "°+" + phiDeg + "°) + ... + "
                            + n + " terms total (each term " + phiDeg + "° apart), rounded to 2 decimal places",
                    Question.QuestionType.NUMERIC, ans,
                    "The sum of n terms of sin(θ), sin(θ+φ), ..., sin(θ+(n-1)φ) equals "
                            + "[sin(nφ/2)/sin(φ/2)]·sin(θ+(n-1)φ/2). With θ=" + thetaDeg + "°, φ=" + phiDeg
                            + "°, n=" + n + ", the sum ≈ " + ans + ".", 22);
        }

        // E. Inverse trig double-angle identity: cos(2·sin⁻¹(x)) = 1-2x²
        for (int i = 1; i <= 20; i++) {
            double xv = Math.round((-0.9 + (i % 19) * 0.1) * 100.0) / 100.0;
            if (Math.abs(xv) > 1) xv = 0.5;
            double val = 1 - 2 * xv * xv;
            String ans = String.format(Locale.US, "%.2f", val);
            String xStr = String.format(Locale.US, "%.2f", xv);
            question(trigJee, "What is cos(2·sin⁻¹(" + xStr + ")), rounded to 2 decimal places?",
                    Question.QuestionType.NUMERIC, ans,
                    "Using the identity cos(2·sin⁻¹(x)) = 1-2x² (from cos(2α)=1-2sin²α with α=sin⁻¹(x)), "
                            + "with x=" + xStr + ", the value is " + ans + ".", 18);
        }
    }

    // ---------------------------------------------------------------- Linear Algebra

    private void seedLinearAlgebra() {
        MathDomain linalg = domain("Linear Algebra", "linear-algebra", "Vectors, matrices, and transformations.", "vector-space");

        Lesson vectors = lesson(linalg, "Vectors & Vector Spaces",
                "Magnitude, direction, and the rules vectors follow.",
                "A vector space is a set of objects (vectors) that can be added together and scaled, following a "
                        + "small set of axioms. Adjust the two vectors above and watch v1 + v2 form the diagonal of "
                        + "a parallelogram.",
                Lesson.Difficulty.BEGINNER, 1, 50);

        question(vectors, "Which operation is NOT a vector space axiom requirement?",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "Vector spaces require closure under addition and scalar multiplication, but not division between vectors.",
                10,
                opt("Closure under addition", false),
                opt("Closure under scalar multiplication", false),
                opt("Division between two vectors", true));

        question(vectors, "What is the magnitude of the vector (3, 4)?",
                Question.QuestionType.NUMERIC, "5",
                "Magnitude = sqrt(3² + 4²) = sqrt(9 + 16) = sqrt(25) = 5.", 10);

        question(vectors, "If v1 = (1, 2) and v2 = (3, -1), what is v1 + v2? (format: (x,y), no spaces)",
                Question.QuestionType.EXPRESSION, "(4,1)",
                "Vector addition is component-wise: (1+3, 2+(-1)) = (4, 1).", 12);

        question(vectors, "What is 2 × (3, -2)? (format: (x,y), no spaces)",
                Question.QuestionType.EXPRESSION, "(6,-4)",
                "Scalar multiplication scales each component: 2×(3, -2) = (6, -4).", 12);

        question(vectors, "The zero vector added to any vector v gives:",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "The zero vector is the additive identity: v + 0 = v for any vector v.", 10,
                opt("v", true),
                opt("0", false),
                opt("-v", false));

        Lesson matrices = lesson(linalg, "Matrices & Linear Transformations",
                "How matrices encode rotations, scaling, and other transformations.",
                "A matrix can be thought of as a function that transforms vectors — rotating, scaling, or shearing "
                        + "them. Multiplying a matrix by a vector applies that transformation to the vector.",
                Lesson.Difficulty.INTERMEDIATE, 2, 60);

        question(matrices, "What is the determinant of the identity matrix [[1,0],[0,1]]?",
                Question.QuestionType.NUMERIC, "1",
                "The identity matrix represents 'no transformation,' preserving area, so its determinant is 1.", 12);

        question(matrices, "What does a determinant of 0 tell you about a matrix's transformation?",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "A zero determinant means the transformation collapses space into a lower dimension (no inverse exists).", 12,
                opt("It collapses space into a lower dimension", true),
                opt("It rotates space by 90 degrees", false),
                opt("It doubles all lengths", false));

        question(matrices, "For matrix [[2,0],[0,2]] applied to vector (1,1), what is the result? (format: (x,y), no spaces)",
                Question.QuestionType.EXPRESSION, "(2,2)",
                "This matrix scales every vector by 2, so (1,1) maps to (2,2).", 15);

        question(matrices, "What is the determinant of [[2,0],[0,3]]?",
                Question.QuestionType.NUMERIC, "6",
                "For a diagonal matrix, the determinant is the product of the diagonal entries: 2 × 3 = 6.", 12);

        Lesson eigen = lesson(linalg, "Eigenvalues & Eigenvectors",
                "The special directions a matrix only stretches, never rotates.",
                "An eigenvector v of a matrix A satisfies Av = λv — the matrix only scales v by the eigenvalue λ, "
                        + "without changing its direction. Eigenvalues are found by solving det(A - λI) = 0.",
                Lesson.Difficulty.ADVANCED, 3, 80);

        question(eigen, "For matrix [[2,0],[0,3]], what are its eigenvalues? (format: comma-separated, ascending order, no spaces)",
                Question.QuestionType.EXPRESSION, "2,3",
                "A diagonal matrix's eigenvalues are simply its diagonal entries: 2 and 3.", 18);

        question(eigen, "An eigenvector of a matrix A, when transformed by A, changes in:",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "By definition Av = λv — the direction of v is preserved; only its length (by factor λ) changes.", 12,
                opt("Length only, not direction", true),
                opt("Direction only, not length", false),
                opt("Both length and direction", false));

        question(eigen, "For matrix [[4,0],[0,4]], what is the eigenvalue (it's the same for every vector)?",
                Question.QuestionType.NUMERIC, "4",
                "This is 4 times the identity matrix, so every vector is an eigenvector with eigenvalue 4.", 15);

        question(eigen, "What equation is solved to find a matrix's eigenvalues λ?",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "Eigenvalues satisfy det(A - λI) = 0, the characteristic equation of the matrix.", 15,
                opt("det(A - λI) = 0", true),
                opt("A + λI = 0", false),
                opt("trace(A) = λ", false));

        question(eigen, "For matrix [[3,1],[0,2]] (upper triangular), what are the eigenvalues? (format: comma-separated, largest first, no spaces)",
                Question.QuestionType.EXPRESSION, "3,2",
                "For a triangular matrix, the eigenvalues are exactly the diagonal entries: 3 and 2.", 18);

        // ---- Practice Lab: large auto-generated drill set ----
        Lesson linalgLab = lesson(linalg, "Practice Lab: Vectors & Matrices Drill",
                "A large set of magnitude, addition, and determinant questions for repeated practice.",
                "This practice lab drills vector magnitude using Pythagorean triples, component-wise vector "
                        + "addition, and 2×2 determinants. There's no penalty for retrying — work through as "
                        + "many as you like.",
                Lesson.Difficulty.EXPERT, 4, 10);

        int[][] triples = {
                {3, 4, 5}, {6, 8, 10}, {5, 12, 13}, {9, 12, 15}, {8, 15, 17}, {12, 16, 20},
                {7, 24, 25}, {20, 21, 29}, {9, 40, 41}, {12, 35, 37}, {11, 60, 61}, {28, 45, 53},
                {33, 56, 65}, {16, 63, 65}, {48, 55, 73}, {13, 84, 85}, {36, 77, 85}
        };
        for (int[] t : triples) {
            question(linalgLab, "What is the magnitude of the vector (" + t[0] + ", " + t[1] + ")?",
                    Question.QuestionType.NUMERIC, String.valueOf(t[2]),
                    "Magnitude = sqrt(" + t[0] + "² + " + t[1] + "²) = sqrt(" + (t[0] * t[0] + t[1] * t[1])
                            + ") = " + t[2] + ".", 8);
        }

        for (int i = 1; i <= 18; i++) {
            int a1 = i, b1 = i + 2, a2 = 15 - i, b2 = i - 5;
            int sx = a1 + a2, sy = b1 + b2;
            question(linalgLab, "If v1 = (" + a1 + ", " + b1 + ") and v2 = (" + a2 + ", " + b2
                            + "), what is v1 + v2? (format: (x,y), no spaces)",
                    Question.QuestionType.EXPRESSION, "(" + sx + "," + sy + ")",
                    "Vector addition is component-wise: (" + a1 + "+" + a2 + ", " + b1 + "+" + b2
                            + ") = (" + sx + ", " + sy + ").", 8);
        }

        for (int i = 1; i <= 18; i++) {
            int p = i, q = 19 - i;
            int det = p * q;
            question(linalgLab, "What is the determinant of the matrix [[" + p + ",0],[0," + q + "]]?",
                    Question.QuestionType.MULTIPLE_CHOICE, null,
                    "For a diagonal matrix, the determinant is the product of the diagonal entries: "
                            + p + " × " + q + " = " + det + ".", 8,
                    opt(String.valueOf(det), true),
                    opt(String.valueOf(p + q), false),
                    opt(String.valueOf(Math.abs(p - q)), false));
        }

        // ---- JEE Practice: genuinely exam-caliber 3D vector geometry ----
        Lesson linalgJee = lesson(linalg, "JEE Practice: Vector Algebra & Determinants",
                "Multi-step 3D vector geometry problems — a heavily-weighted JEE topic.",
                "These require combining the dot product, cross product, and vector magnitude to answer a "
                        + "genuine geometric question: the angle between two vectors, a scalar projection, "
                        + "whether three vectors are coplanar, the area of a 3D triangle, and the distance from "
                        + "a point to a line — each needs at least two formulas chained together.",
                Lesson.Difficulty.EXPERT, 5, 100);

        // A. Angle between two vectors (degrees)
        for (int i = 1; i <= 20; i++) {
            int a1 = i % 4 + 1, a2 = (i * 2) % 5 + 1, a3 = i % 3 + 1;
            int b1 = (i + 1) % 3 + 1, b2 = (i * 2 + 1) % 4 + 1, b3 = (i + 2) % 3 + 1;
            double dot = a1 * b1 + a2 * b2 + a3 * b3;
            double magA = Math.sqrt(a1 * a1 + a2 * a2 + a3 * a3);
            double magB = Math.sqrt(b1 * b1 + b2 * b2 + b3 * b3);
            double cosT = Math.max(-1, Math.min(1, dot / (magA * magB)));
            double ang = Math.toDegrees(Math.acos(cosT));
            String ans = String.format(Locale.US, "%.2f", ang);
            question(linalgJee, "For a = (" + a1 + "," + a2 + "," + a3 + ") and b = (" + b1 + "," + b2 + ","
                            + b3 + "), what is the angle between them in degrees, rounded to 2 decimal places?",
                    Question.QuestionType.NUMERIC, ans,
                    "cosθ = (a·b)/(|a||b|). Computing a·b, |a|, and |b|, then θ = cos⁻¹(...) ≈ " + ans + "°.", 18);
        }

        // B. Scalar projection of a onto b
        for (int i = 1; i <= 20; i++) {
            int a1 = i % 5 + 1, a2 = (i * 2) % 4 + 1, a3 = i % 2 + 1;
            int b1 = (i + 2) % 4 + 1, b2 = (i * 3) % 3 + 1, b3 = (i + 1) % 2 + 1;
            double dot = a1 * b1 + a2 * b2 + a3 * b3;
            double magB = Math.sqrt(b1 * b1 + b2 * b2 + b3 * b3);
            double val = dot / magB;
            String ans = String.format(Locale.US, "%.2f", val);
            question(linalgJee, "For a = (" + a1 + "," + a2 + "," + a3 + ") and b = (" + b1 + "," + b2 + ","
                            + b3 + "), what is the scalar projection of a onto b, rounded to 2 decimal places?",
                    Question.QuestionType.NUMERIC, ans,
                    "Scalar projection of a onto b = (a·b)/|b|. Here that's " + ans + ".", 16);
        }

        // C. Coplanarity check via scalar triple product
        for (int i = 1; i <= 20; i++) {
            int[] a, b, c;
            a = new int[]{i % 3 + 1, (i + 1) % 4 + 1, (i + 2) % 2 + 1};
            b = new int[]{(i + 1) % 3 + 1, (i + 2) % 4 + 1, i % 2 + 1};
            if (i % 3 == 0) {
                int p = (i % 3) + 1, qc = (i % 2) + 1;
                c = new int[]{p * a[0] + qc * b[0], p * a[1] + qc * b[1], p * a[2] + qc * b[2]};
            } else {
                c = new int[]{(i + 2) % 3 + 1, i % 4 + 1, (i + 1) % 2 + 1};
            }
            int bxcx = b[1] * c[2] - b[2] * c[1];
            int bxcy = b[2] * c[0] - b[0] * c[2];
            int bxcz = b[0] * c[1] - b[1] * c[0];
            int stp = a[0] * bxcx + a[1] * bxcy + a[2] * bxcz;
            boolean coplanar = stp == 0;
            question(linalgJee, "Are a=(" + a[0] + "," + a[1] + "," + a[2] + "), b=(" + b[0] + "," + b[1] + ","
                            + b[2] + "), and c=(" + c[0] + "," + c[1] + "," + c[2] + ") coplanar?",
                    Question.QuestionType.MULTIPLE_CHOICE, null,
                    "Three vectors are coplanar iff their scalar triple product a·(b×c) = 0. Computing it "
                            + "here gives " + stp + ", so they " + (coplanar ? "are" : "are not") + " coplanar.", 18,
                    opt("Coplanar", coplanar),
                    opt("Not coplanar", !coplanar));
        }

        // D. Area of a triangle given 3 points in 3D
        for (int i = 1; i <= 20; i++) {
            int[] A = {i % 4, (i + 1) % 3, (i + 2) % 2};
            int[] B = {A[0] + (i % 3) + 1, A[1] + (i % 2) + 1, A[2] + 1};
            int[] C = {A[0] + (i + 1) % 3 + 1, A[1] + 1, A[2] + (i % 3) + 1};
            int[] AB = {B[0] - A[0], B[1] - A[1], B[2] - A[2]};
            int[] AC = {C[0] - A[0], C[1] - A[1], C[2] - A[2]};
            int cx = AB[1] * AC[2] - AB[2] * AC[1];
            int cy = AB[2] * AC[0] - AB[0] * AC[2];
            int cz = AB[0] * AC[1] - AB[1] * AC[0];
            double mag = Math.sqrt(cx * cx + cy * cy + cz * cz);
            double area = 0.5 * mag;
            String ans = String.format(Locale.US, "%.2f", area);
            question(linalgJee, "Triangle has vertices A=(" + A[0] + "," + A[1] + "," + A[2] + "), B=("
                            + B[0] + "," + B[1] + "," + B[2] + "), C=(" + C[0] + "," + C[1] + "," + C[2]
                            + "). What is its area, rounded to 2 decimal places?",
                    Question.QuestionType.NUMERIC, ans,
                    "Area = ½|AB × AC|. Computing the cross product of AB and AC and halving its magnitude "
                            + "gives ≈ " + ans + ".", 20);
        }

        // E. Distance from a point to a line in 3D
        for (int i = 1; i <= 20; i++) {
            int[] A = {i % 3, (i + 1) % 2, (i + 2) % 3};
            int[] d = {(i % 3) + 1, (i + 1) % 2 + 1, (i % 2) + 1};
            int[] P = {A[0] + (i % 2) + 1, A[1] + (i % 3) + 2, A[2] + (i % 2)};
            int[] AP = {P[0] - A[0], P[1] - A[1], P[2] - A[2]};
            int cx = AP[1] * d[2] - AP[2] * d[1];
            int cy = AP[2] * d[0] - AP[0] * d[2];
            int cz = AP[0] * d[1] - AP[1] * d[0];
            double magCross = Math.sqrt(cx * cx + cy * cy + cz * cz);
            double magD = Math.sqrt(d[0] * d[0] + d[1] * d[1] + d[2] * d[2]);
            double dist = magCross / magD;
            String ans = String.format(Locale.US, "%.2f", dist);
            question(linalgJee, "A line passes through A=(" + A[0] + "," + A[1] + "," + A[2] + ") in direction d=("
                            + d[0] + "," + d[1] + "," + d[2] + "). What is the distance from point P=(" + P[0]
                            + "," + P[1] + "," + P[2] + ") to this line, rounded to 2 decimal places?",
                    Question.QuestionType.NUMERIC, ans,
                    "Distance from P to the line = |AP × d| / |d|, where AP is the vector from A to P. "
                            + "Computing this gives ≈ " + ans + ".", 20);
        }
    }

    // ---------------------------------------------------------------- Probability

    private void seedProbability() {
        MathDomain prob = domain("Probability", "probability", "Randomness, distributions, and expectation.", "distribution-plot");

        Lesson foundations = lesson(prob, "Foundations of Probability",
                "Sample spaces, events, and the rules of probability.",
                "Probability measures how likely an event is, from 0 (impossible) to 1 (certain). For equally "
                        + "likely outcomes, P(event) = favorable outcomes / total outcomes.",
                Lesson.Difficulty.BEGINNER, 1, 50);

        question(foundations, "What is the probability of rolling a 4 on a fair six-sided die?",
                Question.QuestionType.EXPRESSION, "1/6",
                "There's 1 favorable outcome (rolling a 4) out of 6 equally likely outcomes: P = 1/6.", 10);

        question(foundations, "What is the probability of an impossible event?",
                Question.QuestionType.NUMERIC, "0",
                "By definition, an event that cannot occur has probability 0.", 10);

        question(foundations, "For any event A, probability values must fall in which range?",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "Probabilities are always between 0 (impossible) and 1 (certain), inclusive.", 10,
                opt("Between 0 and 1", true),
                opt("Between -1 and 1", false),
                opt("Any positive number", false));

        question(foundations, "Two coin flips: what is the probability of getting exactly two heads?",
                Question.QuestionType.EXPRESSION, "1/4",
                "There are 4 equally likely outcomes (HH, HT, TH, TT); only HH matches, so P = 1/4.", 12);

        question(foundations, "If P(A) = 0.3, what is P(not A)?",
                Question.QuestionType.NUMERIC, "0.7",
                "Complementary probabilities sum to 1, so P(not A) = 1 - 0.3 = 0.7.", 10);

        Lesson distributions = lesson(prob, "Expected Value & Distributions",
                "Summarizing randomness with expectation, and the shape of the normal curve.",
                "Expected value is the long-run average outcome of a random variable. The normal distribution, "
                        + "shaped by its mean (μ) and standard deviation (σ), models many natural phenomena. Try "
                        + "adjusting μ and σ above to see the curve shift and spread.",
                Lesson.Difficulty.INTERMEDIATE, 2, 60);

        question(distributions, "What is the expected value of a fair six-sided die roll?",
                Question.QuestionType.NUMERIC, "3.5",
                "E[X] = (1+2+3+4+5+6)/6 = 21/6 = 3.5.", 15);

        question(distributions, "In a normal distribution, increasing σ (standard deviation) makes the curve:",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "A larger standard deviation spreads the data further from the mean, flattening and widening the curve.", 12,
                opt("Wider and flatter", true),
                opt("Narrower and taller", false),
                opt("Shift to the right", false));

        question(distributions, "In a normal distribution, what does μ (mu) control?",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "μ is the mean of the distribution — it sets the center point the curve is symmetric around.", 10,
                opt("The center of the curve", true),
                opt("The width of the curve", false),
                opt("The height of the peak only", false));

        question(distributions, "For a normal distribution, approximately what percent of data falls within 1 standard deviation of the mean?",
                Question.QuestionType.NUMERIC, "68",
                "The empirical rule states ~68% of data lies within 1σ of the mean in a normal distribution.", 15);

        question(distributions, "For a fair die roll X, write the expected value formula summed over outcomes. (format: a general expression in x and P(x))",
                Question.QuestionType.EXPRESSION, "sum(x*P(x))",
                "Expected value is the probability-weighted sum of outcomes: E[X] = Σ x·P(x).", 15);

        Lesson bayes = lesson(prob, "Conditional Probability & Bayes' Theorem",
                "Updating probabilities when new information arrives.",
                "Conditional probability P(A|B) measures the chance of A given that B has occurred: "
                        + "P(A|B) = P(A∩B)/P(B). Bayes' theorem flips the condition around: "
                        + "P(A|B) = P(B|A)·P(A) / P(B), letting us update beliefs with evidence.",
                Lesson.Difficulty.ADVANCED, 3, 80);

        question(bayes, "What is the formula for conditional probability P(A|B)? (format: a fraction of probabilities, no spaces)",
                Question.QuestionType.EXPRESSION, "P(A∩B)/P(B)",
                "Conditional probability restricts the sample space to B: P(A|B) = P(A and B) / P(B).", 18);

        question(bayes, "Two events A and B are independent when:",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "Independence means knowing B occurred doesn't change the probability of A: P(A|B) = P(A), equivalent to P(A∩B) = P(A)P(B).", 12,
                opt("P(A∩B) = P(A) × P(B)", true),
                opt("P(A∩B) = P(A) + P(B)", false),
                opt("P(A|B) = P(B|A)", false));

        question(bayes, "A bag has 3 red and 2 blue balls. What is P(red) on the first draw? (enter as a reduced fraction)",
                Question.QuestionType.EXPRESSION, "3/5",
                "There are 3 red out of 5 total balls, all equally likely: P(red) = 3/5.", 15);

        question(bayes, "If P(B) = 0.5, P(A) = 0.4, and P(B|A) = 0.6, what is P(A|B) by Bayes' theorem? (enter as a decimal, e.g. 0.xx)",
                Question.QuestionType.NUMERIC, "0.48",
                "Bayes' theorem: P(A|B) = P(B|A)·P(A)/P(B) = (0.6 × 0.4) / 0.5 = 0.24/0.5 = 0.48.", 20);

        question(bayes, "In a medical test, testing positive when you actually have the disease relates to which probability?",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "The probability of testing positive given you have the disease is the test's sensitivity, P(positive | disease).", 15,
                opt("P(positive | disease) — the test's sensitivity", true),
                opt("P(disease) alone — the base rate", false),
                opt("P(positive) alone — the marginal probability", false));

        // ---- Practice Lab: large auto-generated drill set ----
        Lesson probLab = lesson(prob, "Practice Lab: Dice, Coins & Fractions Drill",
                "A large set of dice, coin-flip, and fraction-reduction questions for repeated practice.",
                "This practice lab drills classic probability computations — two-dice sums, coin-flip streaks, "
                        + "and reducing probabilities to fractions in lowest terms. There's no penalty for "
                        + "retrying — work through as many as you like.",
                Lesson.Difficulty.EXPERT, 4, 10);

        for (int s = 2; s <= 12; s++) {
            int ways = 6 - Math.abs(7 - s);
            String ans = String.format(Locale.US, "%.4f", ways / 36.0);
            question(probLab, "What is the probability of rolling a sum of " + s
                            + " with two six-sided dice? (rounded to 4 decimal places)",
                    Question.QuestionType.NUMERIC, ans,
                    "There are " + ways + " ways to roll a sum of " + s + " out of 36 total outcomes: P = "
                            + ways + "/36 ≈ " + ans + ".", 8);
        }

        for (int n = 1; n <= 6; n++) {
            String ans = String.format(Locale.US, "%.4f", Math.pow(0.5, n));
            question(probLab, "What is the probability of getting all heads in " + n
                            + " fair coin flips? (rounded to 4 decimal places)",
                    Question.QuestionType.NUMERIC, ans,
                    "Each flip is independent with probability 1/2, so P(all heads) = (1/2)^" + n + " = " + ans + ".", 8);
        }

        for (int s = 2; s <= 12; s++) {
            int ways = 6 - Math.abs(7 - s);
            int g = gcd(ways, 36);
            question(probLab, "Express P(sum = " + s + ") with two dice as a fraction in lowest terms.",
                    Question.QuestionType.EXPRESSION, (ways / g) + "/" + (36 / g),
                    "There are " + ways + "/36 ways, which reduces to " + (ways / g) + "/" + (36 / g)
                            + " in lowest terms.", 8);
        }

        for (int k = 1; k <= 5; k++) {
            int ways = 6 - k;
            int g = gcd(ways, 6);
            question(probLab, "For a fair six-sided die, express P(rolling greater than " + k
                            + ") as a fraction in lowest terms.",
                    Question.QuestionType.EXPRESSION, (ways / g) + "/" + (6 / g),
                    "There are " + ways + " favorable outcomes out of 6: " + ways + "/6 reduces to "
                            + (ways / g) + "/" + (6 / g) + ".", 8);
        }

        int[] waysTwoFlip = {1, 2, 1};
        for (int h = 0; h <= 2; h++) {
            int ways = waysTwoFlip[h];
            int g = gcd(ways, 4);
            question(probLab, "In 2 fair coin flips, express P(exactly " + h + " heads) as a fraction in lowest terms.",
                    Question.QuestionType.EXPRESSION, (ways / g) + "/" + (4 / g),
                    "There are " + ways + " outcomes with exactly " + h + " heads out of 4 equally likely outcomes: "
                            + ways + "/4 reduces to " + (ways / g) + "/" + (4 / g) + ".", 8);
        }

        for (int i = 1; i <= 18; i++) {
            if (i == 10) continue; // p = 0.50 would make P(A) and P(not A) identical strings
            double p = i * 0.05;
            String pStr = String.format(Locale.US, "%.2f", p);
            String compStr = String.format(Locale.US, "%.2f", 1 - p);
            question(probLab, "If P(A) = " + pStr + ", what is P(not A)?",
                    Question.QuestionType.MULTIPLE_CHOICE, null,
                    "Complementary probabilities sum to 1: P(not A) = 1 - " + pStr + " = " + compStr + ".", 8,
                    opt(compStr, true),
                    opt(pStr, false),
                    opt(String.format(Locale.US, "%.2f", p / 2), false));
        }

        // ---- JEE Practice: genuinely exam-caliber probability ----
        Lesson probJee = lesson(prob, "JEE Practice: Conditional Probability & Bayes' Theorem",
                "Multi-stage probability problems — combining counting, conditioning, and Bayes' theorem.",
                "These require chaining several steps: drawing without replacement, applying the law of "
                        + "total probability across two scenarios before using Bayes' theorem, computing an "
                        + "expectation from a distribution table, and summing a binomial tail — each needs "
                        + "real multi-step reasoning, not a single formula lookup.",
                Lesson.Difficulty.EXPERT, 6, 100);

        // A. Draw 2 without replacement, both the same designated color
        for (int i = 1; i <= 20; i++) {
            int R = (i % 6) + 4;
            int B = (i % 5) + 3;
            long num = ncr(R, 2);
            long den = ncr(R + B, 2);
            long g = gcd((int) num, (int) den);
            String ans = (num / g) + "/" + (den / g);
            question(probJee, "An urn has " + R + " red and " + B + " blue balls. Two balls are drawn "
                            + "without replacement. What is P(both red)? (enter as a reduced fraction)",
                    Question.QuestionType.EXPRESSION, ans,
                    "P(both red) = C(" + R + ",2)/C(" + (R + B) + ",2), since order doesn't matter and the "
                            + "second draw depends on the first (no replacement). This reduces to " + ans + ".", 20);
        }

        // B. Bayes' theorem with two urns
        for (int i = 1; i <= 20; i++) {
            int R1 = (i % 4) + 3, B1 = (i % 3) + 2;
            int R2 = (i % 5) + 2, B2 = (i % 4) + 3;
            long pRed1Num = R1, pRed1Den = R1 + B1;
            long pRed2Num = R2, pRed2Den = R2 + B2;
            // P(red) = 1/2 * R1/(R1+B1) + 1/2 * R2/(R2+B2), common denom 2*(R1+B1)*(R2+B2)
            long commonDen = 2L * pRed1Den * pRed2Den;
            long pRedNum = pRed1Num * pRed2Den + pRed2Num * pRed1Den;
            // P(urn1 and red) = 1/2 * R1/(R1+B1) = R1 / (2*(R1+B1)), express over commonDen
            long pUrn1RedNum = pRed1Num * pRed2Den;
            long g = gcd((int) pUrn1RedNum, (int) pRedNum);
            String ans = (pUrn1RedNum / g) + "/" + (pRedNum / g);
            question(probJee, "Urn 1 has " + R1 + " red and " + B1 + " blue balls; Urn 2 has " + R2 + " red and "
                            + B2 + " blue balls. You pick an urn at random (equal chance) and draw one ball — "
                            + "it's red. What is P(it came from Urn 1)? (enter as a reduced fraction)",
                    Question.QuestionType.EXPRESSION, ans,
                    "By Bayes' theorem: P(Urn1|Red) = P(Red|Urn1)P(Urn1) / [P(Red|Urn1)P(Urn1) + P(Red|Urn2)P(Urn2)]. "
                            + "With P(Urn1)=P(Urn2)=1/2, this works out to " + ans + ".", 25);
        }

        // C. Expectation from an explicit probability table
        for (int i = 1; i <= 20; i++) {
            int d = (i % 5) + 6;
            int a1 = (i % 3) + 1, a2 = (i % 2) + 1, a3 = (i % 2) + 1;
            int a4 = d - a1 - a2 - a3;
            if (a4 <= 0) { d += Math.abs(a4) + 1; a4 = d - a1 - a2 - a3; }
            long eNum = 0L * a1 + 1L * a2 + 2L * a3 + 3L * a4;
            long eDen = d;
            long g = gcd((int) eNum, (int) eDen);
            String ans = (eDen / g == 1) ? String.valueOf(eNum / g) : (eNum / g) + "/" + (eDen / g);
            question(probJee, "A random variable X takes values 0,1,2,3 with probabilities " + a1 + "/" + d
                            + ", " + a2 + "/" + d + ", " + a3 + "/" + d + ", " + a4 + "/" + d
                            + " respectively. What is E[X]? (enter as a reduced fraction, or a whole number if it simplifies)",
                    Question.QuestionType.EXPRESSION, ans,
                    "E[X] = Σx·P(x) = 0×(" + a1 + "/" + d + ") + 1×(" + a2 + "/" + d + ") + 2×(" + a3 + "/" + d
                            + ") + 3×(" + a4 + "/" + d + "), which simplifies to " + ans + ".", 20);
        }

        // D. At-least-k binomial (cumulative)
        for (int i = 1; i <= 20; i++) {
            int n = (i % 5) + 4;
            int k = (i % n) + 1;
            double val = 0;
            for (int j = k; j <= n; j++) {
                val += ncr(n, j) * Math.pow(0.5, j) * Math.pow(0.5, n - j);
            }
            String ans = String.format(Locale.US, "%.4f", val);
            question(probJee, "For " + n + " fair coin flips, what is P(at least " + k
                            + " heads), rounded to 4 decimal places?",
                    Question.QuestionType.NUMERIC, ans,
                    "P(X≥" + k + ") = Σ from j=" + k + " to " + n + " of C(" + n + ",j)×0.5^n. Summing all "
                            + "these binomial terms gives ≈ " + ans + ".", 22);
        }
    }

    // ---------------------------------------------------------------- Number Theory

    private void seedNumberTheory() {
        MathDomain numtheory = domain("Number Theory", "number-theory", "Primes, divisibility, and modular arithmetic.", "number-line");

        Lesson primes = lesson(numtheory, "Primes & Divisibility",
                "What makes a number prime, and how divisibility works.",
                "A prime number has exactly two divisors: 1 and itself. Every integer greater than 1 is either "
                        + "prime or can be written as a product of primes (its prime factorization). Use the slider "
                        + "above to highlight multiples and see how they interact with primes.",
                Lesson.Difficulty.BEGINNER, 1, 50);

        question(primes, "Which of these numbers is prime?",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "17 has no divisors other than 1 and itself, making it prime. 15 = 3×5 and 21 = 3×7 are not.", 10,
                opt("15", false),
                opt("17", true),
                opt("21", false));

        question(primes, "Is 1 considered a prime number? (enter yes or no)",
                Question.QuestionType.EXPRESSION, "no",
                "By definition, a prime has exactly two distinct divisors. 1 has only one divisor (itself), so it is not prime.", 10);

        question(primes, "What is the smallest prime number?",
                Question.QuestionType.NUMERIC, "2",
                "2 is the smallest and the only even prime number.", 10);

        question(primes, "What is the prime factorization of 12? (format: p1^e1*p2^e2..., no spaces)",
                Question.QuestionType.EXPRESSION, "2^2*3",
                "12 = 2 × 2 × 3 = 2² × 3.", 12);

        question(primes, "How many primes are there less than 10?",
                Question.QuestionType.NUMERIC, "4",
                "The primes less than 10 are 2, 3, 5, and 7 — four of them.", 12);

        Lesson modular = lesson(numtheory, "Modular Arithmetic",
                "Arithmetic that 'wraps around,' like a clock.",
                "In modular arithmetic, numbers wrap around after reaching a modulus n. We write a ≡ b (mod n) when "
                        + "a and b leave the same remainder after division by n. It's exactly how a 12-hour clock works.",
                Lesson.Difficulty.INTERMEDIATE, 2, 60);

        question(modular, "What is 14 mod 5?",
                Question.QuestionType.NUMERIC, "4",
                "14 divided by 5 is 2 remainder 4, so 14 mod 5 = 4.", 10);

        question(modular, "On a 12-hour clock, if it's 9:00 now, what time will it be in 8 hours?",
                Question.QuestionType.NUMERIC, "5",
                "9 + 8 = 17, and 17 mod 12 = 5, so it will be 5:00.", 12);

        question(modular, "Which statement about modular arithmetic is true?",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "Two numbers are congruent mod n exactly when they leave the same remainder when divided by n.", 10,
                opt("a ≡ b (mod n) means a and b have the same remainder when divided by n", true),
                opt("a ≡ b (mod n) means a and b are exactly equal", false),
                opt("a ≡ b (mod n) means a + b = n", false));

        question(modular, "What is 7 mod 7?",
                Question.QuestionType.NUMERIC, "0",
                "Any number divides itself evenly, so the remainder is 0: 7 mod 7 = 0.", 10);

        question(modular, "What is (5 + 9) mod 6?",
                Question.QuestionType.NUMERIC, "2",
                "5 + 9 = 14, and 14 mod 6 = 2 (since 14 = 2×6 + 2).", 12);

        question(modular, "Write the congruence relation for 17 and 5 modulo 6. (format: a≡r(mod n), no spaces)",
                Question.QuestionType.EXPRESSION, "17≡5(mod 6)",
                "17 mod 6 = 5 and 5 mod 6 = 5 — same remainder, so 17 ≡ 5 (mod 6).", 15);

        Lesson gcd = lesson(numtheory, "GCD, LCM & the Euclidean Algorithm",
                "Finding the greatest common divisor efficiently, and why it matters.",
                "The greatest common divisor (GCD) of two integers is the largest number dividing both. The "
                        + "Euclidean algorithm finds it fast: gcd(a, b) = gcd(b, a mod b), repeated until the "
                        + "remainder is 0. The least common multiple relates to GCD via lcm(a,b) = a·b / gcd(a,b).",
                Lesson.Difficulty.ADVANCED, 3, 80);

        question(gcd, "What is gcd(12, 18)?",
                Question.QuestionType.NUMERIC, "6",
                "The divisors of 12 are 1,2,3,4,6,12 and of 18 are 1,2,3,6,9,18 — the greatest common one is 6.", 15);

        question(gcd, "Using the Euclidean algorithm, gcd(48, 18) reduces first to gcd(18, r). What is r?",
                Question.QuestionType.NUMERIC, "12",
                "48 = 2×18 + 12, so the first step reduces gcd(48,18) to gcd(18,12).", 18);

        question(gcd, "What is lcm(4, 6)?",
                Question.QuestionType.NUMERIC, "12",
                "lcm(4,6) = 4×6/gcd(4,6) = 24/2 = 12.", 15);

        question(gcd, "Two integers a and b are coprime (relatively prime) when:",
                Question.QuestionType.MULTIPLE_CHOICE, null,
                "Coprime numbers share no common factors besides 1, i.e. gcd(a,b) = 1.", 12,
                opt("gcd(a, b) = 1", true),
                opt("a and b are both prime", false),
                opt("a + b is prime", false));

        question(gcd, "Write the general formula relating lcm and gcd of a and b. (format: an equation relating lcm(a,b) and gcd(a,b), no spaces)",
                Question.QuestionType.EXPRESSION, "lcm(a,b)=a*b/gcd(a,b)",
                "The product of two numbers always equals their gcd times their lcm: lcm(a,b) = a·b / gcd(a,b).", 18);

        question(gcd, "What is gcd(17, 5), given 17 is prime and doesn't divide 5?",
                Question.QuestionType.NUMERIC, "1",
                "17 is prime and shares no factors with 5, so they're coprime: gcd(17,5) = 1.", 12);

        // ---- Practice Lab: large auto-generated drill set ----
        Lesson ntLab = lesson(numtheory, "Practice Lab: Mod & GCD Drill",
                "A large set of modular arithmetic and GCD questions for repeated practice.",
                "This practice lab drills remainders, the Euclidean algorithm, and congruence notation across "
                        + "many number pairs. There's no penalty for retrying — work through as many as you like.",
                Lesson.Difficulty.EXPERT, 4, 10);

        for (int i = 1; i <= 15; i++) {
            int a = 21 + i * 3;
            int r = a % 7;
            question(ntLab, "What is " + a + " mod 7?",
                    Question.QuestionType.NUMERIC, String.valueOf(r),
                    a + " divided by 7 leaves a remainder of " + r + ", so " + a + " mod 7 = " + r + ".", 8);
        }

        for (int i = 1; i <= 15; i++) {
            int a = 12 + i;
            int b = 8 + 2 * i;
            int g = gcd(a, b);
            question(ntLab, "What is gcd(" + a + ", " + b + ")?",
                    Question.QuestionType.NUMERIC, String.valueOf(g),
                    "Using the Euclidean algorithm on " + a + " and " + b + " gives a greatest common divisor of " + g + ".", 8);
        }

        for (int i = 1; i <= 15; i++) {
            int a = 50 + i * 2;
            int b = 11;
            int r = a % b;
            question(ntLab, "Write the congruence relation for " + a + " modulo " + b
                            + ". (format: a≡r(mod n), no spaces)",
                    Question.QuestionType.EXPRESSION, a + "≡" + r + "(mod " + b + ")",
                    a + " mod " + b + " = " + r + ", so " + a + " ≡ " + r + " (mod " + b + ").", 10);
        }

        for (int i = 1; i <= 15; i++) {
            int a = 100 + i * 5;
            int b = 9;
            int r = a % b;
            int wrong1 = (r + 1) % b;
            int wrong2 = (r + 2) % b;
            question(ntLab, "What is " + a + " mod " + b + "?",
                    Question.QuestionType.MULTIPLE_CHOICE, null,
                    a + " divided by " + b + " leaves a remainder of " + r + ", so " + a + " mod " + b + " = " + r + ".", 8,
                    opt(String.valueOf(r), true),
                    opt(String.valueOf(wrong1), false),
                    opt(String.valueOf(wrong2), false));
        }

        // ---- JEE Practice: genuinely exam-caliber number theory ----
        Lesson ntJee = lesson(numtheory, "JEE Practice: CRT, Fermat & Diophantine Equations",
                "Real theorem-based problems, not just remainder arithmetic.",
                "These require applying named theorems: the Chinese Remainder Theorem to find a number "
                        + "satisfying two congruences at once, Fermat's Little Theorem, the multiplicative "
                        + "order of an element, when a linear Diophantine equation has a solution, and Wilson's "
                        + "theorem — each needs understanding why the theorem works, not just plugging in.",
                Lesson.Difficulty.EXPERT, 5, 100);

        // A. Chinese Remainder Theorem
        int[][] coprimePairs = {{3,5},{3,7},{4,7},{5,7},{4,9},{5,9},{3,8},{5,8},{7,9},{4,11},{5,11},{7,11}};
        for (int i = 1; i <= 20; i++) {
            int[] mn = coprimePairs[i % coprimePairs.length];
            int m = mn[0], n = mn[1];
            int a = i % m;
            int b = (i * 2) % n;
            int x = -1;
            for (int cand = 0; cand < m * n; cand++) {
                if (cand % m == a && cand % n == b) { x = cand; break; }
            }
            question(ntJee, "Find the smallest non-negative integer x such that x ≡ " + a + " (mod " + m
                            + ") and x ≡ " + b + " (mod " + n + ")",
                    Question.QuestionType.NUMERIC, String.valueOf(x),
                    "Since gcd(" + m + "," + n + ")=1, the Chinese Remainder Theorem guarantees a unique "
                            + "solution mod " + (m * n) + ". Checking values, x = " + x + " satisfies both congruences.", 22);
        }

        // B. Fermat's Little Theorem
        int[] smallPrimes = {5, 7, 11, 13, 17, 19, 23};
        for (int i = 1; i <= 20; i++) {
            int p = smallPrimes[i % smallPrimes.length];
            int a = (i % (p - 1)) + 1;
            question(ntJee, "For prime p=" + p + " and a=" + a + " (where gcd(a,p)=1), what is a^(p-1) mod p?",
                    Question.QuestionType.MULTIPLE_CHOICE, null,
                    "Fermat's Little Theorem states that if p is prime and gcd(a,p)=1, then a^(p-1) ≡ 1 (mod p) "
                            + "— always, regardless of which a you pick.", 15,
                    opt("1", true),
                    opt("0", false),
                    opt(String.valueOf(p - 1), false));
        }

        // C. Multiplicative order
        int[][] orderPairs = {{2,7},{3,7},{2,9},{5,9},{3,10},{7,10},{2,11},{3,11},{2,13},{5,13},{3,14},{5,14}};
        for (int i = 1; i <= 20; i++) {
            int[] an = orderPairs[i % orderPairs.length];
            int a = an[0], n = an[1];
            int k = 1;
            long val = a % n;
            while (val != 1) {
                val = (val * a) % n;
                k++;
            }
            question(ntJee, "What is the multiplicative order of " + a + " modulo " + n
                            + "? (the smallest positive k such that " + a + "^k ≡ 1 mod " + n + ")",
                    Question.QuestionType.NUMERIC, String.valueOf(k),
                    "Computing " + a + "^1, " + a + "^2, ... mod " + n + " until hitting 1: the smallest such "
                            + "exponent is k = " + k + ".", 20);
        }

        // D. Linear Diophantine equation existence
        for (int i = 1; i <= 20; i++) {
            int a = (i % 8) + 2;
            int b = (i % 6) + 3;
            int c = (i * 5) % 40;
            int g = gcd(a, b);
            boolean exists = (c % g == 0);
            question(ntJee, "Does the equation " + a + "x + " + b + "y = " + c
                            + " have a solution in integers x, y?",
                    Question.QuestionType.MULTIPLE_CHOICE, null,
                    "A linear Diophantine equation ax+by=c has an integer solution iff gcd(a,b) divides c. "
                            + "Here gcd(" + a + "," + b + ")=" + g + ", and " + c + " mod " + g + " = " + (c % g)
                            + ", so a solution " + (exists ? "exists" : "does not exist") + ".", 20,
                    opt("Yes, a solution exists", exists),
                    opt("No, no solution exists", !exists));
        }

        // E. Wilson's theorem
        for (int i = 1; i <= 20; i++) {
            int p = smallPrimes[i % smallPrimes.length];
            question(ntJee, "For prime p=" + p + ", what is (p-1)! mod p? (i.e., " + (p - 1) + "! mod " + p + ")",
                    Question.QuestionType.NUMERIC, String.valueOf(p - 1),
                    "Wilson's Theorem: for any prime p, (p-1)! ≡ -1 ≡ p-1 (mod p). Here that's " + (p - 1) + ".", 18);
        }
    }

    // ---------------------------------------------------------------- Achievements

    private void seedAchievements() {
        achievement("FIRST_LESSON", "First Steps", "Complete your first lesson.", Achievement.TriggerType.LESSONS_COMPLETED, 1, 20);
        achievement("LESSONS_5", "Getting Somewhere", "Complete 5 lessons.", Achievement.TriggerType.LESSONS_COMPLETED, 5, 40);
        achievement("STREAK_3", "Warming Up", "Reach a 3-day learning streak.", Achievement.TriggerType.STREAK_DAYS, 3, 30);
        achievement("STREAK_7", "On a Roll", "Reach a 7-day learning streak.", Achievement.TriggerType.STREAK_DAYS, 7, 75);
        achievement("LEVEL_5", "Rising Mathematician", "Reach level 5.", Achievement.TriggerType.LEVEL_REACHED, 5, 50);
        achievement("XP_1000", "Four Digits", "Earn 1000 total XP.", Achievement.TriggerType.TOTAL_XP, 1000, 100);
    }

    // ---------------------------------------------------------------- Helpers

    private MathDomain domain(String name, String slug, String desc, String vizType) {
        MathDomain d = new MathDomain();
        d.setName(name);
        d.setSlug(slug);
        d.setDescription(desc);
        d.setVisualizationType(vizType);
        return domainRepository.save(d);
    }

    private Lesson lesson(MathDomain domain, String title, String summary, String content,
                           Lesson.Difficulty difficulty, int order, int xp) {
        Lesson l = new Lesson();
        l.setDomain(domain);
        l.setTitle(title);
        l.setSummary(summary);
        l.setContent(content);
        l.setDifficulty(difficulty);
        l.setSequenceOrder(order);
        l.setBaseXpReward(xp);
        return lessonRepository.save(l);
    }

    private void question(Lesson lesson, String prompt, Question.QuestionType type, String correctAnswer,
                           String explanation, int xp, QuestionOption... options) {
        Question q = new Question();
        q.setLesson(lesson);
        q.setPrompt(prompt);
        q.setType(type);
        q.setCorrectAnswer(correctAnswer);
        q.setExplanation(explanation);
        q.setXpReward(xp);
        for (QuestionOption o : options) {
            o.setQuestion(q);
            q.getOptions().add(o);
        }
        questionRepository.save(q);
    }

    private QuestionOption opt(String text, boolean correct) {
        QuestionOption o = new QuestionOption();
        o.setText(text);
        o.setCorrect(correct);
        return o;
    }

    /** Returns which quadrant (1-4) a degree angle falls in, using standard math convention. */
    private int quadrantOf(int deg) {
        int d = ((deg % 360) + 360) % 360;
        if (d < 90) return 1;
        if (d < 180) return 2;
        if (d < 270) return 3;
        return 4;
    }

    /** Greatest common divisor via the Euclidean algorithm. */
    private int gcd(int a, int b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            int t = b;
            b = a % b;
            a = t;
        }
        return a;
    }

    /** n choose r. */
    private long ncr(int n, int r) {
        if (r < 0 || r > n) return 0;
        long result = 1;
        for (int i = 0; i < r; i++) {
            result = result * (n - i) / (i + 1);
        }
        return result;
    }

    /** n permute r. */
    private long npr(int n, int r) {
        if (r < 0 || r > n) return 0;
        long result = 1;
        for (int i = 0; i < r; i++) {
            result *= (n - i);
        }
        return result;
    }

    /** (base^exp) mod m, computed the same way as Python's pow(base, exp, m). */
    private long modPow(long base, long exp, long mod) {
        long result = 1;
        long b = base % mod;
        for (long e = 0; e < exp; e++) {
            result = (result * b) % mod;
        }
        return result;
    }

    private void achievement(String code, String name, String desc, Achievement.TriggerType type, int value, int xpBonus) {
        Achievement a = new Achievement();
        a.setCode(code);
        a.setName(name);
        a.setDescription(desc);
        a.setTriggerType(type);
        a.setTriggerValue(value);
        a.setXpBonus(xpBonus);
        achievementRepository.save(a);
    }
}
