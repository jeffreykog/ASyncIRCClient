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
    private ConnectionStage stage = ConnectionStage.AUTHENTICATE;

    IrcHandler(IrcConnection connection){
        this.connection = connection;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx){
        if(this.connection.getServerPassword() != null) ctx.writeAndFlush(new IrcFrame("PASS", this.connection.getServerPassword()));
        System.out.println("Authing");
        ctx.writeAndFlush(new IrcFrame("NICK", this.connection.getNickName()));
        ctx.writeAndFlush(new IrcFrame("USER", this.connection.getLoginName(), "8", ":" + this.connection.getRealName()));
        this.stage = ConnectionStage.LOGIN;
    }

    public void channelRead0(ChannelHandlerContext ctx, IrcFrame msg) throws Exception{
        if(this.stage == ConnectionStage.LOGIN){

        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
}
