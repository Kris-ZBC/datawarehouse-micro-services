package local.sop.sopinfo.organisation.interfaceadapters.persistence.jpa;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name="organisation")
public class OrganisationEntity {
    @Id
    @Column(name="id")
    UUID id;
    @Column(name="name", nullable=false)
    String name;
    @Column(name="cvr", nullable=false)
    String cvr;
    @Version
    private long version;




    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }
    
    public String getCvr() {
        return cvr;
    }

    protected OrganisationEntity(){}

    public OrganisationEntity(UUID id,String name, String cvr){
        this.id = id;
        this.cvr = cvr;
        this.name = name;
    }
}
