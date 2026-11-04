# Deploying Calculon

```
Browser ──▶ Render Static Site (frontend/)  ──▶  Render Web Service (backend/, Docker)  ──▶  Aiven MySQL
```

GitHub is the source: pushing to `main` makes Render rebuild. Keep your **existing** Render services
(do not create new ones) so the URL on your résumé stays the same.

## 0. Before you start

- Aiven console: your MySQL service must show **Running**. Free plans can be powered off when idle; if it is
  off, power it on and wait until it is Running.
- Have three values from Aiven → your service → Overview: **host**, **port**, **password**
  (user is `avnadmin`, database `defaultdb`). Never paste the password into chat, a commit, or a file in the repo.

## 1. Reset the database (once)

Connect with the MySQL client (it asks for the password, so it never appears on screen):

```
& "C:\Program Files\MySQL\MySQL Server 9.5\bin\mysql.exe" -h <HOST> -P <PORT> -u avnadmin -p --ssl-mode=REQUIRED defaultdb
```

At the `mysql>` prompt run `source <full path>/deploy/reset_database.sql` (forward slashes), or paste its
contents. `SHOW TABLES;` should then list nothing. This deletes all users; everyone registers again.
Do this once only. Later deploys never need it.

## 2. Push the code

From the repo folder:

```
git rm -r --cached --ignore-unmatch frontend/node_modules backend/target
git add -A
git status          # read it: no node_modules, no .env, no passwords
git commit -m "Production config for Render + Aiven"
git push origin main
```

## 3. Backend on Render (Web Service)

Settings: **Runtime** Docker · **Root Directory** `backend` · **Branch** `main` · **Health Check Path** `/api/domains`.

**Environment** tab. Delete any older `SPRING_DATASOURCE_*`, `APP_*` or `PORT` variables first: values set
there silently override this file's. Then set exactly these:

| Name | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | `jdbc:mysql://<HOST>:<PORT>/defaultdb?sslMode=REQUIRED` |
| `DB_USERNAME` | `avnadmin` |
| `DB_PASSWORD` | the Aiven password (copy it straight from the Aiven console) |
| `JWT_SECRET` | a random string of at least 32 characters (below) |
| `CORS_ALLOWED_ORIGINS` | your frontend's exact address, e.g. `https://calculon-azfa.onrender.com` (no trailing slash) |

- `DB_URL` must be the **JDBC** form above. Aiven's `mysql://user:pass@host:port/db` URI will not work.
- Generate `JWT_SECRET` in PowerShell:
  `$b = New-Object byte[] 48; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)`
- Do not set `PORT`. Render sets it and the app reads it.

Then **Manual Deploy → Clear build cache & deploy**. In **Logs**, after a minute or two, look for:

```
Question bank 'Algebra': 264 inserted, 0 already present.
JsonQuestionSeeder finished: 932 questions inserted, 0 already present across 6 file(s).
```

If a variable is missing the log says `Could not resolve placeholder 'DB_URL'` (or whichever one).

## 4. Frontend on Render (Static Site)

Settings: **Root Directory** `frontend` · **Build Command** `npm install && npm run build` ·
**Publish Directory** `dist` · **Branch** `main` · Auto-Deploy **Yes**.

**Environment**: `VITE_API_BASE_URL` = `https://<your-backend-service>.onrender.com/api`
(must end in `/api`, no trailing slash). This is read at **build** time, so after changing it, run
**Manual Deploy → Clear build cache & deploy**.

**Redirects/Rewrites** tab: add a rule, source `/*`, destination `/index.html`, action **Rewrite**.
Without it, refreshing on `/domains` or `/leaderboard` gives a 404.

## 5. Verify

1. `https://<backend>/api/domains` in a browser lists 6 domains.
2. `https://<backend>/api/leaderboard` returns `[]` (not 403).
3. In Aiven's SQL client, `SELECT COUNT(*) FROM questions;` gives 932, and `math_domains` 6, `lessons` 30.
4. Open the frontend in a private window, register, finish a lesson, and check the dashboard and leaderboard.

## Troubleshooting

| What you see | Cause |
|---|---|
| `Port scan timeout` | The app is not listening on `$PORT`. Check `server.port=${PORT:8080}` is in application.properties and `PORT` is not set by hand. |
| `Could not resolve placeholder 'X'` | Environment variable `X` is missing on the backend service. |
| `Communications link failure` / `Connection refused` | Wrong `DB_URL` host/port, Aiven service powered off, or `SPRING_PROFILES_ACTIVE` is not `prod`. |
| `Access denied for user` | Wrong `DB_USERNAME` / `DB_PASSWORD`. |
| Browser console shows a CORS error | `CORS_ALLOWED_ORIGINS` doesn't exactly match the address bar (https vs http, trailing slash). |
| Frontend loads but every request fails | `VITE_API_BASE_URL` wrong or missing `/api`, or not rebuilt after changing it. |
| Frontend is still the old version | Check the service's repo, branch and Root Directory, then run Clear build cache & deploy, then hard-refresh (Ctrl+Shift+R). |
| First visit takes about a minute | Free Render services sleep when idle and wake on the next request. |

## Adding or changing questions later

Add or edit a file in `backend/src/main/resources/questions/`, keeping every question `id` unique, then push.
On start the seeder inserts only ids it hasn't seen, so it never duplicates or wipes. It does **not** update
questions that already exist: to fix the wording of an existing one, change it directly in the database or
reset (step 1). Renaming the difficulty levels needs a reset too.
