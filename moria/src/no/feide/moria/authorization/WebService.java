package no.feide.moria.authorization;

import java.util.HashMap;

public class WebService {

    private String id;
    private HashMap allowedAttributes = new HashMap();
    private HashMap deniedAttributes = new HashMap();
    private String name;
    private String url;
    
    protected WebService(String id) {
        this.id = id;
    }

    protected String getId() {
        return id;
    }

    protected void setAllowedAttributes(HashMap allowed) {
        allowedAttributes = allowed;
    }

    protected void setDeniedAttributes(HashMap denied) {
        deniedAttributes = denied;
    }

    protected void setName(String name) {
        this.name = name;
    }
    
    protected void setUrl(String url) {
        this.url = url;
    }

    protected String getUrl() {
        return url;
    }
        
    protected String getName() {
        return name;
    }

}
