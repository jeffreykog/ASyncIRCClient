package jk_5.asyncirc;

import io.netty.channel.ChannelFuture;
import lombok.RequiredArgsConstructor;

/**
 * No description given
 *
 * @author jk-5
 */
@RequiredArgsConstructor
public class JoinFuture {

    private final Conversation conversation;
    private final ChannelFuture future;

    public Conversation conversation(){
        return this.conversation;
    }

    public JoinFuture sync() throws InterruptedException{
        this.future.sync();
        return this;
    }

    public JoinFuture syncUninterruptibly(){
        this.future.syncUninterruptibly();
        return this;
    }

    public JoinFuture await() throws InterruptedException{
        this.future.await();
        return this;
    }

    public JoinFuture awaitUninterruptibly(){
        this.future.awaitUninterruptibly();
        return this;
    }
}
