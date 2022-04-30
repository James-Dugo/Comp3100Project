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
    public static final Logger logger=Logger.getLogger(LrrClient.class.getName());

    public static void main(String[] args) {
        
        if(args.length<2){usage();}
        ClientFactory factory=new ClientFactory();

    }

    private static void usage(){
        System.err.println("Some usage information");
        System.exit(1);
    }
}
