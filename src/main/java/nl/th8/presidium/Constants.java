package nl.th8.presidium;

import org.springframework.stereotype.Component;

@Component
public class Constants {

    private Constants() {
        //Hide public constructor
    }

    /* Properties */
    public static final int MAX_NOTIFICATIONS = 50;
    public static final String DATE_FORMAT = "EEEE, d MMMM";
    public static final int CALLSIGN_LENGTH = 4;

    /* --- Notifications --- */
    public static final String QUEUED_TITLE = "Kamerstuk met nummer: %s is in de wachtrij gezet.";
    public static final String QUEUED_TEXT = "Ingepland op: %s door %s";

    public static final String EDIT_TITLE = "Kamerstuk met nummer: %s is aangepast";
    public static final String EDIT_TEXT = "Aangepast door %s";

    public static final String REQUEUED_TITLE = "Kamerstuk met nummer: %s is verzet.";
    public static final String REQUEUED_TEXT = "Verzet door %s";

    public static final String DEQUEUED_TITLE = "Kamerstuk met nummer: %s is uit de wachtrij gehaald.";
    public static final String DEQUEUED_TEXT = "Uit de wachtrij gehaald door %s";

    public static final String DENIED_TITLE = "Kamerstuk met titel %s is geweigerd.";
    public static final String DENIED_TEXT = "Geweigerd door %s";

    public static final String WITHDRAW_TITLE = "Kamerstuk met titel %s is ingetrokken.";
    public static final String WITHDRAW_TEXT = "Ingetrokken door %s";

    public static final String DELAY_TITLE = "Stemming voor kamerstuk %s is uitgesteld";
    public static final String DELAY_TEXT = "Ingetrokken door %s";


    /* --- Reddit messages --- */

    public static final String QUEUED_SUBJECT = "Je kamerstuk is ingepland!";
    public static final String QUEUED_BODY = "Beste %s, \n \n Jouw kamerstuk met de titel \"%s\" is ingepland voor %s.";

    public static final String REQUEUED_SUBJECT = "Je kamerstuk is verzet.";
    public static final String REQUEUED_BODY = "Beste %s, \n \n Jouw kamerstuk met de titel \"%s\" is verzet naar %s.";

    public static final String DEQUEUED_SUBJECT = "Je kamerstuk is uit de wachtrij gehaald.";
    public static final String DEQUEUED_BODY = "Beste %s, \n \n Jouw kamerstuk met de titel \"%s\" is uit de wachtrij gehaald om de volgende reden: \"%s\".";

    public static final String DENIED_SUBJECT = "Het door jou ingediende kamerstuk is afgewezen.";
    public static final String DENIED_BODY = "Beste %s, \n \n Jouw kamerstuk met de titel \"%s\" is afgewezen om de volgende reden: \"%s\".";

    public static final String COMPLAINT_SUBJECT = "Er is een anonieme klacht ingediend";
    public static final String COMPLAINT_BODY = "Klacht: %s \n \n Berichtlink: %s";

    public static final String RVS_SUBJECT = "De Raad van State heeft advies uitgebracht op: %s";
    public static final String RVS_BODY = "Hallo, je hebt laatst een kamerstuk ingediend waarop de MRvS een advies uit moet brengen. Daarom krijg je hierbij je advies. \n \n " +
            "%s \n \n"+
            "Mocht je vragen hebben over het advies, geef dit aan. Dan kunnen we deze misschien wegnemen. \n \n" +
            "Als je naar aanleiding van het advies je kamerstuk wil aanpassen geef dit dan door aan het presidium. We raden aan in de toelichting op te nemen wat je veranderd hebt op basis van het advies. \n \n" +
            "Als je je kamerstuk niet wilt aanpassen, maar wel antwoord kan geven op de vragen verzoeken we je dit te doen. Op basis daarvan kunnen we eventueel alsnog het advies aanpassen. Ook als je je kamerstuk erg aanpast kan je om een aangepast advies vragen. Je moet hier dus wel zelf om vragen! \n \n" +
            "[Geef vooral ook feedback!](https://docs.google.com/forms/d/e/1FAIpQLSeXxAnYg9fsqLuPYHzzPhJg-JooihYGaVEZgKtmoz6n6DVAnw/viewform?usp=sf_link ) \n \n" +
            "**Let op, je kan op dit bericht geen reactie geven. Spreek hiervoor op discord een MRvS-lid aan.**";
}
