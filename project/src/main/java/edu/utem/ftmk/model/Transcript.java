package edu.utem.ftmk.model;

import java.sql.Timestamp;

/**
 * Domain model for the transcript table.
 * Holds the full raw transcript text and metadata from Faster-Whisper.
 */
public class Transcript {

    private int       transcriptId;
    private int       reelId;
    private int       audioId;
    private String    fileName;
    private String    filePath;
    private long      fileSizeBytes;
    private String    content;          // Full raw transcript text
    private String    languageMix;      // e.g. "mostly-malay", "mixed"
    private String    whisperModel;     // e.g. "medium"
    private String    detectedLanguage; // e.g. "ms"
    private float     languageProbability;
    private boolean   audioTranscriptConsistent;
    private Timestamp createdAt;

    // Joined reel info for dashboard display
    private Reel      reel;

    public Transcript() {}

    // ----------------------------------------------------------------
    // Getters and Setters
    // ----------------------------------------------------------------

    public int    getTranscriptId()                     { return transcriptId; }
    public void   setTranscriptId(int v)                { this.transcriptId = v; }

    public int    getReelId()                           { return reelId; }
    public void   setReelId(int v)                      { this.reelId = v; }

    public int    getAudioId()                          { return audioId; }
    public void   setAudioId(int v)                     { this.audioId = v; }

    public String getFileName()                         { return fileName; }
    public void   setFileName(String v)                 { this.fileName = v; }

    public String getFilePath()                         { return filePath; }
    public void   setFilePath(String v)                 { this.filePath = v; }

    public long   getFileSizeBytes()                    { return fileSizeBytes; }
    public void   setFileSizeBytes(long v)              { this.fileSizeBytes = v; }

    public String getContent()                          { return content; }
    public void   setContent(String v)                  { this.content = v; }

    public String getLanguageMix()                      { return languageMix; }
    public void   setLanguageMix(String v)              { this.languageMix = v; }

    public String getWhisperModel()                     { return whisperModel; }
    public void   setWhisperModel(String v)             { this.whisperModel = v; }

    public String getDetectedLanguage()                 { return detectedLanguage; }
    public void   setDetectedLanguage(String v)         { this.detectedLanguage = v; }

    public float  getLanguageProbability()              { return languageProbability; }
    public void   setLanguageProbability(float v)       { this.languageProbability = v; }

    public boolean isAudioTranscriptConsistent()        { return audioTranscriptConsistent; }
    public void    setAudioTranscriptConsistent(boolean v){ this.audioTranscriptConsistent = v; }

    public Timestamp getCreatedAt()                     { return createdAt; }
    public void      setCreatedAt(Timestamp v)          { this.createdAt = v; }

    public Reel   getReel()                             { return reel; }
    public void   setReel(Reel v)                       { this.reel = v; }

    @Override
    public String toString() {
        return "Transcript#" + transcriptId + " [" + fileName + "]";
    }
}
