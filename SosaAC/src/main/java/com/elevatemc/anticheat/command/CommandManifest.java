package com.elevatemc.anticheat.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
// Credit goes out to Sim0n

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE } )
public @interface CommandManifest {
    String permission() default "sosa.admin";

    // We might want to run some commands async, such as commands that perform database operations
    boolean async() default false;
}
