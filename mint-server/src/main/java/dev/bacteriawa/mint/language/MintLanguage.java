package dev.bacteriawa.mint.language;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.bacteriawa.mint.exception.MintRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MintLanguage {
    private static final String language = dev.bacteriawa.mint.config.modules.globals.LanguageConfig.language;
    private static final Gson gson = new Gson();

    public static JsonObject getLanguage() {
        try(InputStream stream = MintLanguage.class.getResourceAsStream("/assets/mint/lang/" + language + ".json")) {
            if (stream == null) {
                throw new MintRuntimeException("Language " + language + " not found");
            }

            return gson.fromJson(new InputStreamReader(stream), JsonObject.class);
        } catch (IOException e) {
            throw new MintRuntimeException(e);
        }
    }
}
