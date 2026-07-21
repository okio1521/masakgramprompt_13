package edu.utem.ftmk.model;

import java.sql.Timestamp;

/**
 * Domain model representing joined data from reel + influencer tables.
 * Used for display in the Reel Analysis Dashboard.
 */
public class Reel {

    private int       reelId;
    private String    reelIdInstagram;   // e.g. "DVAa0SPk_YG"
    private String    reelUrl;
    private String    influencerHandle;  // instagram_account
    private String    influencerName;
    private Timestamp createdAt;

    public Reel() {}

    public Reel(int reelId, String reelIdInstagram, String reelUrl,
                String influencerHandle, String influencerName) {
        this.reelId          = reelId;
        this.reelIdInstagram = reelIdInstagram;
        this.reelUrl         = reelUrl;
        this.influencerHandle = influencerHandle;
        this.influencerName  = influencerName;
    }

    public int    getReelId()                    { return reelId; }
    public void   setReelId(int v)               { this.reelId = v; }

    public String getReelIdInstagram()           { return reelIdInstagram; }
    public void   setReelIdInstagram(String v)   { this.reelIdInstagram = v; }

    public String getReelUrl()                   { return reelUrl; }
    public void   setReelUrl(String v)           { this.reelUrl = v; }

    public String getInfluencerHandle()          { return influencerHandle; }
    public void   setInfluencerHandle(String v)  { this.influencerHandle = v; }

    public String getInfluencerName()            { return influencerName; }
    public void   setInfluencerName(String v)    { this.influencerName = v; }

    public Timestamp getCreatedAt()              { return createdAt; }
    public void      setCreatedAt(Timestamp v)   { this.createdAt = v; }

    @Override
    public String toString() {
        return reelIdInstagram + " (@" + influencerHandle + ")";
    }
}
