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

    void fireOnJoin(User u){
        for(ConversationListener listener : this.listeners){
            listener.onJoin(u);
        }
    }

    void fireOnMode(User u, String change){
        for(ConversationListener listener : this.listeners){
            listener.onMode(u, change);
        }
    }

    void fireOnNick(User u, String oldNick){
        for(ConversationListener listener : this.listeners){
            listener.onNick(u, oldNick);
        }
    }

    void fireOnPart(User u, String message){
        for(ConversationListener listener : this.listeners){
            listener.onPart(u, message);
        }
    }

    void fireOnKick(User u, String message){
        for(ConversationListener listener : this.listeners){
            listener.onKick(u, message);
        }
    }

    public void kickUser(String user, String reason){

    }

    void onJoin(String nickName){
        this.onJoin(nickName, "");
    }

    void onJoin(String nickName, String mode){
        User u = new User(nickName, mode);
        this.users.add(u);
        this.fireOnJoin(u);
    }

    void onMode(String user, String mode){
        User u = this.getUserFromNickname(user);
        u.onMode(mode);
        this.fireOnMode(u, mode);
    }

    void onNick(String oldNick, String newNick){
        User u = this.getUserFromNickname(oldNick);
        u.setNickName(newNick);
        this.fireOnNick(u, oldNick);
    }

    void onPart(String nick, String message){
        User u = this.getUserFromNickname(nick);
        this.users.remove(u);
        this.fireOnPart(u, message);
    }

    void onKick(String nick, String message){
        User u = this.getUserFromNickname(nick);
        this.users.remove(u);
        this.fireOnKick(u, message);
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
