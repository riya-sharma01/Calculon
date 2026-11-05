package com.calculon.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;

/**
 * Decides whether a typed answer is equivalent to the stored answer, so a student is not marked wrong
 * for writing the same value a different way: {@code 1/2} vs {@code 0.5}, {@code sqrt3/4} vs {@code √3/4},
 * {@code x^2+1} vs {@code 1+x^2}, {@code 2x+y=3} vs {@code y=3-2x}.
 *
 * How it works:
 *  1. Identical text after cleaning is correct.
 *  2. Otherwise both answers are parsed as maths. With no variables the two numbers are compared.
 *     With variables (x, y, ...) both are evaluated at several sample points and must agree at every one.
 *     Equations are compared by checking that (left - right) is the same up to a constant factor.
 *  3. Comma-separated answers ("6,3,2") are compared as lists, in any order. A leading ± means both signs.
 *  4. Anything that cannot be parsed (words, inequalities, matrices) falls back to the cleaned-text comparison.
 *
 * It does not understand every mathematical form, so it only ever adds ways to be right; it never
 * marks an answer wrong that plain text comparison accepted.
 */
public final class AnswerMatcher {

    private AnswerMatcher() {}

    private static final double REL_TOL = 1e-9;
    /** Accepting a rounded decimal (0.667 for 2/3) only when the student typed a decimal point. */
    private static final double ROUNDED_TOL = 0.005;
    private static final int SAMPLES = 8;

