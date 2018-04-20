package com.example.sampleandroid.data.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.sampleandroid.data.config.Constants;


/**
 * Created by KCH on 2018-04-02.
 */

public class IntentData implements Parcelable {
    public int aniType;
    public String authId;
    public String joinType;
    public boolean isRegist;
    public String link;
    public String title;
    public String screenType;

    public IntentData(){
        aniType = Constants.VIEW_ANIMATION.ANI_NONE;
        authId = "";
        joinType = "";
        isRegist = false;
        link = "";
        title = "";
        screenType = "";
    }

    protected IntentData(Parcel in) {
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in)
    {
        aniType = in.readInt();
        authId = in.readString();
        joinType = in.readString();
        isRegist = in.readByte() != 0;
        link = in.readString();
        title = in.readString();
        screenType = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(aniType);
        dest.writeString(authId);
        dest.writeString(joinType);
        dest.writeByte((byte) (isRegist ? 1 : 0));
        dest.writeString(link);
        dest.writeString(title);
        dest.writeString(screenType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<IntentData> CREATOR = new Creator<IntentData>() {
        @Override
        public IntentData createFromParcel(Parcel in) {
            return new IntentData(in);
        }

        @Override
        public IntentData[] newArray(int size) {
            return new IntentData[size];
        }
    };
}

