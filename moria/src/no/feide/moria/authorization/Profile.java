package no.feide.moria.authorization;

import java.util.HashMap;

public class Profile {

    private String name;
    private String id;
    private HashMap attributes = new HashMap();

    public Profile(String id) {
        this.id = id;
        System.out.println("Creating profile object: "+id);
    }

    protected void addAttribute(Attribute attribute, boolean sso) {
        System.out.println("Adding attribute. Name = "+attribute.getName()+" SSO="+attribute.getSso());
        attributes.put(attribute, new Boolean(sso));

    }

    protected HashMap getAttributes() {
        return attributes;
    }

    public String getName() {
        return name;
    }

}
