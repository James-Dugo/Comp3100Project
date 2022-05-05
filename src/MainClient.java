//this class is the highest level class handling the top level of the client runtime
public class MainClient {

    static String hostname;
    static int port;

    public static void main(String[] args) {
        
        for(String arg:args){
            System.out.println(arg);
        }        
        if(args.length==3){
            hostname=args[1];
            port=Integer.parseInt(args[2]);

            if(args[0].equalsIgnoreCase("fc")){
                FcClient client=new FcClient(hostname,port);
                client.mainLoop();
            }else if(args[0].equalsIgnoreCase("lrr")){
                LrrClient client=new LrrClient(hostname,port);
                client.mainLoop();
            }
        } else {usage();}
    }

    private static void usage(){
        System.err.println("Some usage information");
        System.exit(1);
    }
}
