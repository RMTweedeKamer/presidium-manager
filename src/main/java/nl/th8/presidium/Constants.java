package nl.th8.presidium;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Constants {

    /* Properties */
    public static final int MAX_NOTIFICATIONS = 50;
    public static final String DATE_FORMAT = "EEEE, d MMMM";
    public static final int CALLSIGN_LENGTH = 4;

    /* --- Notifications --- */
    public static final String NO_TEXT = "Geen extra informatie beschikbaar.";

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
}
