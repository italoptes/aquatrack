package com.aquatrack.json;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter extends TypeAdapter<LocalDate> {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;       // yyyy-MM-dd
    private static final DateTimeFormatter BR  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter BR2 = DateTimeFormatter.ofPattern("d/M/yyyy");

    @Override
    public void write(JsonWriter out, LocalDate value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.format(ISO)); // sempre normaliza para ISO
        }
    }

    @Override
    public LocalDate read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) { // lida com null no JSON
            in.nextNull();
            return null;
        }
        String str = in.nextString();
        if (str == null || str.isBlank() || str.equals("---")) {
            return null;
        }

        // tenta ISO primeiro
        try { return LocalDate.parse(str, ISO); } catch (Exception ignore) {}

        // retrocompatibilidade com dados antigos
        try { return LocalDate.parse(str, BR); } catch (Exception ignore) {}
        try { return LocalDate.parse(str, BR2); } catch (Exception ignore) {}

        // se quiser, dá para adicionar outros formatos aqui (ex: "dd-MM-yyyy")

        // por fim, deixa a exceção clara
        throw new IOException("Data inválida para LocalDate: '" + str + "'");
    }
}
