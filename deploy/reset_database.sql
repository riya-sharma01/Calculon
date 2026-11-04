-- Calculon: wipe the Aiven database so the app can rebuild it from scratch on its next start.
--
-- WHY DROP TABLES (not just DELETE rows): the app's schema is created by Hibernate. On MySQL it creates
-- the difficulty columns as native ENUM types, and Hibernate's "update" mode never alters an existing
-- column. The old tables only allow the old difficulty names (BEGINNER, EXPERT ...), so inserting the new
-- ones (FOUNDATION, STANDARD, HARD ...) would be rejected. Dropping the tables lets Hibernate recreate
-- them correctly on boot.
--
-- THIS DELETES EVERYTHING, including user accounts. Everyone has to register again.
-- Run it ONCE, before the new backend's first start. Do not run it on later deploys: after this,
-- redeploys are safe and only insert new questions.

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS user_question_attempts;
DROP TABLE IF EXISTS user_lesson_progress;
DROP TABLE IF EXISTS user_achievements;
DROP TABLE IF EXISTS question_options;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS lessons;
DROP TABLE IF EXISTS math_domains;
DROP TABLE IF EXISTS achievements;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- Check: this should list no tables.
SHOW TABLES;
