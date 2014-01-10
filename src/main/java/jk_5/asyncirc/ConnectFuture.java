package jk_5.asyncirc;

import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * No description given
 *
 * @author jk-5
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ConnectFuture {

    private final ChannelFuture future;
    @Getter
    private final IrcConnection connection;

    public IrcConnection sync() throws InterruptedException{
        this.future.sync();
        return this.connection;
    }

    public IrcConnection syncUninterruptibly(){
        this.future.syncUninterruptibly();
        return this.connection;
    }

    public IrcConnection addListener(GenericFutureListener<? extends Future<? super Void>> listener){
        this.future.addListener(listener);
        return this.connection;
    }

    public IrcConnection addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners){
        this.future.addListeners(listeners);
        return this.connection;
    }

    public IrcConnection removeListener(GenericFutureListener<? extends Future<? super Void>> listener){
        this.future.removeListener(listener);
        return this.connection;
    }

    public IrcConnection removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners){
        this.future.removeListeners(listeners);
        return this.connection;
    }

    public boolean isSuccess(){
        return future.isSuccess();
    }

    public boolean isCancellable(){
        return future.isCancellable();
    }

    public Throwable cause(){
        return future.cause();
    }

    public boolean isCancelled(){
        return future.isCancelled();
    }

    public boolean isDone(){
        return future.isDone();
    }
}
