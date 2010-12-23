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
public class SEInputAttribute implements Serializable {

	private static final long serialVersionUID = 9192795573084433854L;

    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	/** identifier field */
    private Integer seinputattributeid;

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
	@JoinColumn(name = "serviceexecutionid")
    /** persistent field */
    private context.arch.logging.hibernate.ServiceExecution ServiceExecution;

    /** full constructor */
    public SEInputAttribute(String attributename, String attributetype, String attributevaluestring, Float attributevaluenumeric, context.arch.logging.hibernate.ServiceExecution ServiceExecution) {
        this.attributename = attributename;
        this.attributetype = attributetype;
        this.attributevaluestring = attributevaluestring;
        this.attributevaluenumeric = attributevaluenumeric;
        this.ServiceExecution = ServiceExecution;
    }

    /** default constructor */
    public SEInputAttribute() {
    }

    /** minimal constructor */
    public SEInputAttribute(context.arch.logging.hibernate.ServiceExecution ServiceExecution) {
        this.ServiceExecution = ServiceExecution;
    }

    public Integer getSeinputattributeid() {
        return this.seinputattributeid;
    }

    public void setSeinputattributeid(Integer seinputattributeid) {
        this.seinputattributeid = seinputattributeid;
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

    public context.arch.logging.hibernate.ServiceExecution getServiceExecution() {
        return this.ServiceExecution;
    }

    public void setServiceExecution(context.arch.logging.hibernate.ServiceExecution ServiceExecution) {
        this.ServiceExecution = ServiceExecution;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("seinputattributeid", getSeinputattributeid())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof SEInputAttribute) ) return false;
        SEInputAttribute castOther = (SEInputAttribute) other;
        return new EqualsBuilder()
            .append(this.getSeinputattributeid(), castOther.getSeinputattributeid())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getSeinputattributeid())
            .toHashCode();
    }

}
