package jk_5.asyncirc;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import jk_5.asyncirc.frames.IrcFrame;

/**
 * No description given
 *
 * @author jk-5
 */
class IrcHandler extends SimpleChannelInboundHandler<IrcFrame> {

    private final IrcConnection connection;

    IrcHandler(IrcConnection connection){
        this.connection = connection;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx){

    }

    public void channelRead0(ChannelHandlerContext ctx, IrcFrame msg) throws Exception{

    }
}
