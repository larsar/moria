public class Attribute {

    private String name;
    private boolean sso;

    public Attribute(String name, String sso) {
        this.name = name;

        if (sso.equals("true"))
            this.sso = true;
        else 
            this.sso = false;

        System.out.println("Creating attribute. Name = "+name+" SSO = "+this.sso);

    }

    public String getName() {
        return name;
    }

    public boolean getSso() {
        return sso;
    }


}
