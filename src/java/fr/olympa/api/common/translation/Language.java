package fr.olympa.api.common.translation;

import fr.olympa.api.common.player.Gender;

import java.util.Map;

public class Language {

    private String code;
    private String name;
    private Map<String, Translation> translations;

    public Language(){

    }

    public Language(String code, String name){

    }

    public String translate(String id, Gender gender){
        if(!translations.containsKey(id)){
            return "Unknown Translation (" + code + ", " + id + ")";
        }

        String translation = translations.get(id).getTranslation(gender);

        if(translation == null || translation.isEmpty()){
            return "Empty Translation (" + code + ", " + id + ")";
        }

        //No placeholder atm
        return translation;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTranslations(Map<String, Translation> translations) {
        this.translations = translations;
    }

    private class Translation {
        private String unspecified;
        private String male;
        private String female;
        private String nonbinary;

        public Translation(){

        }

        public String getTranslation(Gender g){
            switch (g){
                case MALE -> {
                    return (male != null && !male.isEmpty()) ? male : unspecified;
                }
                case FEMALE -> {
                    return (female != null && !female.isEmpty()) ? female : unspecified;
                }
                case UNBINARY -> {
                    return (nonbinary != null && !nonbinary.isEmpty()) ? nonbinary : unspecified;
                }
                default -> {
                    return unspecified;
                }
            }
        }

        public void setUnspecified(String unspecified) {
            this.unspecified = unspecified;
        }

        public void setMale(String male) {
            this.male = male;
        }

        public void setFemale(String female) {
            this.female = female;
        }

        public void setNonbinary(String nonbinary) {
            this.nonbinary = nonbinary;
        }
    }
}
