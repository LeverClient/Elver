package com.lcv.commands.audio;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioHandler implements AudioSendHandler
{
    private final AudioInputStream stream;
    private final byte[] buffer = new byte[1920];
    private boolean finished = false;

    public AudioHandler(byte[] audio) throws UnsupportedAudioFileException, IOException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(audio);
        AudioInputStream ais = AudioSystem.getAudioInputStream(bais);

        AudioFormat baseFormat = ais.getFormat();
        AudioFormat decodedFormat = new AudioFormat(48000.0F, 16, 1, true, true);

        System.out.println(baseFormat);
        System.out.println(decodedFormat);

        this.stream = AudioSystem.getAudioInputStream(decodedFormat, ais);
    }

    @Override
    public boolean canProvide()
    {
        return !finished;
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio()
    {
        try
        {
            int read = stream.read(buffer, 0, buffer.length);
            if (read <= 0)
            {
                stream.close();
                return null;
            }

            if (read < buffer.length)
            {
                for (int i = read; i < buffer.length; i++) buffer[i] = 0;
                finished = true;
            }

            return ByteBuffer.wrap(buffer.clone());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            finished = true;
            return null;
        }
    }

    @Override
    public boolean isOpus()
    {
        return false;
    }
}