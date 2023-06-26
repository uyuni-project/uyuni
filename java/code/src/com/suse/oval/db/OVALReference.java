package com.suse.oval.db;

import javax.persistence.*;

@Entity
@Table(name = "suseOVALReference")
@IdClass(OVALReferenceKey.class)
@NamedQueries({
        @NamedQuery(name = "OVALReference.lookupReferenceByRefIdAndDefinition",
        query = "SELECT ref FROM OVALReference ref " +
                "WHERE ref.refId = :refId AND ref.definition.id = :definitionId")
})
public class OVALReference {
    private String refId;
    private String source;
    private String refURL;
    private OVALDefinition definition;

    public OVALReference() {

    }

    @Id
    @Column(name = "ref_id")
    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    @Column(name = "source")
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Column(name = "url")
    public String getRefURL() {
        return refURL;
    }

    public void setRefURL(String refURL) {
        this.refURL = refURL;
    }

    public void setDefinition(OVALDefinition definition) {
        this.definition = definition;
    }

    @Id
    @ManyToOne
    @JoinColumn(name = "definition_id")
    public OVALDefinition getDefinition() {
        return definition;
    }
}
