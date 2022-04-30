//this handles creating the clients based on the algorithm
public class ClientFactory {
    public Client getClient(String algorithm){
        if(algorithm==null){
            return null;
        }
        if(algorithm.equalsIgnoreCase("lrr")){
            return new Lrr();
        }

        return null;
    }
}
