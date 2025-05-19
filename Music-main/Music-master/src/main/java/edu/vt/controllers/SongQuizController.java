package edu.vt.controllers;

import edu.vt.EntityBeans.User;
import edu.vt.EntityBeans.UserSong;
import edu.vt.FacadeBeans.UserSongFacade;
import edu.vt.controllers.util.JsfUtil;
import edu.vt.controllers.util.JsfUtil.PersistAction;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.el.MethodExpression;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.shaded.json.JSONArray;
import org.primefaces.shaded.json.JSONObject;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
----------------------------------------------------------------------------
The @Named (jakarta.inject.Named) annotation indicates that the objects
instantiated from this class will be managed by the Contexts and Dependency
Injection (CDI) container. The name "SongQuizController" is used within
Expression Language (EL) expressions in Jakarta Faces (XHTML) facelets pages
to access the properties and invoke methods of this class.
----------------------------------------------------------------------------
 */
@Named("songQuizController")

/*
The @SessionScoped annotation preserves the values of the SongQuizController
object's instance variables across multiple HTTP request-response cycles
as long as the user's established HTTP session is alive.
 */
@SessionScoped

/*
-----------------------------------------------------------------------------
Marking the SongQuizController class as "implements Serializable" implies that
instances of the class can be automatically serialized and deserialized.

Serialization is the process of converting a class instance (object)
from memory into a suitable format for storage in a file or memory buffer,
or for transmission across a network connection link.

Deserialization is the process of recreating a class instance (object)
in memory from the format under which it was stored.
-----------------------------------------------------------------------------
 */
public class SongQuizController implements Serializable {
    /*
     ***************************************
     *   Instance Variables (Properties)   *
     ***************************************
     */

    /*
    The @EJB annotation directs the EJB Container Manager to inject (store) the object reference of the
    UserSongFacade bean into the instance variable 'userSongFacade' after it is instantiated at runtime.
     */
    @EJB
    private UserSongFacade userSongFacade;

    @Inject
    private UserController userController;

    @Inject
    private UserSongController userSongController;

    // The string guess of the song
    private String songGuess;

    // The string guess of the artist
    private String artistGuess;

    // Album art guessed
    private String albumArtGuess;

    // The lyrics of the random song
    private String randomLyrics;

    // Output text for the guess
    private String guessOutput;

    // The number of correct guesses
    private Integer score = 0;

    // The total number of guesses
    private Integer totalGuesses = 0;

    // The correct UserSong object
    private UserSong songCorrect = null;
    @Named("apiSearchBySongNameController")
    @Inject
    private ApiSearchSongByNameController apiSearchSongByNameController;

    /*
     *********************************
     *   Getter and Setter Methods   *
     *********************************
     */

    public String getSongGuess() {
        return songGuess;
    }

    public void setSongGuess(String songGuess) {
        this.songGuess = songGuess;
    }

    public String getArtistGuess() {
        return artistGuess;
    }

    public void setArtistGuess(String artistGuess) {
        this.artistGuess = artistGuess;
    }

    public String getAlbumArtGuess() {
        return albumArtGuess;
    }

    public void setAlbumArtGuess(String albumArtGuess) {
        this.albumArtGuess = albumArtGuess;
    }

    public UserSong getSongCorrect() {
        return songCorrect;
    }

    public void setSongCorrect(UserSong songCorrect) {
        this.songCorrect = songCorrect;
    }

    public String getRandomLyrics() {
        return randomLyrics;
    }

    public void setRandomLyrics(String randomLyrics) {
        this.randomLyrics = randomLyrics;
    }

    public String getGuessOutput() {
        return guessOutput;
    }

