package entities;

public class config {

    private String TOKEN;
    private String[] BOTOWNERS;
    private String DBHOST;
    private String DBUSER;
    private String DBPW;

    public void setTOKEN(String TOKEN) {
        this.TOKEN = TOKEN;
    }

    public void setBOTOWNERS(String[] BOTOWNERS) {
        this.BOTOWNERS = BOTOWNERS;
    }

    public void setDBHOST(String DBHOST) {
        this.DBHOST = DBHOST;
    }

    public void setDBUSER(String DBUSER) {
        this.DBUSER = DBUSER;
    }

    public void setDBPW(String DBPW) {
        this.DBPW = DBPW;
    }

    public String getTOKEN() {
        return TOKEN;
    }

    public String[] getBOTOWNERS() {
        return BOTOWNERS;
    }

    public String getDBHOST() {
        return DBHOST;
    }

    public String getDBUSER() {
        return DBUSER;
    }

    public String getDBPW() {
        return DBPW;
    }
}
