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
public class PVCParamAttribute implements Serializable {

	private static final long serialVersionUID = -868473986215961832L;

    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	/** identifier field */
    private Integer pvcparamattributeid;

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
	@JoinColumn(name = "parametervaluechangedid")
    /** persistent field */
    private context.arch.logging.hibernate.ParameterValueChanged ParameterValueChanged;

    /** full constructor */
    public PVCParamAttribute(String attributename, String attributetype, String attributevaluestring, Float attributevaluenumeric, context.arch.logging.hibernate.ParameterValueChanged ParameterValueChanged) {
        this.attributename = attributename;
        this.attributetype = attributetype;
        this.attributevaluestring = attributevaluestring;
        this.attributevaluenumeric = attributevaluenumeric;
        this.ParameterValueChanged = ParameterValueChanged;
    }

    /** default constructor */
    public PVCParamAttribute() {
    }

    /** minimal constructor */
    public PVCParamAttribute(context.arch.logging.hibernate.ParameterValueChanged ParameterValueChanged) {
        this.ParameterValueChanged = ParameterValueChanged;
    }

    public Integer getPvcparamattributeid() {
        return this.pvcparamattributeid;
    }

    public void setPvcparamattributeid(Integer pvcparamattributeid) {
        this.pvcparamattributeid = pvcparamattributeid;
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

    public context.arch.logging.hibernate.ParameterValueChanged getParameterValueChanged() {
        return this.ParameterValueChanged;
    }

    public void setParameterValueChanged(context.arch.logging.hibernate.ParameterValueChanged ParameterValueChanged) {
        this.ParameterValueChanged = ParameterValueChanged;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("pvcparamattributeid", getPvcparamattributeid())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof PVCParamAttribute) ) return false;
        PVCParamAttribute castOther = (PVCParamAttribute) other;
        return new EqualsBuilder()
            .append(this.getPvcparamattributeid(), castOther.getPvcparamattributeid())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getPvcparamattributeid())
            .toHashCode();
    }

}
