import java.util.HashMap;

public class Profile {

    private String name;
    private HashMap attributes = new HashMap();

    public Profile(String name) {
        this.name = name;
        System.out.println("Creating profile object: "+name);
    }

    protected void addAttribute(Attribute attribute, String sso) {
        System.out.println("Adding attribute. Name = "+attribute.getName()+" SSO="+attribute.getSso());
        if (sso.equals("true"))
            attributes.put(attribute, new Boolean(true));
        else
            attributes.put(attribute, new Boolean(false));

    }

    protected HashMap getAttributes() {
        return attributes;
    }

    public String getName() {
        return name;
    }

}
