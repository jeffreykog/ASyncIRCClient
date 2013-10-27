package jk_5.asyncirc;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import jk_5.asyncirc.frames.IrcFrame;

import java.util.List;

/**
 * No description given
 *
 * @author jk-5
 */
public class FrameEncoder extends MessageToMessageEncoder<IrcFrame> {

    public void encode(ChannelHandlerContext ctx, IrcFrame in, List<Object> out){
        in.encode(out);
    }
}
