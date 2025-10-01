package com.aquatrack.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;

public final class GsonProvider {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .enableComplexMapKeySerialization() // <- permite Map com chave complexa (enum, objeto)
            .setPrettyPrinting()
            .create();

    private GsonProvider() {}
    public static Gson get() { return GSON; }
}
