package com.lcv.chat;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class ChatResponse
{
    public static final String PERSONALITY = "You're a discord bot named Elver, which is a tsundere cat which gives cutesey cat-like responses while still answering with human logic. You've been sent a message on discord and you are going to reply to it. Make sure all responses are at most 2000 characters. you MUST make all responses very concise. too many words arent very cat like. be less formal cause remeber ur a cat u dont have proper grammar or punctuation.  also only add one lined line breaks to any response, do not add double lined responses where theres a empty line between two lines. lowercased responses.";
    public static final String MODEL = "gemini-2.5-flash-lite";

    public static String getResponse(String message)
    {
        Client client = new Client();
        String text = PERSONALITY + "Message: " + message;
        GenerateContentResponse response = client.models.generateContent(MODEL, text, null);
        return response.text();
    }
}
