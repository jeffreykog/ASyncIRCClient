package jk_5.asyncirc;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import lombok.Getter;

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

    private final IrcConnectThread connectThread = new IrcConnectThread();
    private Channel channel;

    public IrcConnection(InetSocketAddress address){
        this.address = address;
    }

    public IrcConnection(String host){
        this(host, 6667);
    }

    public IrcConnection(String host, int port){
        this.address = new InetSocketAddress(host, port);
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

    public void connect(){
        this.connectThread.run();
    }

    public ChannelFuture close(){
        if(this.channel == null) return null;
        return this.channel.close();
    }

    private final class IrcConnectThread extends Thread {

        @Override
        public void run(){
            NioEventLoopGroup group = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>(){

                private final StringDecoder decoder = new StringDecoder(CharsetUtil.UTF_8);
                private final LineSepInsertor insertor = new LineSepInsertor();

                public final void initChannel(SocketChannel channel){
                    channel.pipeline().addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                    channel.pipeline().addLast("decoder", this.decoder);
                    channel.pipeline().addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                    channel.pipeline().addLast("lineSep", insertor);
                }
            });
            try{
                ChannelFuture future = bootstrap.connect(IrcConnection.this.address);
                IrcConnection.this.channel = future.channel();
                if(!future.isSuccess()) IrcConnection.this.close();
                IrcConnection.this.channel.closeFuture().syncUninterruptibly();
            }finally{
                group.shutdownGracefully();
            }
        }
    }
}
