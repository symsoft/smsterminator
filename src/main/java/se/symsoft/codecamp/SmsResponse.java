/*
 * Copyright Symsoft AB 1996-2015. All Rights Reserved.
 */
package se.symsoft.codecamp;

import java.util.UUID;

public class SmsResponse {
    private int responseCode;
    private UUID refId;
    private long serviceCentreTime;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public UUID getRefId() {
        return refId;
    }

    public void setRefId(UUID refId) {
        this.refId = refId;
    }

    public long getServiceCentreTime() {
        return serviceCentreTime;
    }

    public void setServiceCentreTime(long serviceCentreTime) {
        this.serviceCentreTime = serviceCentreTime;
    }
}
