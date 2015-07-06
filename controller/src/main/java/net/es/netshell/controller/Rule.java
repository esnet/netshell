package net.es.netshell.controller;

import net.es.netshell.api.Resource;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.IOException;
import java.util.UUID;
/**
 * Created by lomax on 7/6/15.
 */

public class Rule extends Resource {
    @JsonIgnore
    private UUID uuid;

    public Rule (String name) {
        super(name);
        this.uuid = UUID.randomUUID();
    }

    public String getUuid() {
        if (this.uuid == null) {
            UUID.randomUUID();
        }
        return uuid.toString();
    }

    public void setUuid(String uuid) {
        this.uuid = UUID.fromString(uuid);
    }

    /**
     * Activates the rule
     * @throws IOException if the rule c.annot be activated
     */
    public void activate() throws IOException {
        throw new IOException("not implemented");
    }

    /**
     * Stops the rule
     * @throws IOException
     */
    public void disactivate() throws IOException{
        throw new IOException("not implemented");
    }
    /**
     * Destroys the rule
     * @throws IOException
     */
    public void destroy() throws IOException{
        throw new IOException("not implemented");
    }
}
