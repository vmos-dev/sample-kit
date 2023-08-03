package com.samplekit.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class AppItem implements Parcelable {

    private String appName;
    private String packageName;
    private long versionCode;
    private String versionName;
    private String iconUri;

    public AppItem() {
    }


    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getIconUri() {
        return iconUri;
    }

    public void setIconUri(String iconUri) {
        this.iconUri = iconUri;
    }

    public long getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(long versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.appName);
        dest.writeString(this.packageName);
        dest.writeLong(this.versionCode);
        dest.writeString(this.versionName);
        dest.writeString(this.iconUri);
    }

    public void readFromParcel(Parcel source) {
        this.appName = source.readString();
        this.packageName = source.readString();
        this.versionCode = source.readLong();
        this.versionName = source.readString();
        this.iconUri = source.readString();
    }

    protected AppItem(Parcel in) {
        this.appName = in.readString();
        this.packageName = in.readString();
        this.versionCode = in.readLong();
        this.versionName = in.readString();
        this.iconUri = in.readString();
    }

    public static final Creator<AppItem> CREATOR = new Creator<AppItem>() {
        @Override
        public AppItem createFromParcel(Parcel source) {
            return new AppItem(source);
        }

        @Override
        public AppItem[] newArray(int size) {
            return new AppItem[size];
        }
    };
}
