package jk_5.asyncirc.frames;

/**
 * No description given
 *
 * @author jk-5
 */
public class ErrorFrame extends IrcFrame {

    private final String message;
    private final String reason;

    public ErrorFrame(String message, String reason){
        this.message = message;
        this.reason = reason;
    }
}
