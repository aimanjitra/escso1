package com.example.rozmie;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class Population {
    private TupleList tl;
    private TestCaseOperation tco;
    private ArrayList<TestCase> myPopulation = new ArrayList<>();

    public Population(TupleList tl, TestCaseOperation tco) {
        this.tl = tl;
        this.tco = tco;
    }

    public int size() {
        return this.myPopulation.size();
    }

    public void addNewPopulation(TestCase tc) {
        this.myPopulation.add(tc);
    }

    public void addNewPopulation(int[] tc) {
        // assume calculateWeight returns double; adapt if it returns int
        double weight = this.tl.calculateWeight(tc);
        TestCase tempTC = new TestCase(tc, this.tco.convertTCToPoint(tc), weight);
        this.addNewPopulation(tempTC);
    }

    // Sort ascending by fitness (fitness is double)
    public void sortAscending() {
        Collections.sort(this.myPopulation, Comparator.comparingDouble(tc -> tc.fitness));
    }

    // Sort descending by fitness (fitness is double)
    public void sortDescending() {
        Collections.sort(this.myPopulation, (tc1, tc2) -> Double.compare(tc2.fitness, tc1.fitness));
    }

    public TestCase getPopulationByIndex(int index) {
        return this.myPopulation.get(index);
    }

    public TestCase getLowestFitnessPopulation() {
        return Collections.min(this.myPopulation, Comparator.comparingDouble(tc -> tc.fitness));
    }

    public TestCase getHighestFitnessPopulation() {
        return Collections.max(this.myPopulation, Comparator.comparingDouble(tc -> tc.fitness));
    }

    public int getPopulationSize() {
        return this.myPopulation.size();
    }

    // now return double because fitness is double
    public double getLowestFitness() {
        return this.getLowestFitnessPopulation().fitness;
    }

    public double getHighestFitness() {
        return this.getHighestFitnessPopulation().fitness;
    }

    public Iterator<TestCase> getIterator() {
        return this.myPopulation.iterator();
    }

    public static void main(String[] args) {
    }
}
