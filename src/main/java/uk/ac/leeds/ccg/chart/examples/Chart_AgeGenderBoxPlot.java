/**
 * Copyright 2012 Andy Turner, The University of Leeds, UK
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package uk.ac.leeds.ccg.chart.examples;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import uk.ac.leeds.ccg.chart.core.Chart_AgeGender;
import uk.ac.leeds.ccg.generic.core.Generic_Environment;
import uk.ac.leeds.ccg.generic.io.Generic_Defaults;
import uk.ac.leeds.ccg.math.Math_BigDecimal;
import uk.ac.leeds.ccg.stats.Generic_Statistics;

/**
 * An example of generating an Age by Gender Population Box Plot Visualization.
 */
public class Chart_AgeGenderBoxPlot extends Chart_AgeGender {

    public Chart_AgeGenderBoxPlot(Generic_Environment e) {
        super(e);
    }

    /**
     * @param e The Generic_Environment.
     * @param es The ExecutorService.
     * @param f The Path.
     * @param format The format.
     * @param title The title.
     * @param dataWidth The data width.
     * @param dataHeight The data height.
     * @param xAxisLabel The x axis label.
     * @param yAxisLabel The y axis label.
     * @param drawOriginLinesOnPlot If {@code true} then origin lines are drawn
     * on the plot.
     * @param ageInterval The age interval.
     * @param startAgeOfEndYearInterval The start age of the end year interval.
     * @param dpc The decimal place precision for calculations.
     * @param dpd The decimal place precision for display.
     * @param rm The RoundingMode.
     */
    public Chart_AgeGenderBoxPlot(Generic_Environment e, ExecutorService es,
            Path f, String format, String title, int dataWidth, int dataHeight,
            String xAxisLabel, String yAxisLabel, boolean drawOriginLinesOnPlot,
            int ageInterval, int startAgeOfEndYearInterval, int dpc, int dpd,
            RoundingMode rm) {
        super(e);
        init(es, f, format, title, dataWidth, dataHeight, xAxisLabel,
                yAxisLabel, drawOriginLinesOnPlot, ageInterval,
                startAgeOfEndYearInterval, dpc,
                dpd, rm);
    }

