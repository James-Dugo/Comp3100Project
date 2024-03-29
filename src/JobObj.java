public class JobObj {
    private int jobId;
    private int jobCores;
    private int jobMem;
    private int jobDisk;
    private int jobState;
    
    public JobObj(String jobString){
        String jobArr[]=jobString.split(" ");
        this.jobId=Integer.parseInt(jobArr[2]);
        this.jobCores=Integer.parseInt(jobArr[4]);
        this.jobMem=Integer.parseInt(jobArr[5]);
        this.jobDisk=Integer.parseInt(jobArr[6]);
    }

    public JobObj(String[] sArr) {
        this.jobId=Integer.parseInt(sArr[0]);
        this.jobState=Integer.parseInt(sArr[1]);
        this.jobCores=Integer.parseInt(sArr[5]);
        this.jobMem=Integer.parseInt(sArr[6]);
        this.jobDisk=Integer.parseInt(sArr[7]);
    }

    public int getID(){return jobId;}
    public int getCores(){return jobCores;}
    public int getMem(){return jobMem;}
    public int getDisk(){return jobDisk;}
    public int getState(){return jobState;}

    /**
     * 
     */
    public String[] capableMsg(){
        String[] msg=new String[3];
        msg[0]=Integer.toString(jobCores);
        msg[1]=Integer.toString(jobMem);
        msg[2]=Integer.toString(jobDisk);
        return msg;
    }
}
