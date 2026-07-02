package com.onepiecerpg.api.service;

public final class ExperienceCalculator {

  private static final double EXP_BASE = 30.0;
  private static final double EXP_EXPOSANT = 1.645;

  private ExperienceCalculator() {
  }

  public static int experienceRequise(int niveau) {
    return (int) Math.round(EXP_BASE * Math.pow(niveau, EXP_EXPOSANT));
  }
}