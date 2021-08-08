package fr.olympa.api.common.translation;


import fr.olympa.api.common.player.Gender;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractTranslationManager {

    protected Map<String, Language> languages = new HashMap<>();

    public abstract void loadTranslations();

    public String translate(String languageCode, String id, Gender gender){
        if(!languages.containsKey(languageCode)){
            return "Unknown Language (" + languageCode + ")";
        }

        return languages.get(languageCode).translate(id, gender);
    }
}
