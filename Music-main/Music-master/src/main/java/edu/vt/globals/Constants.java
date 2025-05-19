package edu.vt.globals;

public final class Constants {
    //---------------
    // To run locally
    //---------------

    // Unix (macOS) or Linux. For Windows start the path with C:
    public static final String FILES_ABSOLUTE_PATH  = "C:/Users/****/DocRoot/MusicStorage/FileStorage/";
    public static final String PHOTOS_ABSOLUTE_PATH = "C:/Users/****/DocRoot/MusicStorage/PhotoStorage/";

    //--------------------------------
    // To run on your AWS EC2 instance
    //--------------------------------
//    public static final String FILES_ABSOLUTE_PATH  = "/opt/wildfly/DocRoot/MusicStorage/FileStorage/";
//    public static final String PHOTOS_ABSOLUTE_PATH = "/opt/wildfly/DocRoot/MusicStorage/PhotoStorage/";

    //---------------
    // To run locally
    //---------------
    public static final String FILES_URI  = "http://localhost:8080/mfiles/";
    public static final String PHOTOS_URI = "http://localhost:8080/mphotos/";

    //-----------------------------------------------------
    // To run on your AWS EC2 instance with your IP address
    //-----------------------------------------------------
//    public static final String FILES_URI  = "http://mfiles/";
//    public static final String PHOTOS_URI = "http://mphotos/";

    /* 
    =============================================
    |   Our Design Decision for Profile Photo   |
    =============================================
    We do not want to use the uploaded user profile photo as is, which may be very large
    degrading performance. We scale it down to size 200x200 called the Thumbnail photo size.
     */
    public static final Integer THUMBNAIL_SIZE = 200;

    public static final String[] US_STATE_NAMES = {"Alabama", "Alaska", "Arizona", "Arkansas",
            "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia",
            "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky",
            "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota",
            "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire",
            "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota",
            "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina",
            "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington",
            "West Virginia", "Wisconsin", "Wyoming"};

    public static final String[] STATE_NAMES_AND_CODES = {"Alabama (AL)", "Alaska (AK)", "Arizona (AZ)", "Arkansas (AR)",
            "California (CA)", "Colorado (CO)", "Connecticut (CT)", "Delaware (DE)", "Florida (FL)", "Georgia (GA)",
            "Hawaii (HI)", "Idaho (ID)", "Illinois (IL)", "Indiana (IN)", "Iowa (IA)", "Kansas (KS)", "Kentucky (KY)",
            "Louisiana (LA)", "Maine (ME)", "Maryland (MD)", "Massachusetts (MA)", "Michigan (MI)", "Minnesota (MN)",
            "Mississippi (MS)", "Missouri (MO)", "Montana (MT)", "Nebraska (NE)", "Nevada (NV)", "New Hampshire (NH)",
            "New Jersey (NJ)", "New Mexico (NM)", "New York (NY)", "North Carolina (NC)", "North Dakota (ND)", "Ohio (OH)",
            "Oklahoma (OK)", "Oregon (OR)", "Pennsylvania (PA)", "Rhode Island (RI)", "South Carolina (SC)",
            "South Dakota (SD)", "Tennessee (TN)", "Texas (TX)", "Utah (UT)", "Vermont (VT)", "Virginia (VA)",
            "Washington (WA)", "West Virginia (WV)", "Wisconsin (WI)", "Wyoming (WY)"};
    /*
     A security question is selected and answered by the user at the time of account creation.
     The selected question/answer is used as a second level of authentication for
     (a) resetting user's password, and (b) deleting user's account.
     */
    public static final String[] SECURITY_QUESTIONS_EN = {
            "In what city or town did your mother and father meet?",
            "In what city or town were you born?",
            "What did you want to be when you grew up?",
            "What do you remember most from your childhood?",
            "What is the name of the boy or girl that you first kissed?",
            "What is the name of the first school you attended?",
            "What is the name of your favorite childhood friend?",
            "What is the name of your first pet?",
            "What is your mother's maiden name?",
            "What was your favorite place to visit as a child?"
    };

    public static final String[] SECURITY_QUESTIONS_PT = {
            "Em que cidade seus pais se conheceram?",
            "Em que cidade você nasceu?",
            "O que vocês queria ser quando crescer?",
            "O que você mais se lembra da sua infância?",
            "Qual o nome do garoto ou garota que você teve o primeiro beijo?",
            "Qual o nome da primeira escola que você estudou?",
            "Qual o nome do seu melhor amigo de infância?",
            "Qual o nome do seu primeiro animal de estimação?",
            "Qual o sobrenome da sua mãe?",
            "Que lugar você queria visitar quando era criança?"
    };

    public static final String[] SECURITY_QUESTIONS_TR = {
            "Anne ve babanız hangi şehirde tanıştı?",
            "Hangi şehirde doğdunuz?",
            "Büyüyünce ne olmak istiyordunuz?",
            "Çocukluğunuza dair en çok ne hatırlıyorsunuz?",
            "İlk öpüştüğünüz kızın veya erkeğin adı nedir?",
            "İlkokulunuzun adı nedir?",
            "En sevdiğiniz çocukluk arkadaşınızın adı ne?",
            "İlk evcil hayvanınızın adı nedir?",
            "Annenizin kızlık soyadı nedir?",
            "Çocukken ziyaret etmeyi en sevdiğiniz yer neresiydi?"
    };

    public static final String SPOTIFY_CLIENT_ID = "";

    public static final String SPOTIFY_CLIENT_SECRET = "";

    public static final String TICKETMASTER_API_KEY = "";

    public static final String GOOGLE_API_KEY = "";

    public static final String AUDD_API_KEY = "";
}
