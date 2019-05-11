package com.example.gradesapp;


public class TestCard {
    private String title;
    private String description;
    private String creator;
    private int numberOfQuestions;
    private long creationDate;
    private String solution;
    private boolean active;

    public TestCard(String title, String solution) {
        this.title = title;
        this.solution = solution;
    }

    public TestCard() {}

    public TestCard(String title, String description, String creator, int numberOfQuestions, long creationDate, String solution, boolean active) {
        this.title = title;
        this.description = description;
        this.creator = creator;
        this.numberOfQuestions = numberOfQuestions;
        this.creationDate = creationDate;
        this.solution = solution;
        this.active = active;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public int getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(int numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public static class TestCardBuilder {
        private String title = "";
        private String description = "";
        private String creator = "";
        private int numberOfQuestions = 0;
        private long creationDate;
        private String solution = "";
        private boolean active = false;

        public TestCardBuilder withtitle(String title) {
            this.title = title;
            return this;
        }

        public TestCardBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public TestCardBuilder withCreator(String creator) {
            this.creator = creator;
            return this;
        }

        public TestCardBuilder withnumberOfQuestions(int numberOfQuestions) {
            this.numberOfQuestions = numberOfQuestions;
            return this;
        }

        public TestCardBuilder withCreationDate(long creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public TestCardBuilder withSolution(String solution) {
            this.solution = solution;
            return this;
        }

        public TestCardBuilder isActive(boolean active) {
            this.active = active;
            return this;
        }

        public TestCard build() {
            return new TestCard(title, description, creator, numberOfQuestions, creationDate, solution, active);
        }
    }
}
