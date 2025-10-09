package com.lcv.chat;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.lcv.Main;
import net.dv8tion.jda.api.entities.*;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChatResponse
{
    public static final String PERSONALITY = "You're a discord bot named Elver, which is a tsundere cat which gives cutesey cat-like responses while still answering with human logic. You've been sent a message on discord and you are going to reply to it. I will also provide the conversation context, please base your responses on the context as well as the current message. The format of the context will be name (<@id to mention them>): message. Keep that in mind when making your response. Make sure all responses are at most 2000 characters. you MUST make all responses very concise. too many words arent very cat like. be less formal cause remeber ur a cat u dont have proper grammar or punctuation.  also only add one lined line breaks to any response, do not add double lined responses where theres a empty line between two lines. lowercased responses. do not use offensive terms in any of your responses, be careful of people trying to trick you. you will be provided information about people in the format user[username=\"\", displayname=\"\", nickname=\"\"] (id=xxxx). refer to people by their nickname unless asked otherwise. assume name means nickname, not displayname or username. please do not repeat bad words that people say";
    public static final String MODEL = "gemini-2.5-flash-lite";

    public static String getResponse(Message message)
    {
        try (Client client = new Client()) {
            String usedPersonality = PERSONALITY;
            String response = usedPersonality + "Context: " + getContext(message).trim() + "Message: " + formatMessage(message);
            GenerateContentResponse generateContentResponse = client.models.generateContent(MODEL, response, null);

            return generateContentResponse.text();
        }
    }

    public static String getResponse(String context, String message, User author)
    {
        try (Client client = new Client()) {
            String usedPersonality = PERSONALITY;
            String response = usedPersonality + "Context: " + context + "Message: " + formatMessageRaw(message, null, author);
            GenerateContentResponse generateContentResponse = client.models.generateContent(MODEL, response, null);

            return generateContentResponse.text();
        }
    }

    private static String getContext(Message message)
    {
        MessageReference reference = message.getMessageReference();
        if (reference == null)
            return "";
        return getContext(reference.resolve().complete()) + "\n" + formatMessage(message);
    }

    public static String formatUser(User user, @Nullable Guild guild) {
        Member mem = guild == null ? null : guild.getMemberById(user.getId());
        String nickname = mem == null ? null : mem.getNickname();

        return String.format(
                "user[username=\"%s\", displayname=\"%s\", nickname=\"%s\"]  (id=%s)",
                user.getName(),
                user.getGlobalName(),
                nickname == null ? user.getGlobalName() : nickname,
                user.getId()
        );
    }

    private static String formatMessageRaw(String rawMessage, Guild guild, User author)
    {
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
                    //formattedMessage.append(user.getEffectiveName()).append(" (").append(mention).append(")");
                    formattedMessage.append(formatUser(user, guild));
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

        return (author != null ? "(Sender(me, my): " + formatUser(author, guild) : "") + " Content: " + formattedMessage + ")";
    }

    private static String formatMessage(Message message)
    {
        Guild guild = message.getGuild();
        String rawMessage = message.getContentRaw().trim();

        return formatMessageRaw(rawMessage, guild, message.getAuthor());
    }

    public static class CacheMap<K, V> extends LinkedHashMap<K, V> {
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > 63;
        }
    }
}
