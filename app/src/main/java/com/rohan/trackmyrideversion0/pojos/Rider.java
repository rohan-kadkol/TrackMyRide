package com.rohan.trackmyrideversion0.pojos;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.libraries.places.api.model.Place;

public class Rider implements Parcelable {
    public static final int STATUS_TO_BE_PICKED = 0;
    public static final int STATUS_IN_RIDE = 1;

    public String id;
    public String name;
    public int status;
    public int orderNumber;
    public double originLat;
    public double originLng;
    public double destinationLat;
    public double destinationLng;

    public Rider() {}

    public Rider(String id, String name, int status, int orderNumber, double originLat, double originLng, double destinationLat, double destinationLng) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.orderNumber = orderNumber;
        this.originLat = originLat;
        this.originLng = originLng;
        this.destinationLat = destinationLat;
        this.destinationLng = destinationLng;
    }

    protected Rider(Parcel in) {
        id = in.readString();
        name = in.readString();
        status = in.readInt();
        orderNumber = in.readInt();
        originLat = in.readDouble();
        originLng = in.readDouble();
        destinationLat = in.readDouble();
        destinationLng = in.readDouble();
    }

    public static final Creator<Rider> CREATOR = new Creator<Rider>() {
        @Override
        public Rider createFromParcel(Parcel in) {
            return new Rider(in);
        }

        @Override
        public Rider[] newArray(int size) {
            return new Rider[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeInt(status);
        parcel.writeInt(orderNumber);
        parcel.writeDouble(originLat);
        parcel.writeDouble(originLng);
        parcel.writeDouble(destinationLat);
        parcel.writeDouble(destinationLng);
    }
}