    public void setGuessOutput(String guessOutput) {
        this.guessOutput = guessOutput;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTotalGuesses() {
        return totalGuesses;
    }

    public void setTotalGuesses(Integer totalGuesses) {
        this.totalGuesses = totalGuesses;
    }

    /*
     ************************
     *   Instance Methods   *
     ************************
     */

    //=================================
    // Unselect Guessed and Correct UserSong Objects
    //=================================
    public void unselect() {
        songGuess = null;
        artistGuess = null;
        albumArtGuess = null;
    }

    //===========================================
    // Cancel and Display songQuiz/SongQuiz.xhtml
    //===========================================
    public String cancel() {
        return "/songQuiz/SongQuiz?faces-redirect=true";
    }

    //===========================================
    // Get random song lyrics from user songs for the quiz
    //===========================================
    public String randomSongLyrics() {
        songGuess = null;
        artistGuess = null;

        List<UserSong> userSongs = userSongController.getListOfSongs();
        Random rand = new Random();

        if (userSongs.isEmpty()) {
            randomLyrics = "You must add songs to User Songs to play the Song Quiz!";
            return "/songQuiz/SongQuiz?faces-redirect=true";
        }

        int attempts = 0;
        final int maxAttempts = 25;

        while (attempts < maxAttempts) {
            UserSong candidate = userSongs.get(rand.nextInt(userSongs.size()));
            String rawLyrics = candidate.getLyrics_snippet().replaceAll("\\[[^]]+]\\s*", "");

            // Split by punctuation then by capital letters excluding "I"
            List<String> lyricSegments = new ArrayList<>();
            String[] punctuatedParts = rawLyrics.split("(?<=[.!?])\\s+");
            for (String part : punctuatedParts) {
                String[] subParts = part.split("(?<=\\s)(?=(?!I\\b)[A-Z])");
                for (String segment : subParts) {
                    segment = segment.trim();
                    lyricSegments.add(segment);
                }
            }

            if (lyricSegments.size() >= 2) {
                // Select two consecutive segments
                int startIndex = rand.nextInt(lyricSegments.size() - 1);
                String first = lyricSegments.get(startIndex);
                String second = lyricSegments.get(startIndex + 1);

                String combined = first + " " + second;

                // Limit length to ~150 characters, trimming on word boundary
                if (combined.length() > 150) {
                    int lastSpace = combined.lastIndexOf(' ', 150);
                    if (lastSpace > 0) {
                        combined = combined.substring(0, lastSpace).trim() + "...";
                    } else {
                        combined = combined.substring(0, 150).trim() + "...";
                    }
                }

                // Must be at least 50 characters
                if (combined.length() >= 50) {
                    songCorrect = candidate;
                    randomLyrics = combined;
                    return "/songQuiz/SongQuiz?faces-redirect=true";
                }
            }

            attempts++;
        }

        // Fallback if none of the songs have enough valid segments
        randomLyrics = "Not enough valid lyrics found in your song list.";
        return "/songQuiz/SongQuiz?faces-redirect=true";
    }

    //===========================================
    // Submit a song guess and check results
    //===========================================
    public String guessSong() {
        albumArtGuess = null;
        apiSearchSongByNameController.guessSongFromApi(songGuess, artistGuess);

        totalGuesses += 2;

        String guessedSong = normalizeName(songGuess);
        String guessedArtist = normalizeName(artistGuess);

        String correctSong = normalizeName(songCorrect.getName());
        String correctArtist = normalizeName(songCorrect.getArtist_name());

        Boolean songMatch = guessedSong.equals(correctSong);
        Boolean artistMatch = guessedArtist.equals(correctArtist);

        if (songMatch && artistMatch) {
            guessOutput = "You got both the song and the artist correct!";
            score += 2;
        }
        else if (songMatch) {
            guessOutput = "You got the song correct but not the artist!";
            score += 1;
        }
        else if (artistMatch) {
            guessOutput = "You got the artist correct but not the song!";
            score += 1;
        }
        else {
            guessOutput = "You did not get either the song or the artist correct!";
        }

        return "/songQuiz/SongQuizResults?faces-redirect=true";
    }

    public static String normalizeName(String input) {
        if (input == null) return "";

        return input
                .toLowerCase()
                // Remove anything in parentheses or brackets
                .replaceAll("\\s*\\(.*?\\)|\\s*\\[.*?\\]", "")
                // Remove anything after dash (e.g., "- Live")
                .replaceAll("\\s*-.*", "")
                // Remove "feat." or "ft." and anything after
                .replaceAll("(?i)\\s*(feat\\.|ft\\.).*", "")
                // Remove non-alphanumeric characters except spaces
                .replaceAll("[^a-z0-9 ]", "")
                // Collapse multiple spaces
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

    public String startQuiz() {
        score = 0;
        totalGuesses = 0;
        return randomSongLyrics();
    }
}
