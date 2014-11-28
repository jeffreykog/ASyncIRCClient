package jk_5.asyncirc;

/**
 * No description given
 *
 * @author jk-5
 */
public interface ConversationListener {

    void onMessage(User sender, String message);
    void onJoin(User user);
    void onMode(User u, String change);
    void onNick(User u, String oldNick);
    void onPart(User u, String message);
    void onKick(User u, String message);
}
