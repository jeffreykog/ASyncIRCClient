package jk_5.asyncirc.frames;

import com.google.common.base.Joiner;

import java.util.List;

/**
 * No description given
 *
 * @author jk-5
 */
public class IrcFrame {

    private String command;
    private String[] args;

    public IrcFrame(){

    }

    public IrcFrame(String command, String... args){
        this.command = command;
        this.args = args;
    }

    public void encode(List<Object> out){
        System.out.println("> " + command + " " + Joiner.on(" ").join(this.args));
        out.add(command + " " + Joiner.on(" ").join(this.args));
    }
}
