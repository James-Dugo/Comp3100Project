import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

//this class is the highest level class handling the top level of the client runtime
public class MainClient {

    public static void main(String[] args) {
        args=new String[] {"lrr"};
        for(String arg:args){
            System.out.println(arg);
        }        
        if(args.length==1){
            if(args[0].equalsIgnoreCase("fc")){
                FcClient client=new FcClient();
                client.mainLoop();
            }else if(args[0].equalsIgnoreCase("lrr")){
                LrrClient client=new LrrClient();
                client.mainLoop();
            }
        } else {usage();}
    }

    private static void usage(){
        System.err.println("Some usage information");
        System.exit(1);
    }
}
