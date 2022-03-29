package Comp3100Project;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Client {
    private Socket s;
    private DataOutputStream dout;
    private BufferedReader din;
    private String reply;

    static {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (SecurityException | IOException e1) {
            e1.printStackTrace();
        }
    }
    private static final Logger logger=Logger.getLogger(Client.class.getName());

    /**
     * 
     * @param hostname
     * @param port
     */
    public void newConn(String hostname, int port){
        try{
            s=new Socket(hostname, port);
            dout=new DataOutputStream(s.getOutputStream());
            din=new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch(Exception e){System.out.println(e);}

    }
    /**
     * reads a line in from din
     * @return
     */
    private String recieve() {
        try{return din.readLine();}
        catch(IOException e){
            logger.log(Level.SEVERE,"ERR: "+e);
            return null;
        }
	}
    /**
     * Sends a message to dout
     * @param msg
     */
	private void write(String msg) {
        try {
            dout.write(msg.getBytes());
            dout.flush();
        } catch(IOException e){
            logger.log(Level.SEVERE,"ERR: "+e);
        }
	}
    /**
     * Sends a message and gets the reply from the connection, TODO make this less punishing if no reply exits
     * @param msg
     * @return reply
     */
    private String send(String msg){
        this.write(msg);
        reply=this.recieve();
        if(reply!=null){return reply;}
        else{
            logger.log(Level.SEVERE,"ERR: No reply, terminating"); 
            System.exit(1);
            return null;
        }
    }

	public static void main(String[] args){
        Client client=new Client();
        //Hello
        client.newConn("localhost",50000);
        reply=client.send("HELO\n");
        //Auth
        String username=System.getProperty("user.name");
        dout.write(("AUTH "+username+"\n").getBytes());
        dout.flush();
        reply=din.readLine();
        System.out.println("Recieved: "+reply);

        //Ready
        dout.write("REDY\n".getBytes());
        dout.flush();
        reply=din.readLine();
        System.out.println("Recieved: "+reply);


        //Quit
        dout.write("REDY\n".getBytes());
        dout.flush();
        reply=din.readLine();
        System.out.println("Recieved: "+reply);

        dout.close();
        s.close();
        }catch(Exception e){System.out.println(e);}
    }
}