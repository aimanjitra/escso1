package com.example.rozmie;

import java.util.Comparator;

class Population$1 implements Comparator<TestCase> {
   private final Population population;

   Population$1(Population population) {
      this.population = population;
   }

   @Override
   public int compare(TestCase o1, TestCase o2) {
      return Double.compare(o1.fitness, o2.fitness);
   }
}
