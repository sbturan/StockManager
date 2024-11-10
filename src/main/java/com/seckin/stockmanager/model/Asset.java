package com.seckin.stockmanager.model;


import jakarta.persistence.*;

@Entity
@Table(
        name = "assets",
        uniqueConstraints = @UniqueConstraint(columnNames = {"customerId", "name"})
)
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private long customerId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private Double size;

    @Column(nullable = false)
    private Double usableSize;
    @Version
    private Integer version;


    public Asset(){}
    public Asset(long customerId, String name, Double size, Double usableSize) {
        this.customerId = customerId;
        this.name = name;
        this.size = size;
        this.usableSize = usableSize;
    }

    public Long getId() {
        return id;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getSize() {
        return size;
    }

    public void setSize(Double size) {
        this.size = size;
    }

    public Double getUsableSize() {
        return usableSize;
    }

    public void setUsableSize(Double usableSize) {
        this.usableSize = usableSize;
    }

}
