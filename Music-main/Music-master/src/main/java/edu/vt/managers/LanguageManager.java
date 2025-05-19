package edu.vt.managers;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Named("languageManager")
@SessionScoped
public class LanguageManager implements Serializable {
    /*
    ===============================
    Instance Variables (Properties)
    ===============================
     */

    // Language is the language code: 'en' for English, 'pt' for Portuguese, 'tr' for Turkish
    private String language;

    // Locale is the language code: 'en' for English, 'pt' for Portuguese, 'tr' for Turkish
    private Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();

    private static Map<String, Object> languages;
    static {
        languages = new LinkedHashMap<String, Object>();

        // See the list of languages supported by Java / Jakarta EE at
        // https://www.oracle.com/java/technologies/javase/jdk8-jre8-suported-locales.html

        Locale portuguese = new Locale("pt");
        Locale turkish = new Locale("tr");
        languages.put( "English", Locale.ENGLISH );
        languages.put( "Portuguese", portuguese);
        languages.put( "Turkish", turkish);
    }

    @PostConstruct
    public void init() {
        language = String.valueOf(FacesContext.getCurrentInstance().getExternalContext().getRequestLocale());
    }

    /*
    =========================
    Getter and Setter Methods
    =========================
     */

    public String getLanguage() {
        return locale.getLanguage();
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Map<String, Object> getLanguages() {
        return languages;
    }

    public void setLanguages(Map<String, Object> languages) {
        LanguageManager.languages = languages;
    }

    // ValueChangeEvent Listener
    public void languageChanged(ValueChangeEvent event) {
        String newLocale = event.getNewValue().toString();

        for (Map.Entry<String, Object> entry : languages.entrySet()) {
            if (entry.getValue().toString().equals(newLocale)) {
                locale = (Locale)entry.getValue();
                FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
            }
        }
    }

}
