package jk_5.asyncirc;

import io.netty.channel.ChannelFuture;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * No description given
 *
 * @author jk-5
 */
public abstract class Conversation {

    @Getter private final String name;
    @Getter private final IrcConnection connection;
    private final List<ConversationListener> listeners = new ArrayList<ConversationListener>();
    private final List<User> users = new ArrayList<User>();

    protected Conversation(IrcConnection connection, String name){
        this.name = name;
        this.connection = connection;
    }

    public ChannelFuture sendMessage(String message){
        return this.connection.sendRaw("PRIVMSG " + this.name + " :" + message);
    }

    public IrcConnection leaveChannel(){
        this.connection.sendRaw("PART " + this.name);
        return this.connection;
    }

    public Conversation addListener(ConversationListener listener){
        this.listeners.add(listener);
        return this;
    }

    void fireOnMessage(String sender, String message){
        for(ConversationListener listener : this.listeners){
            listener.onMessage(sender, message);
        }
    }

    public void kickUser(String user, String reason){

    }

    void onJoin(String nickName){
        this.onJoin(nickName, "");
    }

    void onJoin(String nickName, String mode){
        this.users.add(new User(nickName, mode));
    }

    void onMode(String user, String mode){
        for(User u : this.users){
            if(u.getNickName().equals(user)){
                u.onMode(mode);
            }
        }
    }

    void onNick(String oldNick, String newNick){
        for(User u : this.users){
            if(u.getNickName().equals(oldNick)){
                u.setNickName(newNick);
            }
        }
    }

    void onPart(String nick, String message){
        for(User u : this.users){
            if(u.getNickName().equals(nick)){
                this.users.remove(u);
            }
        }
    }

    void onKick(String nick, String message){
        for(User u : this.users){
            if(u.getNickName().equals(nick)){
                this.users.remove(u);
            }
        }
    }

    public User getUserFromNickname(String nickname){
        for(User u : this.users){
            if(u.getNickName().equals(nickname)){
                return u;
            }
        }
        return null;
    }
}
