package jk_5.asyncirc;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import jk_5.asyncirc.frames.ConversationFrame;
import jk_5.asyncirc.frames.ErrorFrame;

import java.util.List;

/**
 * No description given
 *
 * @author jk-5
 */
@Sharable
public class FrameDecoder extends MessageToMessageDecoder<String> {

    public void decode(ChannelHandlerContext ctx, String in, List<Object> out){
        System.out.println(in);
        if(in.startsWith("ERROR")){
            int first = in.indexOf(":");
            int second = in.indexOf(":", first);
            String msg = in.substring(first, second);
            first = in.indexOf("(", second);
            second = in.lastIndexOf(")");
            out.add(new ErrorFrame(msg, in.substring(first, second)));
        }else if(in.startsWith(":")){
            String sender = in.substring(1, in.indexOf(" "));
            String msg = in.substring(in.indexOf(" "));
            out.add(new ConversationFrame(sender, msg));
        }else{
            ctx.fireChannelRead(in);
        }
    }
}
