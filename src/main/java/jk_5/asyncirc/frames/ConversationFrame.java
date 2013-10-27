package jk_5.asyncirc.frames;

/**
 * No description given
 *
 * @author jk-5
 */
public class ConversationFrame extends IrcFrame {

    public ConversationFrame(String sender, String msg){
        System.out.println(sender + "  >> " + msg);
    }
}