    public static boolean matches(String expected, String given) {
        if (expected == null || given == null) return false;
        String e = clean(expected);
        String g = clean(given);
        if (g.isEmpty()) return false;
        if (e.equals(g)) return true;

        try {
            List<String> ePieces = splitTopLevel(expandPlusMinus(e));
            List<String> gPieces = splitTopLevel(expandPlusMinus(g));
            if (ePieces.size() != gPieces.size()) return false;
            if (ePieces.size() == 1) return pieceMatches(ePieces.get(0), gPieces.get(0));
            // list answers: every expected piece must pair with a distinct given piece
            boolean[] used = new boolean[gPieces.size()];
            for (String ep : ePieces) {
                boolean found = false;
                for (int i = 0; i < gPieces.size(); i++) {
                    if (!used[i] && pieceMatches(ep, gPieces.get(i))) {
                        used[i] = true;
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }
            return true;
        } catch (RuntimeException ex) {
            return false; // could not parse: already compared as text above
        }
    }

    // ---------------------------------------------------------------- cleaning

    /** Lower-case, no spaces, unicode maths symbols turned into plain ASCII forms. */
    static String clean(String s) {
        String t = s.trim().toLowerCase(Locale.ROOT);
        t = t.replace("−", "-").replace("–", "-").replace("×", "*").replace("·", "*").replace("⋅", "*")
                .replace("÷", "/").replace("π", "pi").replace("²", "^2").replace("³", "^3")
                .replace("√", "sqrt").replace("°", "").replace("\\", "").replace("{", "(").replace("}", ")")
                .replace("θ", "t").replace("λ", "l").replace("α", "a").replace("β", "b").replace("μ", "m")
                .replace("σ", "s");
        t = t.replaceAll("\\s+", "");
        // an indefinite integral's constant: x^2/2+c is the same family as x^2/2
        t = t.replaceAll("\\+c$", "");
        t = t.replace("tan^-1", "atan").replace("sin^-1", "asin").replace("cos^-1", "acos")
                .replace("arctan", "atan").replace("arcsin", "asin").replace("arccos", "acos");
        return t;
    }

    private static String expandPlusMinus(String s) {
        // "±2" -> "2,-2" ; "±sqrt3/2" -> "sqrt3/2,-sqrt3/2" (only when the sign leads a whole piece)
        if (!s.contains("±")) return s;
        List<String> out = new ArrayList<>();
        for (String piece : splitTopLevel(s)) {
            if (piece.startsWith("±")) {
                out.add(piece.substring(1));
                out.add("-" + piece.substring(1));
            } else {
                out.add(piece);
            }
        }
        return String.join(",", out);
    }

    private static List<String> splitTopLevel(String s) {
        List<String> parts = new ArrayList<>();
        int depth = 0, start = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(' || c == '[') depth++;
            else if (c == ')' || c == ']') depth--;
            else if (c == ',' && depth == 0) {
                parts.add(s.substring(start, i));
                start = i + 1;
            }
        }
        parts.add(s.substring(start));
        return parts;
    }

    // ---------------------------------------------------------------- comparing one piece

    private static boolean pieceMatches(String e, String g) {
        if (e.equals(g)) return true;
        // inequalities, matrices and plain words are only compared as text
        if (e.contains("<") || e.contains(">") || e.contains("[") || g.contains("<") || g.contains(">") || g.contains("[")) {
            return false;
        }
        boolean eEq = e.contains("="), gEq = g.contains("=");
        if (eEq != gEq) return false;
        if (eEq) return equationsMatch(e, g);
        Expr ee = new Parser(e).parseAll();
        Expr ge = new Parser(g).parseAll();
        return expressionsMatch(ee, ge, g.contains("."));
    }

    private static boolean expressionsMatch(Expr e, Expr g, boolean givenHasDecimal) {
        Set<String> vars = new LinkedHashSet<>(e.vars());
        vars.addAll(g.vars());
        if (vars.isEmpty()) {
            double a = e.eval(Env.EMPTY), b = g.eval(Env.EMPTY);
            if (Double.isNaN(a) || Double.isNaN(b)) return false;
            if (close(a, b, REL_TOL)) return true;
            return givenHasDecimal && Math.abs(a) > 1e-12 && Math.abs(a - Math.rint(a)) > 1e-12 && close(a, b, ROUNDED_TOL);
        }
        Random rnd = new Random(12345);
        int agree = 0;
        for (int i = 0; i < SAMPLES * 3 && agree < SAMPLES; i++) {
            Env env = Env.random(vars, rnd);
            double a = e.eval(env), b = g.eval(env);
            if (Double.isNaN(a) || Double.isNaN(b) || Double.isInfinite(a) || Double.isInfinite(b)) continue;
            if (!close(a, b, 1e-7)) return false;
            agree++;
        }
        return agree >= 3;
    }

    private static boolean equationsMatch(String e, String g) {
        Expr ef = equationAsExpr(e), gf = equationAsExpr(g);
        Set<String> vars = new LinkedHashSet<>(ef.vars());
        vars.addAll(gf.vars());
        Random rnd = new Random(54321);
        Double ratio = null;
        int agree = 0;
        for (int i = 0; i < SAMPLES * 3 && agree < SAMPLES; i++) {
            Env env = Env.random(vars, rnd);
            double a = ef.eval(env), b = gf.eval(env);
            if (Double.isNaN(a) || Double.isNaN(b) || Double.isInfinite(a) || Double.isInfinite(b)) continue;
            if (Math.abs(a) < 1e-9 && Math.abs(b) < 1e-9) continue;   // on the curve: no information
            if (Math.abs(b) < 1e-9) return false;
            double r = a / b;
            if (ratio == null) ratio = r;
            else if (!close(r, ratio, 1e-6)) return false;
            agree++;
        }
        return agree >= 3;
    }

    private static Expr equationAsExpr(String eq) {
        int i = eq.indexOf('=');
        if (i < 0 || eq.indexOf('=', i + 1) >= 0) throw new IllegalArgumentException("not a single equation");
        Expr left = new Parser(eq.substring(0, i)).parseAll();
        Expr right = new Parser(eq.substring(i + 1)).parseAll();
        return new Expr() {
            public double eval(Env env) { return left.eval(env) - right.eval(env); }
            public Set<String> vars() {
                Set<String> v = new LinkedHashSet<>(left.vars());
                v.addAll(right.vars());
                return v;
            }
        };
    }

    private static boolean close(double a, double b, double tol) {
        double scale = Math.max(Math.abs(a), Math.abs(b));
        return Math.abs(a - b) <= tol * Math.max(1.0, scale);
    }

    // ---------------------------------------------------------------- expression tree

    private interface Expr {
        double eval(Env env);
        default Set<String> vars() { return Set.of(); }
    }

    private static final class Env {
        static final Env EMPTY = new Env(new String[0], new double[0]);
        final String[] names;
        final double[] values;
        Env(String[] names, double[] values) { this.names = names; this.values = values; }
        static Env random(Set<String> vars, Random rnd) {
            String[] n = vars.toArray(new String[0]);
            double[] v = new double[n.length];
            for (int i = 0; i < n.length; i++) v[i] = 0.35 + rnd.nextDouble() * 2.3; // positive: keeps ln and sqrt defined
            return new Env(n, v);
        }
        double get(String name) {
            for (int i = 0; i < names.length; i++) if (names[i].equals(name)) return values[i];
            return Double.NaN;
        }
    }

    // ---------------------------------------------------------------- parser
    //   sum     := term (('+'|'-') term)*
    //   term    := unary ((('*'|'/') unary) | implicit unary)*
    //   unary   := ('-'|'+') unary | power
    //   power   := atom ('^' unary)?
    //   atom    := number | name | func atomArg | '(' sum ')' | '|' sum '|'

    private static final class Parser {
        private final String s;
        private int pos = 0;

        Parser(String s) { this.s = s; }

        Expr parseAll() {
            if (s.isEmpty()) throw new IllegalArgumentException("empty");
            Expr e = sum();
            if (pos != s.length()) throw new IllegalArgumentException("unexpected '" + s.charAt(pos) + "'");
            return e;
        }

        private boolean more() { return pos < s.length(); }
        private char peek() { return s.charAt(pos); }

        private Expr sum() {
            Expr left = term();
            while (more() && (peek() == '+' || peek() == '-')) {
                char op = s.charAt(pos++);
                Expr l = left, r = term();
                left = op == '+' ? binary(l, r, (a, b) -> a + b) : binary(l, r, (a, b) -> a - b);
            }
            return left;
        }

        private Expr term() {
            Expr left = unary();
            while (more()) {
                char c = peek();
                if (c == '*' || c == '/') {
                    pos++;
                    Expr l = left, r = unary();
                    left = c == '*' ? binary(l, r, (a, b) -> a * b) : binary(l, r, (a, b) -> a / b);
                } else if (startsAtom(c)) {
                    Expr l = left, r = unary(); // implicit multiplication: 2x, 3sin(x), (x+1)(x-1)
                    left = binary(l, r, (a, b) -> a * b);
                } else {
                    break;
                }
            }
            return left;
        }

        private boolean startsAtom(char c) {
            return Character.isLetterOrDigit(c) || c == '(' || c == '.' || c == '|';
        }

        private Expr unary() {
            if (more() && peek() == '-') { pos++; Expr u = unary(); return unaryOp(u, a -> -a); }
            if (more() && peek() == '+') { pos++; return unary(); }
            return power();
        }

        private Expr power() {
            Expr base = atom();
            if (more() && peek() == '^') {
                pos++;
                Expr exp = unary();
                return binary(base, exp, Math::pow);
            }
            return base;
        }

        private Expr atom() {
            if (!more()) throw new IllegalArgumentException("unexpected end");
            char c = peek();
            if (c == '(') {
                pos++;
                Expr e = sum();
                expect(')');
                return e;
            }
            if (c == '|') {
                pos++;
                Expr e = sum();
                expect('|');
                return unaryOp(e, Math::abs);
            }
            if (Character.isDigit(c) || c == '.') return number();
            if (Character.isLetter(c)) return nameOrFunction();
            throw new IllegalArgumentException("unexpected '" + c + "'");
        }

        private void expect(char c) {
            if (!more() || peek() != c) throw new IllegalArgumentException("expected '" + c + "'");
            pos++;
        }

        private Expr number() {
            int start = pos;
            while (more() && (Character.isDigit(peek()) || peek() == '.')) pos++;
            double v = Double.parseDouble(s.substring(start, pos));
            return new Expr() { public double eval(Env env) { return v; } };
        }

        private static final String[] FUNCS = {"asin", "acos", "atan", "sqrt", "sin", "cos", "tan", "cot", "sec", "csc", "ln", "log", "exp", "abs"};

        private Expr nameOrFunction() {
            for (String f : FUNCS) {
                if (s.startsWith(f, pos)) {
                    pos += f.length();
                    Expr arg = functionArgument();
                    return unaryOp(arg, function(f));
                }
            }
            if (s.startsWith("pi", pos)) {
                pos += 2;
                return new Expr() { public double eval(Env env) { return Math.PI; } };
            }
            char c = s.charAt(pos++);
            if (c == 'e') {
                return new Expr() { public double eval(Env env) { return Math.E; } };
            }
            String name = String.valueOf(c);
            return new Expr() {
                public double eval(Env env) { return env.get(name); }
                public Set<String> vars() { return Set.of(name); }
            };
        }

        /** sqrt(2), sqrt2, sin3x, lnx, sin^2 x is not supported (falls back to text comparison). */
        private Expr functionArgument() {
            if (more() && peek() == '(') {
                pos++;
                Expr e = sum();
                expect(')');
                return e;
            }
            // bare argument: a number, a constant or one variable, with an optional power (ln x^2 = ln(x^2))
            Expr atom = atom();
            if (more() && peek() == '^') {
                pos++;
                Expr exp = unary();
                return binary(atom, exp, Math::pow);
            }
            return atom;
        }

        private DoubleUnaryOperator function(String f) {
            return switch (f) {
                case "sqrt" -> Math::sqrt;
                case "sin" -> Math::sin;
                case "cos" -> Math::cos;
                case "tan" -> Math::tan;
                case "cot" -> a -> 1.0 / Math.tan(a);
                case "sec" -> a -> 1.0 / Math.cos(a);
                case "csc" -> a -> 1.0 / Math.sin(a);
                case "asin" -> Math::asin;
                case "acos" -> Math::acos;
                case "atan" -> Math::atan;
                case "ln", "log" -> Math::log;
                case "exp" -> Math::exp;
                case "abs" -> Math::abs;
                default -> throw new IllegalArgumentException("unknown function " + f);
            };
        }

        private static Expr binary(Expr l, Expr r, java.util.function.DoubleBinaryOperator op) {
            return new Expr() {
                public double eval(Env env) { return op.applyAsDouble(l.eval(env), r.eval(env)); }
                public Set<String> vars() {
                    Set<String> v = new LinkedHashSet<>(l.vars());
                    v.addAll(r.vars());
                    return v;
                }
            };
        }

        private static Expr unaryOp(Expr e, DoubleUnaryOperator op) {
            return new Expr() {
                public double eval(Env env) { return op.applyAsDouble(e.eval(env)); }
                public Set<String> vars() { return e.vars(); }
            };
        }
    }
}
