package net.es.netshell.shell;

/**
 * Author: sowmya
 * Date: 4/9/15
 * Time: 5:06 PM
 */
public class CommandResponse {

    String message;


    boolean result;

    public CommandResponse(String message, boolean resultCode){
        this.message = message;
        this.result = resultCode;
    }

    public boolean isSuccess() {

        return result;
    }

    public String getMessage() {

        return message;
    }


}
