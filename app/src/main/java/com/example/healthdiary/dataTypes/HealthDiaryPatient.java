package com.example.healthdiary.dataTypes;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Objects;

public class HealthDiaryPatient implements Parcelable {
    private long id = -2;
    private String firstNames, lastName, ssn, addressLine, zip, country;
    private Date dob;


    /** only for testing purposes. */
    public HealthDiaryPatient(){
        this.firstNames = "Maximilian";
        this.lastName = "Testović";
        this.ssn = "0123456789";
        this.addressLine = "Straße 1";
        this.zip = "plz";
        this.country = "AT";
        this.dob = new Date(666666666666L);
    }

    public HealthDiaryPatient(String firstNames, String lastName, String ssn, String addressLine, String zipCode, String country, Date birthDate){
        this.firstNames = firstNames;
        this.lastName = lastName;
        this.ssn = ssn;
        this.addressLine = addressLine;
        this.zip = zipCode;
        this.country = country;
        this.dob = birthDate;
    }
    public HealthDiaryPatient(long id, String firstNames, String lastName, String ssn, String addressLine, String zipCode, String country, long birthDateUnixEpochMs){
        this.id = id;
        this.firstNames = firstNames;
        this.lastName = lastName;
        this.ssn = ssn;
        this.addressLine = addressLine;
        this.zip = zipCode;
        this.country = country;
        this.dob = new Date(birthDateUnixEpochMs);
    }

    public long getId() {
        return id;
    }
    public void setId(long id){
        this.id = id;
    }
    public String getFirstNames() {
        return firstNames;
    }
    public String getLastName() {
        return lastName;
    }
    public String getSsn() {
        return ssn;
    }
    public String getAddressLine() {
        return addressLine;
    }
    public Date getDob() {
        return dob;
    }
    public long getDobTs(){
        return dob.getTime();
    }
    public String getZip() {
        return zip;
    }
    public String getCountry() {
        return country;
    }

    protected HealthDiaryPatient(Parcel in) {
        id = in.readLong();
        firstNames = in.readString();
        lastName = in.readString();
        ssn = in.readString();
        addressLine = in.readString();
        zip = in.readString();
        country = in.readString();
        dob = new Date(in.readLong());
    }

    public static final Creator<HealthDiaryPatient> CREATOR = new Creator<HealthDiaryPatient>() {
        @Override
        public HealthDiaryPatient createFromParcel(Parcel in) {
            return new HealthDiaryPatient(in);
        }

        @Override
        public HealthDiaryPatient[] newArray(int size) {
            return new HealthDiaryPatient[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(firstNames);
        dest.writeString(lastName);
        dest.writeString(ssn);
        dest.writeString(addressLine);
        dest.writeString(zip);
        dest.writeString(country);
        dest.writeLong(dob.getTime()); // could also directly write java.util.Date-object as serializable but is unnecessarily slow since date does not have timezones anyway
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthDiaryPatient that = (HealthDiaryPatient) o;
        return getId() == that.getId() && getFirstNames().equals(that.getFirstNames()) && getLastName().equals(that.getLastName()) && getSsn().equals(that.getSsn()) && Objects.equals(getAddressLine(), that.getAddressLine()) && Objects.equals(zip, that.zip) && Objects.equals(country, that.country) && Objects.equals(getDob(), that.getDob());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFirstNames(), getLastName(), getSsn(), getAddressLine(), getZip(), getCountry(), getDob());
    }

    /**
     * @return hex-string of sha256 digest or, if SHA-256 is not found, string of decimal {@link Objects#hashCode}
     */
    public String hexSha256() {
        String s = getId() + getFirstNames() + getLastName() + getSsn() + getAddressLine() + getZip() + getCountry() + getDob();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return byteToHexString(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException should_not_happen) {
            should_not_happen.printStackTrace();
            return String.valueOf(hashCode());
        }
    }


    /**
     * @return <code>firstNames</code> <code>lastNames</code>, YYYY-mm-dd, id
     */
    public String toValueOnlyString() {
        return firstNames + " " + lastName + ", " + String.format("%tF",dob) + ", " + id;
    }

    @NonNull
    @Override
    public String toString() {
        return "HealthDiaryPatient{" +
                "id=" + id +
                ", firstNames='" + firstNames + '\'' +
                ", lastName='" + lastName + '\'' +
                ", ssn='" + ssn + '\'' +
                ", addressLine='" + addressLine + '\'' +
                ", zip='" + zip + '\'' +
                ", country='" + country + '\'' +
                ", dob=" + String.format("%tF",dob) +
                '}';
    }
    /**
     * @param payload byte array
     * @return big endian hex representation or empty string if null
     */
    public static String byteToHexString(byte[] payload) {
        if (payload == null) return "";
        StringBuilder stringBuilder = new StringBuilder(payload.length);
        for (byte byteChar : payload)
            stringBuilder.append(String.format("%02x", byteChar));
        return stringBuilder.toString();
    }
}
