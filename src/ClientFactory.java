//this handles creating the clients based on the algorithm being specified
public class ClientFactory {
    public Client getClient(String algorithm){
        if(algorithm==null){
            return null;
        }
        if(algorithm.equalsIgnoreCase("lrr")){
            return new LrrClient();
        }
        if(algorithm.equalsIgnoreCase("fc")){
            return new FcClient();
        }

        return null;
    }
}
