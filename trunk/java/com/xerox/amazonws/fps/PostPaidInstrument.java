package com.xerox.amazonws.fps;

import java.util.Date;
import java.io.Serializable;

/**
 * @author J. Bernard
 * @author Elastic Grid, LLC.
 * @author jerome.bernard@elastic-grid.com
 */
public class PostPaidInstrument implements Instrument {
    private final String creditInstrumentId;
    private final String creditSenderTokenId;
    private final String settlementTokenId;
    private final Date expiry;
    private final Status status;

    public PostPaidInstrument(String creditInstrumentId, String creditSenderTokenId, String settlementTokenId, Date expiry) {
        this.creditInstrumentId = creditInstrumentId;
        this.creditSenderTokenId = creditSenderTokenId;
        this.settlementTokenId = settlementTokenId;
        this.expiry = expiry;
        this.status = Status.ACTIVE;
    }

    public String getCreditInstrumentId() {
        return creditInstrumentId;
    }

    public String getCreditSenderTokenId() {
        return creditSenderTokenId;
    }

    public String getSettlementTokenId() {
        return settlementTokenId;
    }

    public Date getExpiry() {
        return expiry;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "PostPaidInstrument{" +
                "creditInstrumentId='" + creditInstrumentId + '\'' +
                ", creditSenderTokenId='" + creditSenderTokenId + '\'' +
                ", settlementTokenId='" + settlementTokenId + '\'' +
                ", expiry=" + expiry +
                ", status=" + status +
                '}';
    }
}
