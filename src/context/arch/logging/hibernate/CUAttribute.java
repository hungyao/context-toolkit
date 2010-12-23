package context.arch.logging.hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
@Entity
public class CUAttribute implements Serializable {

	private static final long serialVersionUID = -7904836815008736250L;

    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	/** identifier field */
    private Integer cuattributeid;

    /** persistent field */
    private String attributename;

    /** persistent field */
    private String attributetype;

    @Column(nullable = true)
    /** nullable persistent field */
    private String attributevaluestring;

    @Column(nullable = true)
    /** nullable persistent field */
    private Float attributevaluenumeric;

    /** persistent field */
    private boolean constant;

    @ManyToOne
	@JoinColumn(name = "cudestinationid")
    /** persistent field */
    private context.arch.logging.hibernate.CUDestination CUDestination;

    /** full constructor */
    public CUAttribute(String attributename, String attributetype, String attributevaluestring, Float attributevaluenumeric, boolean constant, context.arch.logging.hibernate.CUDestination CUDestination) {
        this.attributename = attributename;
        this.attributetype = attributetype;
        this.attributevaluestring = attributevaluestring;
        this.attributevaluenumeric = attributevaluenumeric;
        this.constant = constant;
        this.CUDestination = CUDestination;
    }

    /** default constructor */
    public CUAttribute() {
    }

    /** minimal constructor */
    public CUAttribute(String attributename, String attributetype, boolean constant, context.arch.logging.hibernate.CUDestination CUDestination) {
        this.attributename = attributename;
        this.attributetype = attributetype;
        this.constant = constant;
        this.CUDestination = CUDestination;
    }

    public Integer getCuattributeid() {
        return this.cuattributeid;
    }

    public void setCuattributeid(Integer cuattributeid) {
        this.cuattributeid = cuattributeid;
    }

    public String getAttributename() {
        return this.attributename;
    }

    public void setAttributename(String attributename) {
        this.attributename = attributename;
    }

    public String getAttributetype() {
        return this.attributetype;
    }

    public void setAttributetype(Class<?> attributetype) {
        this.attributetype = attributetype.getName();
    }

    public String getAttributevaluestring() {
        return this.attributevaluestring;
    }

    public void setAttributevaluestring(String attributevaluestring) {
        this.attributevaluestring = attributevaluestring;
    }

    public Float getAttributevaluenumeric() {
        return this.attributevaluenumeric;
    }

    public void setAttributevaluenumeric(Float attributevaluenumeric) {
        this.attributevaluenumeric = attributevaluenumeric;
    }

    public boolean isConstant() {
        return this.constant;
    }

    public void setConstant(boolean constant) {
        this.constant = constant;
    }

    public context.arch.logging.hibernate.CUDestination getCUDestination() {
        return this.CUDestination;
    }

    public void setCUDestination(context.arch.logging.hibernate.CUDestination CUDestination) {
        this.CUDestination = CUDestination;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("cuattributeid", getCuattributeid())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof CUAttribute) ) return false;
        CUAttribute castOther = (CUAttribute) other;
        return new EqualsBuilder()
            .append(this.getCuattributeid(), castOther.getCuattributeid())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getCuattributeid())
            .toHashCode();
    }

}
