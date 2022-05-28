//this class is the highest level class handling the top level of the client runtime
public class MainClient {

    static String hostname;
    static int port;
    static String algorithm;
    static Boolean verbose=false;

    public static void main(String[] args) {

        if( (args.length%2!=0)||(args.length==0)){usage();}
        /**
         * Goes through the arguments and sets the hostname,port and algorithm based on them
         * -u [hostname:port] -a [fc|lrr] defaults 
         */
        for(int i=0;i<args.length;i++){
            switch(args[i]){
                case "-u":
                    String[] temp=args[i+1].split(":");
                    hostname=temp[0];
                    port=Integer.parseInt(temp[1]);
                    i++;
                    break;
                case "-a":
                    algorithm=args[i+1];
                    i++;
                    break;
                case "-v":
                    verbose=Boolean.parseBoolean(args[i+1]);
                    i++;
                    break;
                default:
                    System.err.println("Using default");
                    algorithm="tt";
                    hostname="localhost";
                    port=50000;
            }
        }

        if(algorithm.equalsIgnoreCase("fc")){
            FcClient client=new FcClient(hostname,port,verbose);
            client.runClient();
        }else if(algorithm.equalsIgnoreCase("lrr")){
            LrrClient client=new LrrClient(hostname,port,verbose);
            client.runClient();
        }else if(algorithm.equalsIgnoreCase("tt")){
            TtClient client=new TtClient(hostname,port,verbose);
            client.runClient();
        }
    }

    private static void usage(){
        System.err.println("Some usage information");
        System.exit(1);
    }
}
