package de.f0rke.pageindicatorexample;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by moritzkochig on 07.09.16.
 *
 * @author Moritz Köchig
 *         © mobile concepts GmbH 2016
 */
public class ContentContainer implements Parcelable {
    private final String text;

    ContentContainer(String text) {
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

    protected ContentContainer(Parcel in) {
        this.text = in.readString();
    }

    public static final Creator<ContentContainer> CREATOR = new Creator<ContentContainer>() {
        @Override
        public ContentContainer createFromParcel(Parcel source) {
            return new ContentContainer(source);
        }

        @Override
        public ContentContainer[] newArray(int size) {
            return new ContentContainer[size];
        }
    };

    public String getText() {
        return text;
    }
}
