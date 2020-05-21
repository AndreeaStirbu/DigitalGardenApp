package com.example.digitalgarden.Model;

/**
 * @Author: Andreea Stirbu
 * @Since: 27/03/2020.
 *
 * Simple OOP Java class that stores the details of a Plant
 */

public class Plant {
    private String mPlantId;
    private String mPlantName;
    private String mPlantSpecie;
    private String mSoilType;
    private String mLightLevel;
    private int mMinTemp;
    private int mMaxTemp;
    private String mImageSrc;
    private String mNotes;
    private int mLastWatering;
    private int mWateringFrequency;
    private long mNextNotificationDate;
    private boolean mNeedsWater;
    private int mNotificationCode;

    public void setPlantId(String id) { mPlantId = id ; }
    public String getPlantId() { return mPlantId; }

    public void setPlantName(String plantName) { mPlantName = plantName; }
    public String getPlantName() { return mPlantName; }

    public void setPlantSpecie(String mPlantSpecie) { this.mPlantSpecie = mPlantSpecie; }
    public String getPlantSpecie() { return mPlantSpecie; }

    public void setSoilType(String mSoilType) { this.mSoilType = mSoilType; }
    public String getSoilType() { return mSoilType; }

    public void setLightLevel(String mLightLevel) { this.mLightLevel = mLightLevel; }
    public String getLightLevel() {
        return mLightLevel;
    }

    public void setMinTemp(int mMinTemp) { this.mMinTemp = mMinTemp; }
    public int getMinTemp() {
        return mMinTemp;
    }

    public void setMaxTemp(int mMaxTemp) {
        this.mMaxTemp = mMaxTemp;
    }
    public int getMaxTemp() {
        return mMaxTemp;
    }

    public void setImageSrc(String mImageSrc) { this.mImageSrc = mImageSrc; }
    public String getImageSrc() { return mImageSrc; }

    public void setNotes(String notes) { mNotes = notes; }
    public String getNotes() { return mNotes; }

    public void setLastWatering(int mLastWatering) { this.mLastWatering = mLastWatering; }
    public int getLastWatering() { return mLastWatering; }

    public void setWateringFrequency(int wateringFrequency) { mWateringFrequency = wateringFrequency; }
    public int getWateringFrequency() { return mWateringFrequency; }

    public void setNextNotificationDate(long nextNotificationDate) { this.mNextNotificationDate = nextNotificationDate; }
    public long getNextNotificationDate() { return mNextNotificationDate; }

    public void setNeedsWater(Boolean watered) { mNeedsWater = watered; }
    public boolean getNeedsWater() { return mNeedsWater; }

    public void setNotificationCode(int mNotificationCode) { this.mNotificationCode = mNotificationCode; }
    public int getNotificationCode() { return mNotificationCode; }
}
