package io.github.flemmli97.debugutils.neoforge.data;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import io.github.flemmli97.debugutils.DebugUtils;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.util.GsonHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Lang implements DataProvider {

    private final Map<String, String> data = new LinkedHashMap<>();
    private final PackOutput packOutput;
    private final String modid;
    private final String locale;

    public Lang(PackOutput output) {
        this.packOutput = output;
        this.modid = DebugUtils.MODID;
        this.locale = "en_us";
    }

    protected void addTranslations() {
        this.add("debugutils.command.all.off", "Turned all debugging features off for %s");
        this.add("debugutils.command.all.off.self", "Turned all debugging features off");
        this.add("debugutils.command.all.on.note", "Note: Turning all debuggings on at once is disabled for performance reasons");
        this.add("debugutils.command.toggle.on", "Turned %s on for %s");
        this.add("debugutils.command.toggle.off", "Turned %s off for %s");
        this.add("debugutils.command.toggle.on.self", "Turned %s on");
        this.add("debugutils.command.toggle.off.self", "Turned %s off");
        this.add("debugutils.command.toggle.none", "No such toggle %s");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.runAsync(() -> {
            this.addTranslations();
            if (!this.data.isEmpty()) {
                try {
                    this.save(cache, this.packOutput.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(this.modid + "/lang/" + this.locale + ".json"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public String getName() {
        return "Languages: " + this.locale;
    }

    private void save(CachedOutput cache, Path target) throws IOException {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, String> pair : this.data.entrySet()) {
            json.addProperty(pair.getKey(), pair.getValue());
        }
        saveTo(cache, json, target);
    }

    public void add(String key, String value) {
        if (this.data.put(key, value) != null)
            throw new IllegalStateException("Duplicate translation key " + key);
    }

    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    private static void saveTo(CachedOutput cachedOutput, JsonElement jsonElement, Path path) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), byteArrayOutputStream);
        OutputStreamWriter writer = new OutputStreamWriter(hashingOutputStream, StandardCharsets.UTF_8);
        JsonWriter jsonWriter = new JsonWriter(writer);
        jsonWriter.setSerializeNulls(false);
        jsonWriter.setIndent("  ");
        GsonHelper.writeValue(jsonWriter, jsonElement, null);
        jsonWriter.close();
        cachedOutput.writeIfNeeded(path, byteArrayOutputStream.toByteArray(), hashingOutputStream.hash());
    }
}
