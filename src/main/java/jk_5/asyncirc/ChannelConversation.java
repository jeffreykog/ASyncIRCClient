package jk_5.asyncirc;

/**
 * No description given
 *
 * @author jk-5
 */
class ChannelConversation extends Conversation {

    ChannelConversation(IrcConnection connection, String name){
        super(connection, name);
    }

    @Override
    public void kickUser(String user, String reason){
        this.getConnection().sendRaw("KICK " + this.getName() + " " + user + " :" + reason);
    }
}
