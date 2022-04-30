import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

//this class is the highest level class handling the top level of the client runtime
public class MainClient {

    static {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("logging.properties"));
        } catch (SecurityException | IOException e1) {
            e1.printStackTrace();
        }
    }
    public static final Logger logger=Logger.getLogger(MainClient.class.getName());

    public static void main(String[] args) {
        
        if(args.length==2){
            if(args[1].equalsIgnoreCase("fc")){
                FcClient client=new FcClient();
                client.mainLoop();
            }else if(args[1].equalsIgnoreCase("lrr")){
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
