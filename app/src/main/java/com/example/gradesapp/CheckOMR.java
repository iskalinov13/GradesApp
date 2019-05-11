package com.example.gradesapp;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CheckOMR {
    MatOfPoint2f approx = new MatOfPoint2f();
    Mat exitGray, lowStep;
    String reading;
    boolean readingStable;
    int repetitions;
    Scalar colorRespuesta;
    Scalar colorRespuestaIncorrecta;
    Scalar colorRespuestaCorrecta;
    Scalar colorNumeros;
    Scalar colorBordeBox;
    Scalar colorFondoBox;
    Scalar colorSuspenso;
    Scalar colorAprobado;
    Scalar colorTexto;

    public CheckOMR() {
        exitGray = new Mat();
        lowStep = new Mat();
        reading = "";
        repetitions = 0;
        readingStable = false;
        colorNumeros = new Scalar(77, 40, 0);
        colorBordeBox = new Scalar(0, 0, 0);
        colorFondoBox = new Scalar(217, 217, 217);
        colorRespuesta = new Scalar(0, 102, 204);
        colorRespuestaCorrecta = new Scalar(102, 153, 0);
        colorRespuestaIncorrecta = new Scalar(204, 0, 0);
        colorSuspenso = new Scalar(204, 0, 0);
        colorAprobado = new Scalar(102, 153, 0);
        colorTexto = new Scalar(0, 102, 204);
    }

    public boolean proccess(Mat entryGray, Mat exit, Test test) {
        binarizationAdaptive(entryGray, exitGray);
        erosionar(exitGray, exitGray);
        List<Rect> segments = segment(exitGray);
        sortedSegments(segments);
        segments = removeSegments(segments, mostCommonArea(segments));
        restore(entryGray, segments, exitGray);
        test.fill(entryGray, segments);
        Imgproc.cvtColor(exitGray, exit, Imgproc.COLOR_GRAY2RGBA);
        readingStable = readingStable(test.getCodification());
        return readingStable;
    }

    void erosionar(Mat entry, Mat exit) {
        double tam = 5;
        Mat SE = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(tam, tam));
        Imgproc.erode(entry, exit, SE);
    }

    void binarizationAdaptive(Mat entry, Mat exit) {
        int contrast = 40;
        int size = 15;
        Imgproc.adaptiveThreshold(entry, exit, 255,
                Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY,
                size, contrast);
    }

    List<Rect> segment(Mat entry) {
        List<Rect> listrect = new ArrayList<>();
        List<MatOfPoint> blobs = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(entry, blobs, hierarchy,
                Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_NONE);
        int minimumSize = 15;
        int maximumSize = 60;
        for (int i = 0; blobs != null && i < blobs.size(); i++) {
            MatOfPoint c = blobs.get(i);
            double[] data = hierarchy.get(0, i);
            int parent = (int) data[3];
            int firstChild = (int) data[2];
            if (parent < 0 && firstChild >= 0)
                continue;
            Rect BB = Imgproc.boundingRect(c);
            if (BB.height < minimumSize || BB.width < minimumSize) {
                continue;
            }
            if (BB.height > maximumSize || BB.width > maximumSize) {
                continue;
            }

            float wf = BB.width;
            float hf = BB.height;
            float ratio = wf / hf;
            if (ratio < 0.75 || ratio > 1.25) {
                continue;
            }
            if (!hasFourVertices(c)) {
                continue;
            }
            if (!hasSquareShape(c, BB)) {
                continue;
            }
            final Point P1 = new Point(BB.x, BB.y);
            final Point P2 = new Point(BB.x + BB.width, BB.y + BB.height);
            listrect.add(new Rect((int) P1.x, (int) P1.y, (int) (P2.x - P1.x), (int) (P2.y - P1.y)));
        }
        return listrect;
    }

    public boolean hasFourVertices(MatOfPoint c) {
        double precisionAproximacion = Imgproc.arcLength(new MatOfPoint2f(c.toArray()), true) * 0.03;
        Imgproc.approxPolyDP(new MatOfPoint2f(c.toArray()), approx, precisionAproximacion, true);
        return (approx.size().height == 4.0);
    }

    public boolean hasSquareShape(MatOfPoint c, Rect BB) {
        double threshold = 1;
        Point[] pd = new Point[4];
        pd[0] = new Point(BB.x, BB.y);
        pd[1] = new Point(BB.x + BB.width - 1, BB.y);
        pd[2] = new Point(BB.x + BB.width - 1, BB.y + BB.height - 1);
        pd[3] = new Point(BB.x, BB.y + BB.height - 1);
        MatOfPoint d = new MatOfPoint(pd);
        double distance = Imgproc.matchShapes(c, d, Imgproc.CV_CONTOURS_MATCH_I1, 0);
        return (distance <= threshold);
    }

    void restore(Mat entryGray, List<Rect> segments, Mat exitGray) {
        for (int s = 0; s < segments.size(); s++) {
            Rect BB = segments.get(s);
            entryGray.submat(BB).copyTo(exitGray.submat(BB));
        }
    }

    void drawBoxes(Mat entry, Test test) {
        for (int p = 0; p < test.getNumberOfQuestions(); p++) {
            Question question = test.getQuestion(p);
            for (int o = 0; o < question.getNumeroOptions(); o++) {
                OMRdata OMRdata = question.getOption(o);
                Rect Box = OMRdata.getBox();
                boolean value = OMRdata.getValue();
                if (value) {
                    Imgproc.rectangle(entry, Box.tl(), Box.br(), colorFondoBox, -1);
                    drawCross(entry, Box, colorRespuesta);
                    Imgproc.rectangle(entry, Box.tl(), Box.br(), colorBordeBox, 3);
                } else {
                    Imgproc.rectangle(entry, Box.tl(), Box.br(), colorFondoBox, -1);
                    Imgproc.rectangle(entry, Box.tl(), Box.br(), colorBordeBox, 3);
                }
            }
        }
    }

    void drawCorrection(Mat entry, Test test, String correction) {
        for (int i = 0, p = 0, o = 0; i < correction.length(); i++) {
            if (correction.charAt(i) == '*') {
                p++;
                o = 0;
            } else {
                Question question = test.getQuestion(p);
                OMRdata OMRdata = question.getOption(o);
                Rect Box = OMRdata.getBox();
                switch (correction.charAt(i)) {
                    case 'T':
                        Imgproc.rectangle(entry, Box.tl(), Box.br(), colorFondoBox, -1);
                        drawTick(entry, Box, colorRespuestaCorrecta);
                        Imgproc.rectangle(entry, Box.tl(), Box.br(), colorBordeBox, 3);
                        break;
                    case 't':
                        Imgproc.rectangle(entry, Box.tl(), Box.br(), colorFondoBox, -1);
                        Imgproc.circle(entry, new Point(Box.x + Box.width / 2, Box.y + Box.height / 2), (int) (Box.height * 0.20), colorRespuestaCorrecta, -1);
                        Imgproc.rectangle(entry, Box.tl(), Box.br(), colorBordeBox, 3);
                        break;
                    case 'F':
                        Imgproc.rectangle(entry, Box.tl(), Box.br(), colorFondoBox, -1);
                        drawCross(entry, Box, colorRespuestaIncorrecta);
                        Imgproc.rectangle(entry, Box.tl(), Box.br(), colorBordeBox, 3);
                        break;
                    case 'f':
                        Imgproc.rectangle(entry, Box.tl(), Box.br(), colorFondoBox, -1);
                        Imgproc.rectangle(entry, Box.tl(), Box.br(), colorBordeBox, 3);
                        break;
                }
                o++;
            }
        }
    }

    public void drawCross(Mat entry, Rect Box, Scalar color) {
        Point supIzq = new Point(Box.x + Box.width * 0.25, Box.y + Box.height * 0.25);
        Point supDer = new Point(Box.x + Box.width * 0.75, Box.y + Box.height * 0.25);
        Point infDer = new Point(Box.x + Box.width * 0.75, Box.y + Box.height * 0.75);
        Point infIzq = new Point(Box.x + Box.width * 0.25, Box.y + Box.height * 0.75);
        Imgproc.line(entry, supIzq, infDer, color, 3);
        Imgproc.line(entry, infIzq, supDer, color, 3);
    }

    public void drawTick(Mat entry, Rect Box, Scalar color) {
        Point izq = new Point(Box.x + Box.width * 0.25, Box.y + Box.height / 2);
        Point inf = new Point(Box.x + Box.width * 0.40, Box.y + Box.height * 0.80);
        Point der = new Point(Box.x + Box.width * 0.8, Box.y + Box.height * 0.20);
        Imgproc.line(entry, izq, inf, color, 3);
        Imgproc.line(entry, inf, der, color, 3);
    }

