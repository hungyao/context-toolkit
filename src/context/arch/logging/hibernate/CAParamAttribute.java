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
public class CAParamAttribute implements Serializable {

	private static final long serialVersionUID = -1684893642435551351L;

    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	/** identifier field */
    private Integer caparamattributeid;

    @Column(nullable = true)
    /** nullable persistent field */
    private String attributename;

    @Column(nullable = true)
    /** nullable persistent field */
    private String attributetype;

    @Column(nullable = true)
    /** nullable persistent field */
    private String attributevaluestring;

    @Column(nullable = true)
    /** nullable persistent field */
    private Float attributevaluenumeric;

    @ManyToOne
	@JoinColumn(name = "componentaddedid")
    /** persistent field */
    private context.arch.logging.hibernate.ComponentAdded ComponentAdded;

    /** full constructor */
    public CAParamAttribute(String attributename, String attributetype, String attributevaluestring, Float attributevaluenumeric, context.arch.logging.hibernate.ComponentAdded ComponentAdded) {
        this.attributename = attributename;
        this.attributetype = attributetype;
        this.attributevaluestring = attributevaluestring;
        this.attributevaluenumeric = attributevaluenumeric;
        this.ComponentAdded = ComponentAdded;
    }

    /** default constructor */
    public CAParamAttribute() {
    }

    /** minimal constructor */
    public CAParamAttribute(context.arch.logging.hibernate.ComponentAdded ComponentAdded) {
        this.ComponentAdded = ComponentAdded;
    }

    public Integer getCaparamattributeid() {
        return this.caparamattributeid;
    }

    public void setCaparamattributeid(Integer caparamattributeid) {
        this.caparamattributeid = caparamattributeid;
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

    public context.arch.logging.hibernate.ComponentAdded getComponentAdded() {
        return this.ComponentAdded;
    }

    public void setComponentAdded(context.arch.logging.hibernate.ComponentAdded ComponentAdded) {
        this.ComponentAdded = ComponentAdded;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("caparamattributeid", getCaparamattributeid())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof CAParamAttribute) ) return false;
        CAParamAttribute castOther = (CAParamAttribute) other;
        return new EqualsBuilder()
            .append(this.getCaparamattributeid(), castOther.getCaparamattributeid())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getCaparamattributeid())
            .toHashCode();
    }

}
