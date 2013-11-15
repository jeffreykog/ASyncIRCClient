package jk_5.asyncirc;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Queue;

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

/**
 * No description given
 *
 * @author jk-5
 */
public class IrcConnection {

    @Getter private final InetSocketAddress address;

    @Getter private String loginName;
    @Getter private String nickName;
    @Getter private String realName;
    @Getter private String serverPassword;

    @Getter @Setter private Charset charset = Charset.defaultCharset();
    @Getter private Channel channel;
    @Getter private boolean connected = false;
    @Getter private final Conversation serverConversation;
    private Queue<String> loginMessageQueue = new LinkedList<String>();

    public IrcConnection(InetSocketAddress address){
        this.address = address;
        this.serverConversation = new ServerConversation(address.getHostName());
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

    public Conversation getServerConversation(){
        return this.serverConversation;
    }

    public IrcConnection connect(){
        this.channel = new Bootstrap().remoteAddress(this.address).channel(NioSocketChannel.class).handler(new Initializer()).group(new NioEventLoopGroup()).connect().channel();
        return this;
    }

    private void handleLine(String line){
        System.out.println(">>> " + line);
        if(line.startsWith("PING ")){
            sendRaw("PONG " + line.substring(5), true);
            return;
        }
        String[] split = line.split(" ", 4);
        String source = split[0].substring(1);
        String operation = split[1];
        String target = split[2];
        String data = split[3].startsWith(":") ? split[3].substring(1) : split[3];
    }

    public final void sendRaw(String line){
        sendRaw(line, false);
    }

    private void sendRaw(String line, boolean force){
        if(!this.connected && !force){
            this.loginMessageQueue.add(line);
        }else{
            System.out.println("<<< " + line);
            this.channel.writeAndFlush(line + "\r\n");
        }
    }

    public ChannelFuture close(){
        if(this.channel == null) return null;
        return this.channel.close();
    }

    private final class Initializer extends ChannelInitializer<SocketChannel> {

        private final StringDecoder decoder = new StringDecoder(CharsetUtil.UTF_8);
        private final LineSepInsertor insertor = new LineSepInsertor();

        public final void initChannel(SocketChannel channel){
            //Downstream
            channel.pipeline().addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
            channel.pipeline().addLast("stringDecoder", this.decoder);
            //channel.pipeline().addLast("frameDecoder", frameDecoder);
            channel.pipeline().addLast("handler", new Handler());
            //Upstream
            channel.pipeline().addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
            //channel.pipeline().addLast("lineSep", insertor);
            //channel.pipeline().addLast("frameEncoder", frameEncoder);
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
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            handleLine(msg);
        }
    }
}
