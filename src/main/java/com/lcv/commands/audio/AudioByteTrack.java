package com.lcv.commands.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;
import com.sedmelluq.discord.lavaplayer.track.TrackMarker;

public class AudioByteTrack implements AudioTrack
{
    private final byte[] audio;
    private int pointer = 0;

    public AudioByteTrack(byte[] audio, AudioPlayer player)
    {
        super(new AudioTrackInfo("TTS", "VOICEVOX", 0, "tts", false, null), player);
    }

    @Override
    public AudioTrackInfo getInfo()
    {
        return null;
    }

    @Override
    public String getIdentifier()
    {
        return "";
    }

    @Override
    public AudioTrackState getState()
    {
        return null;
    }

    @Override
    public void stop()
    {

    }

    @Override
    public boolean isSeekable()
    {
        return false;
    }

    @Override
    public long getPosition()
    {
        return 0;
    }

    @Override
    public void setPosition(long l)
    {

    }

    @Override
    public void setMarker(TrackMarker trackMarker)
    {

    }

    @Override
    public void addMarker(TrackMarker trackMarker)
    {

    }

    @Override
    public void removeMarker(TrackMarker trackMarker)
    {

    }

    @Override
    public long getDuration()
    {
        return 0;
    }

    @Override
    public AudioTrack makeClone()
    {
        return null;
    }

    @Override
    public AudioSourceManager getSourceManager()
    {
        return null;
    }

    @Override
    public void setUserData(Object o)
    {

    }

    @Override
    public Object getUserData()
    {
        return null;
    }

    @Override
    public <T> T getUserData(Class<T> aClass)
    {
        return null;
    }
}
