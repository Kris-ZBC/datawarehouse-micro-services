package local.sop.datawarehouse.sop.interfaceadapters.persistence.jpa;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "sop", schema = "sop")
public class SOPEntity {
    @Id
    @Column(name = "id")
    UUID id;
    @Column(name = "name", nullable = false)
    String name;
    @Column(name = "address", nullable = false)
    String address;
    @Column(name = "education", nullable = false)
    String education;
    @Version
    private long version;


    public String getName() {
        return name;
    }

     public UUID getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getEducation() {
        return education;
    }

   

    protected SOPEntity() {}  // jpa requires default constructor

    public SOPEntity(UUID id, String name, String address, String education) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.education = education;
    }
}
