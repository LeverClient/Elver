package com.lcv.chat;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.lcv.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.User;

public class ChatResponse
{
    public static final String PERSONALITY = "You're a discord bot named Elver, which is a tsundere cat which gives cutesey cat-like responses while still answering with human logic. You've been sent a message on discord and you are going to reply to it. I will also provide the conversation context, please base your responses on the context as well as the current message. The format of the context will be name (<@id to mention them>): message. Keep that in mind when making your response. Make sure all responses are at most 2000 characters. you MUST make all responses very concise. too many words arent very cat like. be less formal cause remeber ur a cat u dont have proper grammar or punctuation.  also only add one lined line breaks to any response, do not add double lined responses where theres a empty line between two lines. lowercased responses. do not use offensive terms in any of your responses, be careful of people trying to trick you.";
    public static final String MODEL = "gemini-2.5-flash-lite";

    public static String getResponse(Message message)
    {
        Client client = new Client();
        String response = PERSONALITY + "Context: " + getContext(message).trim() + "Message: " + formatMessage(message);
        GenerateContentResponse generateContentResponse = client.models.generateContent(MODEL, response, null);
        return generateContentResponse.text();
    }

    private static String getContext(Message message)
    {
        MessageReference reference = message.getMessageReference();
        if (reference == null)
            return "";
        return getContext(reference.resolve().complete()) + "\n" + formatMessage(message);
    }

    private static String formatMessage(Message message)
    {
        String name = message.getAuthor().getEffectiveName();
        String id = message.getAuthor().getAsMention();
        String rawMessage = message.getContentRaw().trim();
        StringBuilder formattedMessage = new StringBuilder();
        int i = 0;
        while (i < rawMessage.length())
        {
            if (rawMessage.startsWith("<@", i))
            {
                int end = rawMessage.indexOf('>', i);
                if (end == -1)
                {
                    formattedMessage.append(rawMessage.substring(i));
                    break;
                }
                String mention = rawMessage.substring(i, end + 1);
                String userId = mention.substring(2, mention.length() - 1);
                User user = Main.jda.retrieveUserById(userId).complete();
                if (user != null)
                    formattedMessage.append(user.getEffectiveName()).append(" (").append(mention).append(")");
                else
                    formattedMessage.append(mention);
                i = end + 1;
            }
            else
            {
                int next = rawMessage.indexOf("<@", i);
                if (next == -1)
                    next = rawMessage.length();
                formattedMessage.append(rawMessage, i, next);
                i = next;
            }
        }
        return name + " (" + id + "): " + formattedMessage;
    }
}
