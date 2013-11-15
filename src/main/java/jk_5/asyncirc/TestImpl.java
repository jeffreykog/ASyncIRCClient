package jk_5.asyncirc;

/**
 * No description given
 *
 * @author jk-5
 */
public class TestImpl {

    private static IrcConnection connection = new IrcConnection("irc.reening.nl");

    public static void main(String[] args) throws Exception {
        connection.setLoginName("TestBot").setNickname("TestBot").setRealName("TestBot");
        connection.connect();
        connection.getChannel().closeFuture().sync();
    }
}