//    public boolean isContourSquare(MatOfPoint thisContour) {
//        Rect ret = null;
//        MatOfPoint2f thisContour2f = new MatOfPoint2f();
//        MatOfPoint approxContour = new MatOfPoint();
//        MatOfPoint2f approxContour2f = new MatOfPoint2f();
//        thisContour.convertTo(thisContour2f, CvType.CV_32FC2);
//        Imgproc.approxPolyDP(thisContour2f, approxContour2f, 2, true);
//        approxContour2f.convertTo(approxContour, CvType.CV_32S);
//        if (approxContour.size().height == 4) {
//            ret = Imgproc.boundingRect(approxContour);
//        }
//        return (ret != null);
//    }

    double mostCommonArea(List<Rect> segments) {
        double areaFrequency = -1, areaFrequencyA, areaFrequencyB;
        int frequency = 0;
        int frequencyTmp;
        for (int a = 0; a < segments.size(); a++) {
            areaFrequencyA = segments.get(a).size().area();
            frequencyTmp = 0;
            for (int b = 0; b < segments.size(); b++) {
                areaFrequencyB = segments.get(b).size().area();
                if (areaFrequencyA == areaFrequencyB) frequencyTmp++;
            }
            if (frequencyTmp > frequency) {
                areaFrequency = areaFrequencyA;
                frequency = frequencyTmp;
            }
        }
        return areaFrequency;
    }

    public List<Rect> removeSegments(List<Rect> contours, double areaFrequency) {
        List<Rect> exit = new ArrayList<Rect>();
        double margin = 0.50 * areaFrequency;
        for (int c = 0; c < contours.size(); c++) {
            double area = contours.get(c).size().area();
            if (Math.abs(area - areaFrequency) <= margin) {
                exit.add(contours.get(c));
            }
        }
        return exit;
    }

    // Reference: http://stackoverflow.com/questions/9109890/android-java-how-to-sort-a-list-of-objects-by-a-certain-value-within-the-object
    void sortedSegments(List<Rect> segmentsD) {
        try {
            Collections.sort(segmentsD, new Comparator<Rect>() {
                public int compare(Rect obj1, Rect obj2) {
                    if (Math.abs(obj1.y - obj2.y) > (obj1.height)) {
                        return (Integer.valueOf(obj1.y).compareTo(obj2.y));
                    } else {
                        return (Integer.valueOf(obj1.x).compareTo(obj2.x)); // To compare integer values
                    }
                }
            });
        }catch(Exception e){

            Log.e("Tester", e.toString());
            segmentsD.clear();
        }
    }

    boolean readingStable(String reading) {
        if (this.reading.equals(reading) && reading.length() > 2) {
            repetitions++;
        } else {
            this.reading = reading;
            repetitions = 1;
        }
        return repetitions > 2;
    }

    public void showScore(Mat entry, double score, double maxScore) {
        String message = String.format("%.2f", score);
        Point center = new Point(entry.width() * 0.94, entry.height() * 0.105);
        Point point;
        int ratio;
        if (score < 10) {
            ratio = (int) Math.round(entry.height() * 0.08);
            point = new Point(entry.width() * 0.906, entry.height() * 0.12);
        } else {
            ratio = (int) Math.round(entry.height() * 0.09);
            point = new Point(entry.width() * 0.897, entry.height() * 0.12);
        }
        if (score < (maxScore / 2)) {
            Imgproc.circle(entry, center, ratio, colorBordeBox, 10);
            Imgproc.circle(entry, center, ratio, colorSuspenso, 5);
            showMessage(entry, point, message, 1.2, colorBordeBox, colorSuspenso);
        } else {
            Imgproc.circle(entry, center, ratio, colorBordeBox, 10);
            Imgproc.circle(entry, center, ratio, colorAprobado, 5);
            showMessage(entry, point, message, 1.2, colorBordeBox, colorAprobado);
        }
    }

    public void showMessage(Mat entry, Point point, String message, double fontScale, Scalar colorBorde, Scalar colorRelleno) {
        int fontFace = 4;
        int thickness = 5;
        Imgproc.putText(entry, message,
                point, fontFace, fontScale,
                colorBorde, thickness, 8, false);
        Imgproc.putText(entry, message,
                point, fontFace, fontScale,
                colorRelleno, thickness / 2, 8, false);
    }

    public void showMessageTexto(Mat entry, Point point, String message, double fontScale, Scalar color) {
        int fontFace = 1;
        int thickness = 2;
        Imgproc.putText(entry, message,
                point, fontFace, fontScale,
                colorBordeBox, thickness, 8, false);
        Imgproc.putText(entry, message,
                point, fontFace, fontScale,
                color, thickness / 2, 8, false);
    }

    public void showTitle(Mat entry, String title, String user, String date) {
        Point point1 = new Point(entry.width() * 0.01, entry.height() * 0.04);
        showMessageTexto(entry, point1, title, 2, colorTexto);
        Point point2 = new Point(entry.width() * 0.695, entry.height() * 0.935);
        showMessageTexto(entry, point2, user, 2, colorTexto);
        point2 = new Point(entry.width() * 0.695, entry.height() * 0.985);
        showMessageTexto(entry, point2, date, 2, colorTexto);
    }
}

