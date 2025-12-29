package com.example.rozmie;

import java.util.Comparator;

class Population$2 implements Comparator<TestCase> {
   private final Population population;

   Population$2(Population population) {
      this.population = population;
   }

   @Override
   public int compare(TestCase o1, TestCase o2) {
      return Double.compare(o2.fitness, o1.fitness);
   }
}
