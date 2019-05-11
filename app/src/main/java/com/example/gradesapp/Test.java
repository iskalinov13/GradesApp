package com.example.gradesapp;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Test implements Cloneable {
    List<Question> questions;

    public Test() {
        questions = new ArrayList<>();
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public void delete() {
        questions.clear();
    }

    public void fill(Mat entry, List<Rect> boxes) {
        delete();
        int distanceBetweenBoxes = 0;
        boolean sameLine= true;
        boolean sameGroup = true;
        if (boxes != null && boxes.size() > 0) {
            Question question = new Question();
            questions.add(question);
            OMRdata OMRdata, OMRdataPrevious = null;
            for (int c = 0; c < boxes.size(); c++) {
                Rect box = boxes.get(c);
                boolean value = readBox(entry, box);
                OMRdata = new OMRdata(value, box);
                if (OMRdataPrevious != null) {
                    sameLine= OMRdata.sameLine(OMRdataPrevious);
                }
                if (OMRdataPrevious != null && c > 1) {
                    sameGroup = OMRdata.sameGroup(OMRdataPrevious, distanceBetweenBoxes);
                }
                if (sameLine&& !sameGroup) {
                    question = new Question();
                    questions.add(question);
                }
                if (!sameLine) {
                    question = new Question();
                    questions.add(question);
                }
                question.addOption(OMRdata);
                OMRdataPrevious = OMRdata;
                if (c == 1) {
                    if (question.getNumeroOptions() == 2) {
                        distanceBetweenBoxes = question.getOption(0).distance(question.getOption(1));
                    } else {
                        questions.clear();
                        return;
                    }
                }
            }
        }
    }

    public boolean readBox(Mat entry, Rect box) {
        Rect correctedRec = new Rect();
        reduceBB(box, correctedRec);
        Mat cut = entry.submat(correctedRec);
        List<MatOfPoint> hz = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.threshold(cut, cut, 0, 255, Imgproc.THRESH_OTSU);
        Imgproc.findContours(cut, hz, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        return (hz.size() >= 3);
    }


    void reduceBB(Rect entry, Rect exit) {
        double margin = 0.20;
        int marginHorizontal = (int) (entry.width * margin);
        int marginVertical = (int) (entry.height * margin);
        exit.x = entry.x + marginHorizontal;
        exit.y = entry.y + marginVertical;
        exit.width = entry.width - 2 * marginHorizontal;
        exit.height = entry.height - 2 * marginVertical;
    }


    public int getNumberOfQuestions() {
        return questions.size();
    }

    public Question getQuestion(int p) {
        return questions.get(p);
    }

    public String getCodification() {
        String codification = "";
        for (int p = 0; p < getNumberOfQuestions(); p++) {
            Question question = getQuestion(p);
            for (int r = 0; r < question.getNumeroOptions(); r++) {
                OMRdata OMRdata = question.getOption(r);
                boolean value = OMRdata.getValue();
                if (value) codification = codification + 'T';
                else codification = codification + 'F';
            }
            codification = codification + '*';
        }
        return codification;
    }

    public String getCorrection(String solution) {
        String codification = getCodification();
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"+codification);
        String correction = "";
        int score = 0;
        if (sameStructure(solution)) {
            for (int i = 0; i < codification.length(); i++) {
                switch (codification.charAt(i)) {
                    case 'T':
                        score++;
                        correction = correction + solution.charAt(i);
                        break;
                    case 'F':
                        correction = correction + ("" + solution.charAt(i)).toLowerCase();
                        break;
                    case '*':
                        correction = correction + '*';
                }
            }
        }
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"+correction+"SCOREEEEEEEEEEE"+score);
        return correction;
    }

    public boolean sameStructure(String solution) {
        String codification = getCodification();
        boolean result = (codification.length() == solution.length());
        if (result) {
            for (int i = 0; i < codification.length(); i++) {
                if (codification.charAt(i) == '*' || solution.charAt(i) == '*')
                    result = result && (codification.charAt(i) == '*' && solution.charAt(i) == '*');
            }
        }
        return result;
    }

    public double getScore(double maxScore, String correction, boolean ScoreNegativaPermit) {
        double Score = 0;
        double numberOfQuestions = getNumberOfQuestions();
        double optiones = 0;
        double validOptions = 0;
        double successes = 0;
        double errors = 0;
        double answers= 0;
        double valueQuestion = (maxScore / numberOfQuestions);
        double myScore = 0;
        String codification = getCodification();
        if (sameStructure(correction)) {
            for (int i = 0; i < codification.length(); i++) {
                switch (correction.charAt(i)) {
                    case 'T':
                        optiones++;
                        answers++;
                        successes++;
                        validOptions++;
                        myScore++;
                        break;
                    case 't':
                        optiones++;
                        validOptions++;
                        break;
                    case 'F':
                        optiones++;
                        answers++;
                        errors++;
                        break;
                    case 'f':
                        optiones++;
                        break;
                    case '*':
                        if (validOptions > 0 && answers> 0) {
                            Score += (valueQuestion * (successes / validOptions) - (valueQuestion * (errors / (optiones - 1))));
                        }
                        optiones = 0;
                        validOptions = 0;
                        answers= 0;
                        successes = 0;
                        errors = 0;
                        break;
                }
            }
        }
        if (!ScoreNegativaPermit) Score = Math.max(0, Score);
        return Score;
    }
}
