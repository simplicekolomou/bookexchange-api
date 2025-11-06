package ovh.bookexchange.api.domains.entities;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class TestEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String value;

    public TestEntity() {} //créé pour que l'orm puisse initialiser les objets, faut voir si l'enlever casse toujours

    public TestEntity(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("TestEntity[id='%s', value='%s']", id, value);
    }

    public long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
