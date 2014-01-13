package jk_5.asyncirc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * No description given
 *
 * @author jk-5
 */
public final class IrcConnection {

    @Getter private final InetSocketAddress address;

    @Getter private String loginName;
    @Getter private String nickName;
    @Getter private String realName;
    @Getter private String serverPassword;
    @Getter @Setter private String nickservPassword;

    @Getter @Setter private Charset charset = Charset.defaultCharset();
    @Getter private Channel channel;
    @Getter private boolean connected = false;
    @Getter private final Conversation serverConversation;
    private Queue<String> loginMessageQueue = new LinkedList<String>();
    private Map<String, ChannelPromise> joiningChannels = new HashMap<String, ChannelPromise>();
    private Map<String, Conversation> joinedChannels = new HashMap<String, Conversation>();
    private EventLoopGroup worker;
    private ChannelPromise connectPromise;

    public IrcConnection(InetSocketAddress address){
        this.address = address;
        this.serverConversation = new ServerConversation(this, address.getHostName());
    }

    public IrcConnection(String host){
        this(host, 6667);
    }

    public IrcConnection(String host, int port){
        this(new InetSocketAddress(host, port));
    }

    public IrcConnection setLoginName(String loginName){
        this.loginName = loginName;
        return this;
    }

    public IrcConnection setName(String name){
        this.nickName = name;
        this.realName = name;
        this.loginName = name;
        return this;
    }

    public IrcConnection setNickname(String nickname){
        this.nickName = nickname;
        return this;
    }

    public IrcConnection setRealName(String realName){
        this.realName = realName;
        return this;
    }

    public IrcConnection setServerPassword(String serverPassword){
        this.serverPassword = serverPassword;
        return this;
    }

    public ConnectFuture connect(){
        return this.connect(new NioEventLoopGroup());
    }

    public ConnectFuture connect(NioEventLoopGroup worker){
        this.worker = worker;
        ChannelFuture future = new Bootstrap().remoteAddress(this.address).channel(NioSocketChannel.class).handler(new Initializer()).group(this.worker).connect();
        this.channel = future.channel();
        this.connectPromise = this.channel.newPromise();
        return new ConnectFuture(this.connectPromise, this);
    }

    private void handleLine(String line){
        if(line.startsWith("PING ")){
            sendRaw("PONG " + line.substring(5), true);
            return;
        }
        String[] split = line.split(" ", 4);
        String source = split[0].substring(1);
        String operation = split[1];
        //String target = split[2];
        //String data = split[3].startsWith(":") ? split[3].substring(1) : split[3];

        if(operation.equals("433")){
            this.sendRaw("NICK " + this.nickName + "_", true);
        }else if(operation.equals("004")){
            this.connected = true;
            while(!this.loginMessageQueue.isEmpty()){
                this.sendRaw(this.loginMessageQueue.poll());
            }
            if(this.nickservPassword != null && !this.nickservPassword.isEmpty()){
                this.sendRaw("NICKSERV IDENTIFY " + this.nickservPassword);
            }
            this.connectPromise.setSuccess();
        }else if(operation.equals("353")){
            String[] users = line.split(":", 3)[2].split(" ");
            Conversation channel = this.joinedChannels.get(line.split("@", 2)[1].trim().split(" ", 2)[0]);
            for(String s : users){
                String mode = "";
                if(s.startsWith("@")){
                    mode = "o";
                    s = s.substring(1);
                }else if(s.startsWith("+")){
                    mode = "v";
                    s = s.substring(1);
                }else if(s.startsWith("%")){
                    mode = "h";
                    s = s.substring(1);
                }
                channel.onJoin(s, mode);
            }
        }else if(operation.equals("JOIN") && source.startsWith(this.getNickName())){
            String channel = split[2].substring(1);
            if(this.joiningChannels.containsKey(channel)){
                ChannelPromise promise = this.joiningChannels.get(channel);
                promise.setSuccess();
                this.joiningChannels.remove(channel);
            }
        }else if(operation.equals("PRIVMSG")){
            String channel = split[2];
            if(this.joinedChannels.containsKey(channel)){
                Conversation conversation = this.joinedChannels.get(channel);
                conversation.fireOnMessage(line.substring(1).split("!", 2)[0], line.split(":", 3)[2]);
            }
        }else if(operation.equals("JOIN")){
            this.joinedChannels.get(split[2]).onJoin(source.split("!", 2)[0]);
        }else if(operation.equals("PART")){
            String nick = source.split("!", 2)[0];
            String message = line.split(":", 3)[2];
            this.joinedChannels.get(split[2]).onPart(nick, message);
        }else if(operation.equals("KICK")){
            String nick = line.split(" ", 5)[3];
            String message = line.split(" ", 5)[4].substring(1);
            this.joinedChannels.get(split[2]).onKick(nick, message);
        }else if(operation.equals("MODE")){
            if(split[2].startsWith("+") || split[2].startsWith("-")){
                this.joinedChannels.get(split[2]).onMode(line.split(" ", 5)[4], line.split(" ", 5)[3]);
            }
        }else if(operation.equals("NICK")){
            String oldNick = source.split("!", 2)[0];
            String newNick = split[2].substring(1);
            for(Conversation channel : this.joinedChannels.values()){
                channel.onNick(oldNick, newNick);
            }
        }
    }

    public final ChannelFuture sendRaw(String line){
        return sendRaw(line, false);
    }

    private ChannelFuture sendRaw(String line, boolean force){
        if(!this.connected && !force){
            this.loginMessageQueue.add(line);
            return null;
        }else{
            return this.channel.writeAndFlush(line + "\r\n");
        }
    }

    public final JoinFuture joinChannel(String channel){
        ChannelPromise promise = this.channel.newPromise();
        Conversation conversation = new ChannelConversation(this, channel);
        this.joinedChannels.put(channel, conversation);
        this.sendRaw("JOIN " + channel);
        this.joiningChannels.put(channel, promise);
        return new JoinFuture(conversation, promise);
    }

    public Conversation getConversation(String channel){
        return this.joinedChannels.get(channel);
    }

    public ChannelFuture close(){
        if(this.channel == null) return null;
        ChannelFuture future = this.channel.close();
        return future;
    }

    private final class Initializer extends ChannelInitializer<SocketChannel> {

        private final StringDecoder decoder = new StringDecoder(CharsetUtil.UTF_8);
        private final StringEncoder encoder = new StringEncoder(CharsetUtil.UTF_8);

        public final void initChannel(SocketChannel channel){
            channel.pipeline().addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
            channel.pipeline().addLast("stringDecoder", this.decoder);
            channel.pipeline().addLast("encoder", this.encoder);
            channel.pipeline().addLast("handler", new Handler());
        }
    }

    private class Handler extends SimpleChannelInboundHandler<String>{

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            sendRaw("NICK " + IrcConnection.this.nickName, true);
            sendRaw("USER " + IrcConnection.this.loginName + " 8 * :" + IrcConnection.this.realName, true);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception{
            handleLine(msg);
        }
    }
}
