package com.lcv.commands;

import net.dv8tion.jda.api.interactions.InteractionContextType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandMeta
{
    String name();
    String description();
    InteractionContextType[] contexts() default InteractionContextType.GUILD;
}
