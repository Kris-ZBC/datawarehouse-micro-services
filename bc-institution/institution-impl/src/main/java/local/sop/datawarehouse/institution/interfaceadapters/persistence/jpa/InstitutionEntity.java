package local.sop.datawarehouse.institution.interfaceadapters.persistence.jpa;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "institution")
public class InstitutionEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", nullable = false)
    private String address;

   

    protected InstitutionEntity() {}

    public InstitutionEntity(UUID id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
        
    }

    
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
    @Version
    private Long version;
    
}
