package com.ick.kalambury.di.gamehandler;

import com.ick.kalambury.GameMode;

import dagger.MapKey;

@MapKey(unwrapValue = false)
public @interface GameHandlerKey {
    GameMode mode();
    boolean host();
}
