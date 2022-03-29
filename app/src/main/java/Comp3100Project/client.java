package Comp3100Project;
import java.io.*;
import java.net.*;
public class client {
    private Socket s;
    private DataOutputStream dout;
    private BufferedReader din;
    private String reply;

    public void newConn(String hostname, int port){
        try{
            s=new Socket(hostname, port);
            dout=new DataOutputStream(s.getOutputStream());
            din=new BufferedReader(new InputStreamReader(s.getInputStream()));
            this.write("HELO\n");
            reply=this.recieve();

        } catch(Exception e){System.out.println(e);}

    }
    private String recieve() {
        try{return din.readLine();}
        catch(IOException e){logger.log(ERR,"Error: "+e);}
	}
	private void write(String msg) {
        try {
            dout.write(msg.getBytes());
            dout.flush();
        } catch (Exception e) {
            //TODO: handle exception
        }
	}
	public static void main(String[] args){
        try{
            Socket s=new Socket("localhost",60000);
            DataOutputStream dout=new DataOutputStream(s.getOutputStream());
            BufferedReader din=new BufferedReader(new InputStreamReader(s.getInputStream()));
            //Hello
            dout.write("HELO\n".getBytes());
            dout.flush();
            String reply=din.readLine();
            System.out.println("Recieved: "+reply);
            
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