import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public abstract class Client {
    Socket s;
    DataOutputStream dout;
    BufferedReader din;
    String reply;
    String[] splitReply;
    Boolean flag=false;

    List<ServerObj> serverList;
    int jobId;
    int jobCores;
    int jobMem;
    int jobDisk;

    void send(){

    }
    void write(){

    }
    void getCapable(){
        
    }
}