    public static void main(String[] args) {
        try {
            Generic_Environment e = new Generic_Environment(new Generic_Defaults());

            /*
         * Initialise title and Path to write image to
             */
            String title;
            Path file;
            String format = "PNG";
            if (args.length != 2) {
                System.out.println("Expected 2 args: args[0] title; args[1] Path."
                        + " Recieved " + args.length + " args.");
                // Use defaults
                title = "Age Gender Population Box Plot";
                System.out.println("Use default title: " + title);
                Path outdir = e.files.getOutputDir();
                file = Paths.get(outdir.toString(), title.replace(" ", "_") + "." + format);
                System.out.println("Use default Path: " + file.toString());
            } else {
                title = args[0];
                file = Paths.get(args[1]);
            }
            int dataWidth = 1000;//250;
            int dataHeight = 500;
            String xAxisLabel = "Population";
            String yAxisLabel = "Age";
            boolean drawOriginLinesOnPlot = true;
            int ageInterval = 5;
            int startAgeOfEndYearInterval = 70;//95;
            int decimalPlacePrecisionForCalculations = 10;
            int decimalPlacePrecisionForDisplay = 3;
            RoundingMode rm = RoundingMode.HALF_UP;
            ExecutorService es = Executors.newSingleThreadExecutor();
            Chart_AgeGenderBoxPlot plot = new Chart_AgeGenderBoxPlot(e, es, file,
                    format, title, dataWidth, dataHeight, xAxisLabel, yAxisLabel,
                    drawOriginLinesOnPlot, ageInterval, startAgeOfEndYearInterval,
                    decimalPlacePrecisionForCalculations,
                    decimalPlacePrecisionForDisplay, rm);
            plot.setData(plot.getDefaultData());
            plot.vis.getHeadlessEnvironment();
            //plot.run();
            plot.start();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);

        }
    }

    @Override
    public void drawData() {
        drawBoxplots();
    }

    public void drawBoxplots() {
        int ageInterval = getAgeInterval();
        Line2D abLine2D;
        TreeMap<Integer, BigDecimal[]> femaleBoxPlotData;
        femaleBoxPlotData = (TreeMap<Integer, BigDecimal[]>) data[0];
        TreeMap<Integer, BigDecimal[]> maleBoxPlotData;
        maleBoxPlotData = (TreeMap<Integer, BigDecimal[]>) data[1];

        Iterator<Map.Entry<Integer, BigDecimal[]>> ite;
        Map.Entry<Integer, BigDecimal[]> entry;
        Integer age;
        BigDecimal[] stats;
        /*
         * result[0] = sum;
         * result[1] = mean;
         * result[2] = median;
         * result[3] = q1;
         * result[4] = q3;
         * result[5] = mode;
         * result[6] = min;
         * result[7] = max;
         */
        // boxHeight is in pixels. 4 pixels are used to seperate boxPlots 
        // between age intervals. If boxHeight is less than 3 then each boxplot 
        // will display more like a line. If the boxHeight is an odd number 
        // then this allows for a central centre line that is one pixel thick.
//        int boxHeight = (Math_BigDecimal.divideRoundIfNecessary(
//                BigDecimal.valueOf(ageInterval),
//                getCellHeight(),
//                0,
//                getRoundingMode()).intValueExact()) - 4;
        int boxHeight = (Math_BigDecimal.divideRoundIfNecessary(
                BigDecimal.valueOf(ageInterval), getCellHeight(), 0,
                getRoundingMode()).intValue()) - 4;
        int whiskerHeight = boxHeight / 2;

        // Calculate plot drawing metrics
        BigDecimal cellWidth = getCellWidth();
        /*
         * Draw Female Box Plots
         */
        ite = femaleBoxPlotData.entrySet().iterator();
        while (ite.hasNext()) {
            entry = ite.next();
            age = entry.getKey();
            stats = entry.getValue();

            // Calculate plot drawing metrics
//            int boxWidth = Math_BigDecimal.divideRoundIfNecessary(
//                    stats[4].subtract(stats[3]), cellWidth, 0,
//                    getRoundingMode()).intValueExact();
            int boxWidth = Math_BigDecimal.divideRoundIfNecessary(
                    stats[4].subtract(stats[3]), cellWidth, 0,
                    getRoundingMode()).intValue();

            int boxTopRow = coordinateToScreenRow(BigDecimal.valueOf(age + 1)) + 2;
            //int boxTopRow = coordinateToScreenRow(BigDecimal.valueOf(age));
            int boxMiddleRow = boxTopRow + (boxHeight / 2);
            int boxBottomRow = boxTopRow + boxHeight;
            int q1Col = coordinateToScreenCol(stats[3]);
            int q3Col = coordinateToScreenCol(stats[4]);

            // Draw min line
            setPaint(Color.DARK_GRAY);
            int minCol = coordinateToScreenCol(stats[6]);
            System.out.println();
            abLine2D = new Line2D.Double(minCol, boxMiddleRow, q1Col, boxMiddleRow);
            draw(abLine2D);
            abLine2D = new Line2D.Double(
                    minCol, boxMiddleRow + (whiskerHeight / 2),
                    minCol, boxMiddleRow - (whiskerHeight / 2));
            draw(abLine2D);

            // Draw max line
            int maxCol = coordinateToScreenCol(stats[7]);
            abLine2D = new Line2D.Double(maxCol, boxMiddleRow, q3Col,
                    boxMiddleRow);
            draw(abLine2D);
            abLine2D = new Line2D.Double(
                    maxCol, boxMiddleRow + (whiskerHeight / 2),
                    maxCol, boxMiddleRow - (whiskerHeight / 2));
            draw(abLine2D);
            // Draw box after drawing min and max lines to neaten overlaps
            setPaint(Color.WHITE);
            fillRect(q1Col, boxTopRow, boxWidth, boxHeight);
            setPaint(Color.DARK_GRAY);
            Rectangle2D r2;
            r2 = new Rectangle2D.Double(q1Col, boxTopRow, boxWidth, boxHeight);
            setPaint(Color.DARK_GRAY);
            draw(r2);

            // Draw median line
            int medianCol = coordinateToScreenCol(stats[2]);
            abLine2D = new Line2D.Double(medianCol, boxTopRow, medianCol,
                    boxBottomRow);
            draw(abLine2D);
        }
        /*
         * result[0] = sum;
         * result[1] = mean;
         * result[2] = median;
         * result[3] = q1;
         * result[4] = q3;
         * result[5] = mode;
         * result[6] = min;
         * result[7] = max;
         */
 /*
         * Draw Male Box Plots
         */
        ite = maleBoxPlotData.entrySet().iterator();
        while (ite.hasNext()) {
            entry = ite.next();
            age = entry.getKey();
            stats = entry.getValue();

            // Calculate plot drawing metrics
            int boxWidth = Math_BigDecimal.divideRoundIfNecessary(
                    stats[4].subtract(stats[3]), getCellWidth(), 0,
                    getRoundingMode()).intValueExact();
            int boxTopRow = coordinateToScreenRow(BigDecimal.valueOf(age + 1)) + 2;
            //int boxTopRow = coordinateToScreenRow(BigDecimal.valueOf(age));
            int boxMiddleRow = boxTopRow + (boxHeight / 2);
            int boxBottomRow = boxTopRow + boxHeight;
            int q1Col = coordinateToScreenCol(stats[3].negate());
            int q3Col = coordinateToScreenCol(stats[4].negate());

            // Draw min line
            setPaint(Color.DARK_GRAY);
            int minCol = coordinateToScreenCol(stats[6].negate());
            abLine2D = new Line2D.Double(minCol, boxMiddleRow, q1Col, boxMiddleRow);
            draw(abLine2D);
            abLine2D = new Line2D.Double(minCol, boxMiddleRow + (whiskerHeight / 2),
                    minCol, boxMiddleRow - (whiskerHeight / 2));
            draw(abLine2D);

            // Draw max line
            setPaint(Color.DARK_GRAY);
            int maxCol = coordinateToScreenCol(stats[7].negate());
            abLine2D = new Line2D.Double(maxCol, boxMiddleRow, q3Col, boxMiddleRow);
            draw(abLine2D);
            abLine2D = new Line2D.Double(maxCol, boxMiddleRow + (whiskerHeight / 2),
                    maxCol, boxMiddleRow - (whiskerHeight / 2));
            draw(abLine2D);

            // Draw box after drawing min and max lines to neaten overlaps
            setPaint(Color.DARK_GRAY);
            Rectangle2D r2;
            r2 = new Rectangle2D.Double(q3Col, boxTopRow, boxWidth, boxHeight);
            setPaint(Color.WHITE);
            fillRect(q3Col, boxTopRow, boxWidth, boxHeight);
            setPaint(Color.DARK_GRAY);
            draw(r2);

            // Draw median line
            int medianCol = coordinateToScreenCol(stats[2].negate());
            abLine2D = new Line2D.Double(medianCol, boxTopRow, medianCol, boxBottomRow);
            draw(abLine2D);
        }
    }

    @Override
    public Object[] getDefaultData() {
        int ageInterval = 5;
        int startAgeOfEndYearInterval = 70;//95;
        decimalPlacePrecisionForCalculations = 10;
        RoundingMode rm = RoundingMode.HALF_UP;
        Object[] r = getDefaultData(ageInterval, startAgeOfEndYearInterval,
                decimalPlacePrecisionForCalculations, rm);
        return r;
    }

    /**
     * Override this method to use other data
     *
     * @param femalePopAge0
     * @param malePopAge0
     * @return
     */
    private static Object[] getPopulationData(int femalePopAge0, int malePopAge0) {
        Object[] r = new Object[2];
        TreeMap<Integer, BigDecimal> femaleData = new TreeMap<>();
        TreeMap<Integer, BigDecimal> maleData = new TreeMap<>();
        BigDecimal pop;
        BigDecimal change;
        int age;
        pop = new BigDecimal("" + femalePopAge0);
        change = new BigDecimal("0.94");
        for (age = 0; age < 5; age++) {
            femaleData.put(age, pop);
            pop = pop.multiply(change);
        }
        change = new BigDecimal("0.95");
        for (age = 5; age < 10; age++) {
            femaleData.put(age, pop);
            pop = pop.multiply(change);
        }
        change = new BigDecimal("0.96");
        for (age = 10; age < 15; age++) {
            femaleData.put(age, pop);
            pop = pop.multiply(change);
        }
        change = new BigDecimal("0.97");
        for (age = 15; age < 20; age++) {
            femaleData.put(age, pop);
            pop = pop.multiply(change);
        }
        change = new BigDecimal("0.99");
        for (age = 20; age < 60; age++) {
            femaleData.put(age, pop);
            pop = pop.multiply(change);
        }
        change = new BigDecimal("0.97");
        for (age = 60; age < 80; age++) {
            femaleData.put(age, pop);
            pop = pop.multiply(change);
        }
        change = new BigDecimal("0.75");
        for (age = 80; age < 100; age++) {
            femaleData.put(age, pop);
            pop = pop.multiply(change);
        }
        pop = new BigDecimal("" + malePopAge0);
        change = new BigDecimal("0.93");
        for (age = 0; age < 5; age++) {
            maleData.put(age, pop);
            pop = pop.multiply(change);
        }
        change = new BigDecimal("0.94");
        for (age = 5; age < 10; age++) {
            maleData.put(age, pop);
            pop = pop.multiply(change);
        }
        change = new BigDecimal("0.95");
        for (age = 10; age < 15; age++) {
            maleData.put(age, pop);
            pop = pop.multiply(change);
        }
        change = new BigDecimal("0.96");
        for (age = 15; age < 20; age++) {
            maleData.put(age, pop);
            pop = pop.multiply(change);
        }
        change = new BigDecimal("0.98");
        for (age = 20; age < 60; age++) {
            maleData.put(age, pop);
            pop = pop.multiply(change);
        }
        change = new BigDecimal("0.7");
        for (age = 60; age < 70; age++) {
            maleData.put(age, pop);
            pop = pop.multiply(change);
        }
        change = new BigDecimal("0.5");
        for (age = 70; age < 100; age++) {
            maleData.put(age, pop);
            pop = pop.multiply(change);
        }
//        Integer maxAge = 99;
//        BigDecimal maxCount;
//        maxCount = new BigDecimal("" + Math.max(femalePopAge0, malePopAge0));
        r[0] = femaleData;
        r[1] = maleData;
//        result[2] = maxAge;
//        result[3] = maxCount;
        return r;
    }

    /**
     * Returns a sample dataset.
     *
     * @param ageInterval Age interval.
     * @param rm The RoundingMode.
     * @param saeyi The start age of the end year interval.
     * @param dpc decimalPlacePrecisionForCalculations
     * @return The dataset.
     */
    public static Object[] getDefaultData(int ageInterval,
            int saeyi, int dpc, RoundingMode rm) {
        //int startAgeOfEndYearInterval = getStartAgeOfEndYearInterval();
        Object[] r = new Object[5];
        TreeMap<Integer, BigDecimal[]> femaleBoxPlotStats = new TreeMap<>();
        TreeMap<Integer, BigDecimal[]> maleBoxPlotStats = new TreeMap<>();
        Object[] data10000 = getPopulationData(10000, 10000);
        TreeMap<Integer, BigDecimal> female10000 = (TreeMap<Integer, BigDecimal>) data10000[0];
        TreeMap<Integer, BigDecimal> male10000 = (TreeMap<Integer, BigDecimal>) data10000[1];
        Object[] data9000 = getPopulationData(9000, 9000);
        TreeMap<Integer, BigDecimal> female9000 = (TreeMap<Integer, BigDecimal>) data9000[0];
        TreeMap<Integer, BigDecimal> male9000 = (TreeMap<Integer, BigDecimal>) data9000[1];
        Object[] data9900 = getPopulationData(9900, 9900);
        TreeMap<Integer, BigDecimal> female9900 = (TreeMap<Integer, BigDecimal>) data9900[0];
        TreeMap<Integer, BigDecimal> male9900 = (TreeMap<Integer, BigDecimal>) data9900[1];
        Object[] data9950 = getPopulationData(9950, 9950);
        TreeMap<Integer, BigDecimal> female9950 = (TreeMap<Integer, BigDecimal>) data9950[0];
        TreeMap<Integer, BigDecimal> male9950 = (TreeMap<Integer, BigDecimal>) data9950[1];
        Object[] data9800 = getPopulationData(9800, 9800);
        TreeMap<Integer, BigDecimal> female9800 = (TreeMap<Integer, BigDecimal>) data9800[0];
        TreeMap<Integer, BigDecimal> male9800 = (TreeMap<Integer, BigDecimal>) data9800[1];
        Iterator<Integer> iterator;
        Integer age = 0;
        BigDecimal pop10000;
        BigDecimal pop9000;
        BigDecimal pop9900;
        BigDecimal pop9950;
        BigDecimal pop9800;
        BigDecimal maxValue;
        maxValue = BigDecimal.ZERO;
        iterator = ((TreeMap<Integer, BigDecimal>) data10000[0]).keySet().iterator();
        ArrayList<BigDecimal> values = null;
        while (iterator.hasNext()) {
            pop10000 = BigDecimal.ZERO;
            pop9000 = BigDecimal.ZERO;
            pop9900 = BigDecimal.ZERO;
            pop9950 = BigDecimal.ZERO;
            pop9800 = BigDecimal.ZERO;
            for (int interval = 0; interval < ageInterval; interval++) {
                age = iterator.next();
                if (age >= saeyi) {
                    pop10000 = values.get(0).add(female10000.get(age));
                    pop9000 = values.get(1).add(female9000.get(age));
                    pop9900 = values.get(2).add(female9900.get(age));
                    pop9950 = values.get(3).add(female9950.get(age));
                    pop9800 = values.get(4).add(female9800.get(age));
                } else {
                    pop10000 = pop10000.add(female10000.get(age));
                    pop9000 = pop9000.add(female9000.get(age));
                    pop9900 = pop9900.add(female9900.get(age));
                    pop9950 = pop9950.add(female9950.get(age));
                    pop9800 = pop9800.add(female9800.get(age));
                }
            }
            if (age < saeyi) {
                values = new ArrayList<>();
            }
            maxValue = maxValue.max(pop10000);
            maxValue = maxValue.max(pop9000);
            maxValue = maxValue.max(pop9900);
            maxValue = maxValue.max(pop9950);
            maxValue = maxValue.max(pop9800);
            values.add(pop10000);
            values.add(pop9000);
            values.add(pop9900);
            values.add(pop9950);
            values.add(pop9800);
            BigDecimal[] boxPlotStats;
            boxPlotStats = Generic_Statistics.getSummaryStatistics_0(values,
                    dpc, rm);
            System.out.println("Female age " + age);
            if (age < saeyi) {
                femaleBoxPlotStats.put(age, boxPlotStats);
            } else {
                femaleBoxPlotStats.put(saeyi + ageInterval,
                        boxPlotStats);
            }
        }
//maxX = maxValue;
//maxValue = BigDecimal.ZERO;
        iterator = ((TreeMap<Integer, BigDecimal>) data10000[1]).keySet().iterator();
        while (iterator.hasNext()) {
            pop10000 = BigDecimal.ZERO;
            pop9000 = BigDecimal.ZERO;
            pop9900 = BigDecimal.ZERO;
            pop9950 = BigDecimal.ZERO;
            pop9800 = BigDecimal.ZERO;
            for (int interval = 0; interval < ageInterval; interval++) {
                age = iterator.next();
                if (age >= saeyi) {
                    pop10000 = values.get(0).add(male10000.get(age));
                    pop9000 = values.get(1).add(male9000.get(age));
                    pop9900 = values.get(2).add(male9900.get(age));
                    pop9950 = values.get(3).add(male9950.get(age));
                    pop9800 = values.get(4).add(male9800.get(age));
                } else {
                    pop10000 = pop10000.add(male10000.get(age));
                    pop9000 = pop9000.add(male9000.get(age));
                    pop9900 = pop9900.add(male9900.get(age));
                    pop9950 = pop9950.add(male9950.get(age));
                    pop9800 = pop9800.add(male9800.get(age));
                }
            }
            if (age < saeyi) {
                values = new ArrayList<>();
            }
            maxValue = maxValue.max(pop10000);
            maxValue = maxValue.max(pop9000);
            maxValue = maxValue.max(pop9900);
            maxValue = maxValue.max(pop9950);
            maxValue = maxValue.max(pop9800);
            values.add(pop10000);
            values.add(pop9000);
            values.add(pop9900);
            values.add(pop9950);
            values.add(pop9800);
            System.out.println("Male age " + age);
            BigDecimal[] boxPlotStats;
            boxPlotStats = Generic_Statistics.getSummaryStatistics_0(values,
                    dpc, rm);
            if (age < saeyi) {
                maleBoxPlotStats.put(age, boxPlotStats);
            } else {
                maleBoxPlotStats.put(saeyi + ageInterval,
                        boxPlotStats);
            }
        }
        r[0] = femaleBoxPlotStats;
        r[1] = maleBoxPlotStats;
        //minX = maxValue.negate();
        r[2] = maxValue;
        r[3] = BigDecimal.valueOf(100);
        r[4] = BigDecimal.ZERO;
        return r;
    }

    /**
     * Returns a sample dataset.
     *
     * @return The dataset.
     */
    private static Object[] getDefaultData(RoundingMode rm,
            int decimalPlacePrecisionForCalculations) {
        Object[] result = new Object[2];
        TreeMap<Integer, BigDecimal[]> femaleBoxPlotStats = new TreeMap<>();
        TreeMap<Integer, BigDecimal[]> maleBoxPlotStats = new TreeMap<>();
        Object[] data10000 = getPopulationData(10000, 10000);
        TreeMap<Integer, BigDecimal> female10000 = (TreeMap<Integer, BigDecimal>) data10000[0];
        TreeMap<Integer, BigDecimal> male10000 = (TreeMap<Integer, BigDecimal>) data10000[1];
        Object[] data9000 = getPopulationData(9000, 9000);
        TreeMap<Integer, BigDecimal> female9000 = (TreeMap<Integer, BigDecimal>) data9000[0];
        TreeMap<Integer, BigDecimal> male9000 = (TreeMap<Integer, BigDecimal>) data9000[1];
        Object[] data9900 = getPopulationData(9900, 9900);
        TreeMap<Integer, BigDecimal> female9900 = (TreeMap<Integer, BigDecimal>) data9900[0];
        TreeMap<Integer, BigDecimal> male9900 = (TreeMap<Integer, BigDecimal>) data9900[1];
        Object[] data9950 = getPopulationData(9950, 9950);
        TreeMap<Integer, BigDecimal> female9950 = (TreeMap<Integer, BigDecimal>) data9950[0];
        TreeMap<Integer, BigDecimal> male9950 = (TreeMap<Integer, BigDecimal>) data9950[1];
        Object[] data9800 = getPopulationData(9800, 9800);
        TreeMap<Integer, BigDecimal> female9800 = (TreeMap<Integer, BigDecimal>) data9800[0];
        TreeMap<Integer, BigDecimal> male9800 = (TreeMap<Integer, BigDecimal>) data9800[1];
        Iterator<Integer> iterator;
        Integer age;
        BigDecimal pop;
        BigDecimal maxValue;
        maxValue = BigDecimal.ZERO;
        iterator = ((TreeMap<Integer, BigDecimal>) data10000[0]).keySet().iterator();
        while (iterator.hasNext()) {
            age = iterator.next();
            pop = female10000.get(age);
            ArrayList<BigDecimal> values = new ArrayList<>();
            maxValue = maxValue.max(pop);
            values.add(pop);
            pop = female9000.get(age);
            maxValue = maxValue.max(pop);
            values.add(pop);
            pop = female9900.get(age);
            maxValue = maxValue.max(pop);
            values.add(pop);
            pop = female9950.get(age);
            maxValue = maxValue.max(pop);
            values.add(pop);
            pop = female9800.get(age);
            maxValue = maxValue.max(pop);
            values.add(pop);
            BigDecimal[] boxPlotStats;
            boxPlotStats = Generic_Statistics.getSummaryStatistics_0(values,
                    decimalPlacePrecisionForCalculations, rm);
            femaleBoxPlotStats.put(age, boxPlotStats);
        }
        maxValue = BigDecimal.ZERO;
        iterator = ((TreeMap<Integer, BigDecimal>) data10000[0]).keySet().iterator();
        while (iterator.hasNext()) {
            age = iterator.next();
            pop = male10000.get(age);
            ArrayList<BigDecimal> values = new ArrayList<>();
            maxValue = maxValue.max(pop);
            values.add(pop);
            pop = male9000.get(age);
            maxValue = maxValue.max(pop);
            values.add(pop);
            pop = male9900.get(age);
            maxValue = maxValue.max(pop);
            values.add(pop);
            pop = male9950.get(age);
            maxValue = maxValue.max(pop);
            values.add(pop);
            pop = male9800.get(age);
            maxValue = maxValue.max(pop);
            values.add(pop);
            BigDecimal[] boxPlotStats;
            boxPlotStats = Generic_Statistics.getSummaryStatistics_0(values,
                    decimalPlacePrecisionForCalculations, rm);
            maleBoxPlotStats.put(age, boxPlotStats);
        }
        result[0] = femaleBoxPlotStats;
        result[1] = maleBoxPlotStats;
        return result;
    }

    @Override
    public void drawTitle(String title) {
        super.drawTitle(title);
        int barHeight = Math_BigDecimal.divideRoundIfNecessary(
                BigDecimal.valueOf(getAgeInterval()), getCellHeight(), 0,
                getRoundingMode()).intValue();
        extraHeightTop += barHeight;
    }
}
