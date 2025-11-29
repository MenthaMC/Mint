package dev.bacteriawa.mint.language;

import com.google.gson.*;
import dev.bacteriawa.mint.exception.MintRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MintLanguage {
    private static final Gson gson = new Gson();
    private static JsonObject object = null;

    public static String[] translateComments(String path, String[] comments) {
        String[] rawComment = comments;
        if (rawComment.length == 0) {
            if (getLanguage().has(path)) {
                JsonElement element = getLanguage().get(path);
                if (element instanceof JsonArray array) {
                    rawComment = jsonArray2StringArray(array);
                } else if (element instanceof JsonPrimitive string) {
                    rawComment = new String[]{string.getAsString()};
                }
            }
        }

        return rawComment;
    }

    private static JsonObject getLanguage() {
        if (object == null) {
            String language = dev.bacteriawa.mint.config.modules.globals.LanguageConfig.language;
            try(InputStream stream = MintLanguage.class.getResourceAsStream("/assets/mint/lang/" + language + ".json")) {
                if (stream == null) {
                    throw new MintRuntimeException("Language " + language + " not found");
                }

                object = gson.fromJson(new InputStreamReader(stream), JsonObject.class);
            } catch (IOException e) {
                throw new MintRuntimeException(e);
            }
        }

        return object;
    }

    private static String[] jsonArray2StringArray(JsonArray array) {
        return array.asList().stream().map(JsonElement::getAsString).toArray(String[]::new);
    }
}
