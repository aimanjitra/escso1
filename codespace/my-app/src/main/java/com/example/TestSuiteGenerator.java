package com.example;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import com.example.rozmie.TWayUtil2D;
import com.example.rozmie.TestCase;

public class TestSuiteGenerator {

    public static double[] c = new double[360];
    public static final int MAX_ITER = 100; // Maximum iterations per optimization
    public static final double S = 20.0;    // Step size factor
    public static final int numberOfPopulation = 10; // Population size

    // single Random instance (reuse)
    private static final Random rnd = new Random();

    // stored problem definition (restored so constructor + generateTestSuite() match your Main)
    private final int[] value;
    private final int strength;

    // Constructor expected by your Main.java
    public TestSuiteGenerator(int[] value, int strength) {
        this.value = value.clone();
        this.strength = strength;
        initializeRoulleteWheel();
    }

    // Public API expected by Main.java
    public String generateTestSuite() {
        StringBuilder out = new StringBuilder();

        TWayUtil2D tt = new TWayUtil2D(value);
        boolean[] pInvolve = new boolean[value.length];
        for (int i = 0; i < value.length; i++) pInvolve[i] = true;
        tt.addSetting(pInvolve, strength);

        int count = 1;

        while (!tt.allTuplesCovered()) {
            int t = 0;

            ArrayList<TestCase> pop = tt.createRandomPopulation(numberOfPopulation);
            TestCase.setDescendingOrder();
            Collections.sort(pop);

            // ensure initial valid pop
            while (pop.get(0).fitness == 0) {
                pop = tt.createRandomPopulation(numberOfPopulation);
                Collections.sort(pop);
            }

            TestCase bestTC = pop.get(0).clone();

            // SARSA variables (2 actions: 0 = oscillate, 1 = jump)
            double[][] Q = new double[2][2];
            double alpha = 0.1;
            double gamma = 0.9;
            double epsilon = 0.2;

            while (t < MAX_ITER) {
                double rg = S - ((S) * t / (double) (MAX_ITER));
                double r = rnd.nextDouble() * rg;

                Iterator<TestCase> itr = pop.iterator();
                int bestX = bestTC.getX();
                int bestY = bestTC.getY();
                int currentState = 0;

                while (itr.hasNext()) {
                    TestCase temp = itr.next();
                    int currentX = temp.getX();
                    int currentY = temp.getY();
                    int action;

                    // epsilon-greedy between 2 actions
                    if (rnd.nextDouble() < epsilon) {
                        action = rnd.nextInt(2);
                    } else {
                        double q0 = Q[currentState][0];
                        double q1 = Q[currentState][1];
                        action = (q1 > q0) ? 1 : 0;
                    }

                    int newX = currentX, newY = currentY;
                    double oldFitness = temp.fitness;

                    if (action == 0) {
                        // Oscillate (Sand Cat)
                        int thetaDeg = getTheta();
                        double theta = thetaDeg / 360.0;
                        double randX = Math.abs((rnd.nextDouble() * bestX) - currentX);
                        double randY = Math.abs((rnd.nextDouble() * bestY) - currentY);
                        newX = (int) Math.round(bestX - (r * randX) * Math.cos(2 * Math.PI * theta));
                        newY = (int) Math.round(bestY - (r * randY) * Math.sin(2 * Math.PI * theta));
                    } else {
                        // Jump (local exploratory)
                        newX = (int) Math.round(r * (bestX - (rnd.nextDouble() * currentX)));
                        newY = (int) Math.round(r * (bestY - (rnd.nextDouble() * currentY)));
                    }

                    // update individual (guarded)
                    try {
                        tt.updateTestCase(temp, newX, newY);
                    } catch (Throwable e) {
                        System.err.println("[UPDATE-ERROR] updateTestCase failed. tempTC=" + java.util.Arrays.toString(temp.getTestCase())
                                + " newX=" + newX + " newY=" + newY + " tt=" + (tt == null ? "null" : tt.getClass().getName()));
                        e.printStackTrace(System.err);
                        throw e;
                    }

                    double newFitness = temp.fitness;
                    double reward = newFitness - oldFitness;

                    int nextState = (newFitness >= bestTC.fitness) ? 1 : 0;

                    double na0 = Q[nextState][0], na1 = Q[nextState][1];
                    int nextAction = (na1 > na0) ? 1 : 0;

                    Q[currentState][action] += alpha * (reward + gamma * Q[nextState][nextAction] - Q[currentState][action]);

                    currentState = nextState;
                } // end population iteration

                // Re-sort and accept improvement if present
                Collections.sort(pop);
                TestCase bestCandidate = pop.get(0);

                if (bestCandidate.fitness > bestTC.fitness) {
                    try {
                        tt.updateTestCase(bestTC, bestCandidate.getPoint());
                    } catch (Throwable e) {
                        System.err.println("[UPDATE-ERROR] updateTestCase(bestTC, bestCandidate) failed");
                        e.printStackTrace(System.err);
                        throw e;
                    }
                    bestTC = bestCandidate.clone();
                }

                t++;
            } // end optimization

            // record / output bestTC
            out.append(count++).append(") ");
            out.append(getTestCaseAsString(bestTC.getTestCase())).append("\n");

            try {
                tt.deleteTuples(bestTC.getTestCase());
            } catch (Throwable e) {
                System.err.println("[DELETE-ERROR] deleteTuples failed. bestTC=" + java.util.Arrays.toString(bestTC.getTestCase()));
                e.printStackTrace(System.err);
                throw e;
            }
        } // while !allTuplesCovered

        return out.toString();
    }

    // ---------- Roulette wheel & theta helpers ----------
    public static void initializeRoulleteWheel() {
        int[] degree = new int[360];
        int sum = 0;
        for (int i = 0; i < degree.length; i++) {
            degree[i] = i + 1;
            sum += degree[i];
        }
        double[] p = new double[360];
        for (int i = 0; i < degree.length; i++) {
            p[i] = (double) degree[i] / sum;
        }
        c[0] = p[0];
        for (int i = 1; i < degree.length; i++) {
            c[i] = p[i] + c[i - 1];
        }
    }

    public static int getTheta() {
        double r = rnd.nextDouble();
        for (int i = 0; i < c.length; i++) {
            if (r <= c[i]) return i;
        }
        return 0;
    }

    // ---------- Utility ----------
    private String getTestCaseAsString(int[] tc) {
        StringBuilder sb = new StringBuilder();
        for (int val : tc) {
            sb.append(val).append(" ");
        }
        return sb.toString().trim();
    }

    private double safeCalculateWeight(TWayUtil2D tt, int[] tc) {
    if (tt == null || tc == null) return -1.0;
    // Work on a clone to protect caller arrays from accidental mutation.
    int[] copy = tc.clone();
    double w;
    try {
        w = tt.calculateWeight(copy);
    } catch (Throwable e) {
        // If calculateWeight throws, treat as invalid candidate and log for diagnosis.
        System.err.println("[SAFE-CALCWEIGHT-EX] calculateWeight threw: " + e);
        e.printStackTrace(System.err);
        return -1.0;
    }
    if (Double.isNaN(w)) return -1.0;
    return w; // may be -1.0 (invalid), 0.0 (no new coverage), or >0 (useful)
}
}
