package com.bry.adcafe.models;

public class AgeGroup {
    private int mStartingAge;
    private int mFinishAge;

    public AgeGroup(int startingAge, int finishingAge){
        this.mStartingAge = startingAge;
        this.mFinishAge = finishingAge;
    }

    public AgeGroup(){}

    public int getStartingAge() {
        return mStartingAge;
    }

    public void setStartingAge(int mStartingAge) {
        this.mStartingAge = mStartingAge;
    }

    public int getFinishAge() {
        return mFinishAge;
    }

    public void setFinishAge(int mFinishAge) {
        this.mFinishAge = mFinishAge;
    }
}
