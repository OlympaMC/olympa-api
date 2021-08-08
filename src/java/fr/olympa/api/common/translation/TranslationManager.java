package fr.olympa.api.common.translation;

import com.google.gson.Gson;
import com.mongodb.client.MongoDatabase;
import fr.olympa.api.common.plugin.OlympaSpigot;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class TranslationManager extends AbstractTranslationManager {

    private OlympaSpigot plugin;

    public TranslationManager(OlympaSpigot plugin){
        this.plugin = plugin;
    }

    @Override
    public void loadTranslations() {
        MongoDatabase db = plugin.getMongo().getDatabase();
        List<String> codes = new ArrayList<>();
        db.getCollection("translations").distinct("code", String.class).into(codes);
        Gson gson = new Gson();

        for(String code : codes){
            Document query = new Document("code", code);
            Language language = gson.fromJson(db.getCollection("translations").find(query).first().toJson(), Language.class);
            languages.put(code, language);
        }
    }
}
