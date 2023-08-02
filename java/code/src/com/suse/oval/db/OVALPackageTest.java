package com.suse.oval.db;

import com.suse.oval.ovaltypes.CheckEnum;
import com.suse.oval.ovaltypes.ExistenceEnum;
import com.suse.oval.ovaltypes.LogicOperatorType;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Optional;

@Entity
@Table(name = "suseOVALPackageTest")
public class OVALPackageTest {
    private String id;
    private String comment;
    private ExistenceEnum checkExistence;
    private CheckEnum check;
    private LogicOperatorType stateOperator;
    private boolean isRpm;
    private OVALPackageObject packageObject;
    @ManyToOne
    @JoinColumn(name = "pkg_state_id")
    @Access(AccessType.FIELD)
    private OVALPackageState packageState;

    @Column(name = "id")
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "comment")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Column(name = "check_exist")
    @Enumerated(EnumType.STRING)
    public ExistenceEnum getCheckExistence() {
        if (checkExistence == null) {
            return ExistenceEnum.AT_LEAST_ONE_EXISTS;
        }
        return checkExistence;
    }

    public void setCheckExistence(ExistenceEnum checkExistence) {
        this.checkExistence = checkExistence;
    }

    @Column(name = "test_check") // check is reserved in PostgresSQL
    @Enumerated(EnumType.STRING)
    public CheckEnum getCheck() {
        return check;
    }

    public void setCheck(CheckEnum check) {
        this.check = check;
    }

    @Column(name = "state_operator")
    @Enumerated(EnumType.STRING)
    public LogicOperatorType getStateOperator() {
        if (stateOperator == null) {
            return LogicOperatorType.AND;
        } else {
            return stateOperator;
        }
    }

    public void setStateOperator(LogicOperatorType stateOperator) {
        this.stateOperator = stateOperator;
    }

    @JoinColumn(name = "pkg_object_id")
    @ManyToOne(optional = false)
    public OVALPackageObject getPackageObject() {
        return packageObject;
    }

    public void setPackageObject(OVALPackageObject packageObject) {
        this.packageObject = packageObject;
    }

    public Optional<OVALPackageState> getPackageState() {
        return Optional.ofNullable(packageState);
    }

    public void setPackageState(OVALPackageState packageState) {
        this.packageState = packageState;
    }

    @Column(name = "isRpm")
    public boolean isRpm() {
        return isRpm;
    }

    public void setRpm(boolean rpm) {
        isRpm = rpm;
    }


}
