package jk_5.asyncirc;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * No description given
 *
 * @author jk-5
 */
@Sharable
class LineSepInsertor extends MessageToMessageEncoder<String> {

    public void encode(ChannelHandlerContext ctx, String msg, List<Object> out){
        out.add(msg + "\r\n");
    }
}
