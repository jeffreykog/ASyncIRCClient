package jk_5.asyncirc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * No description given
 *
 * @author jk-5
 */
@Getter
@Setter
@AllArgsConstructor
public class User {

    private String nickName;
    private String mode = "";

    public void onMode(String mode){
        if(mode.startsWith("+")){
            mode = mode.substring(1);
            for(String s : mode.split("")){
                if(!this.mode.contains(s)){
                    this.mode += s;
                }
            }
        }else if(mode.startsWith("-")){
            mode = mode.substring(1);
            for(String s : mode.split("")){
                if(!this.mode.contains(s)){
                    this.mode = mode.replace(s, "");
                }
            }
        }
    }

    public boolean isOp(){
        return this.mode.contains("o");
    }

    public boolean isVoiced(){
        return this.mode.contains("v");
    }

    public boolean isHalfop(){
        return this.mode.contains("h");
    }
}
