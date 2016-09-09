package de.f0rke.pageindicatorexample;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by moritzkochig on 07.09.16.
 *
 * @author f0rke
 */
public class SampleContentContainer implements Parcelable {
    private final String text;

    SampleContentContainer(String text) {
        this.text = text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.text);
    }

    protected SampleContentContainer(Parcel in) {
        this.text = in.readString();
    }

    public static final Creator<SampleContentContainer> CREATOR = new Creator<SampleContentContainer>() {
        @Override
        public SampleContentContainer createFromParcel(Parcel source) {
            return new SampleContentContainer(source);
        }

        @Override
        public SampleContentContainer[] newArray(int size) {
            return new SampleContentContainer[size];
        }
    };

    public String getText() {
        return text;
    }
}
