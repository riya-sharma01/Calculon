package com.calculon.dto;

import java.util.List;

/**
 * Maps the on-disk question-bank JSON schema (src/main/resources/questions/*.json) 1:1, so Jackson can
 * deserialize a whole file with no custom logic. See JsonQuestionSeeder for how this is turned into entities.
 *
 * Example file shape:
 * {
 *   "domain": "Algebra",
 *   "levels": [
 *     { "level": 1, "levelName": "Foundation", "questions": [ { "id": "alg3-f-set-01", "type": "MULTIPLE_CHOICE", ... } ] }
 *   ]
 * }
 */
public class QuestionBankFile {

    private String domain;
    private List<LevelBlock> levels;

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public List<LevelBlock> getLevels() { return levels; }
    public void setLevels(List<LevelBlock> levels) { this.levels = levels; }

    public static class LevelBlock {
        private int level;
        private String levelName;
        private List<QuestionBlock> questions;

        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }

        public String getLevelName() { return levelName; }
        public void setLevelName(String levelName) { this.levelName = levelName; }

        public List<QuestionBlock> getQuestions() { return questions; }
        public void setQuestions(List<QuestionBlock> questions) { this.questions = questions; }
    }

    public static class QuestionBlock {
        private String id;
        private String type;
        private String topic;
        private String prompt;
        private List<OptionBlock> options;
        /** Correct answer for NUMERIC/EXPRESSION questions. Absent (null) for MULTIPLE_CHOICE. */
        private String answer;
        private String explanation;
        private int xp;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }

        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }

        public List<OptionBlock> getOptions() { return options; }
        public void setOptions(List<OptionBlock> options) { this.options = options; }

        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }

        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }

        public int getXp() { return xp; }
        public void setXp(int xp) { this.xp = xp; }
    }

    public static class OptionBlock {
        private String text;
        private boolean correct;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public boolean isCorrect() { return correct; }
        public void setCorrect(boolean correct) { this.correct = correct; }
    }
}
