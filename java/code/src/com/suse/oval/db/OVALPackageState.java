package com.suse.oval.db;

import com.suse.oval.ovaltypes.LogicOperatorType;

import javax.persistence.*;
import java.util.Optional;

@Entity
@Table(name = "suseOVALPackageState")
public class OVALPackageState {
    private String id;
    private LogicOperatorType operator;
    private String comment;
    @ManyToOne
    @JoinColumn(name = "arch_state_id")
    private OVALPackageArchStateEntity packageArchState;
    @ManyToOne
    @JoinColumn(name = "version_state_id")
    private OVALPackageVersionStateEntity packageVersionState;
    @ManyToOne
    @JoinColumn(name = "evr_state_id")
    private OVALPackageEvrStateEntity packageEvrState;
    private boolean isRpm;

    @Column(name = "id")
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "operator")
    @Enumerated(EnumType.STRING)
    public LogicOperatorType getOperator() {
        return operator;
    }

    public void setOperator(LogicOperatorType operator) {
        this.operator = operator;
    }

    @Column(name = "comment")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Optional<OVALPackageArchStateEntity> getPackageArchState() {
        return Optional.ofNullable(packageArchState);
    }

    public void setPackageArchState(OVALPackageArchStateEntity packageArchState) {
        this.packageArchState = packageArchState;
    }

    public Optional<OVALPackageVersionStateEntity> getPackageVersionState() {
        return Optional.ofNullable(packageVersionState);
    }

    public void setPackageVersionState(OVALPackageVersionStateEntity packageVersionState) {
        this.packageVersionState = packageVersionState;
    }

    public Optional<OVALPackageEvrStateEntity> getPackageEvrState() {
        return Optional.ofNullable(packageEvrState);
    }

    public void setPackageEvrState(OVALPackageEvrStateEntity packageEvrState) {
        this.packageEvrState = packageEvrState;
    }

    @Column(name = "isRpm")
    public boolean isRpm() {
        return isRpm;
    }

    public void setRpm(boolean rpm) {
        isRpm = rpm;
    }
}
