package com.lcv;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main extends ListenerAdapter
{
    public static void main(String[] args)
    {
        JDA jda = JDABuilder.create(
                System.getenv("BOTKEY"),
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(new Main())
                .build();

        System.out.println("apple");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (!event.getAuthor().isBot())
            event.getMessage().reply("hey!").queue();
    }
}