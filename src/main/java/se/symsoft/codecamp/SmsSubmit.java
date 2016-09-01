/*
 * Copyright Symsoft AB 1996-2015. All Rights Reserved.
 */
package se.symsoft.codecamp;

public class SmsSubmit {
    private String originator;
    private String destination;
    private String userData;

    public SmsSubmit(String originator, String destination, String userData) {
        this.originator = originator;
        this.destination = destination;
        this.userData = userData;
    }

    public SmsSubmit() {
    }

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }
}
