package ua.nure.vkmessanger.model;

import java.io.Serializable;

/**
 * Created by Nickitee on 15.05.2016.
 */
public class User implements Serializable {

    private int id;

    private String firstName, lastName;

    private String birthDayDate;

    private String avatar100Url;

    private String avatar200Url;

    private String fullAvatarOriginalUrl;

    private boolean isOnline;

    public User(int id, String fName, String lName, String birthday,
                String avatar100Url, String avatar200, String avatarOriginal, boolean online) {
        this.id = id;
        this.lastName = lName;
        this.firstName = fName;
        this.birthDayDate = birthday;
        this.avatar100Url = avatar100Url;
        this.avatar200Url = avatar200;
        this.fullAvatarOriginalUrl = avatarOriginal;
        this.isOnline = online;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getBirthDayDate() {
        return birthDayDate;
    }

    public String getAvatar100Url() {
        return avatar100Url;
    }

    public String getAvatar200Url() {
        return avatar200Url;
    }

    public String getFullAvatarOriginalUrl() {
        return fullAvatarOriginalUrl;
    }

    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", fullAvatarOriginalUrl='" + fullAvatarOriginalUrl + '\'' +
                ", avatar200Url='" + avatar200Url + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthDayDate='" + birthDayDate + '\'' +
                ", isOnline=" + isOnline +
                '}';
    }
}
