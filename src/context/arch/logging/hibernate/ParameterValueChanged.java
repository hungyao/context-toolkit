package context.arch.logging.hibernate;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
@Entity
public class ParameterValueChanged implements Serializable {

	private static final long serialVersionUID = 8499982432143940691L;

    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	/** identifier field */
    private Integer parametervaluechangedid;

    @Column(nullable = true)
    /** nullable persistent field */
    private String parametervaluestring;

    @Column(nullable = true)
    /** nullable persistent field */
    private Float parametervaluenumeric;

    /** persistent field */
    private Date parametervaluechangedtime;

    @ManyToOne
	@JoinColumn(name = "erparameterid")
    /** persistent field */
    private context.arch.logging.hibernate.ERParameter ERParameter;

    @ManyToOne
	@JoinColumn(name = "enactorregistrationid")
    /** persistent field */
    private context.arch.logging.hibernate.EnactorRegistration EnactorRegistration;

	@OneToMany(fetch=FetchType.LAZY)
    /** persistent field */
    private Set<PVCParamAttribute> PVCParamAttributes;

    /** full constructor */
    public ParameterValueChanged(String parametervaluestring, Float parametervaluenumeric, Date parametervaluechangedtime, context.arch.logging.hibernate.ERParameter ERParameter, context.arch.logging.hibernate.EnactorRegistration EnactorRegistration, Set<PVCParamAttribute> PVCParamAttributes) {
        this.parametervaluestring = parametervaluestring;
        this.parametervaluenumeric = parametervaluenumeric;
        this.parametervaluechangedtime = parametervaluechangedtime;
        this.ERParameter = ERParameter;
        this.EnactorRegistration = EnactorRegistration;
        this.PVCParamAttributes = PVCParamAttributes;
    }

    /** default constructor */
    public ParameterValueChanged() {
    }

    /** minimal constructor */
    public ParameterValueChanged(Date parametervaluechangedtime, context.arch.logging.hibernate.ERParameter ERParameter, context.arch.logging.hibernate.EnactorRegistration EnactorRegistration, Set<PVCParamAttribute> PVCParamAttributes) {
        this.parametervaluechangedtime = parametervaluechangedtime;
        this.ERParameter = ERParameter;
        this.EnactorRegistration = EnactorRegistration;
        this.PVCParamAttributes = PVCParamAttributes;
    }

    public Integer getParametervaluechangedid() {
        return this.parametervaluechangedid;
    }

    public void setParametervaluechangedid(Integer parametervaluechangedid) {
        this.parametervaluechangedid = parametervaluechangedid;
    }

    public String getParametervaluestring() {
        return this.parametervaluestring;
    }

    public void setParametervaluestring(String parametervaluestring) {
        this.parametervaluestring = parametervaluestring;
    }

    public Float getParametervaluenumeric() {
        return this.parametervaluenumeric;
    }

    public void setParametervaluenumeric(Float parametervaluenumeric) {
        this.parametervaluenumeric = parametervaluenumeric;
    }

    public Date getParametervaluechangedtime() {
        return this.parametervaluechangedtime;
    }

    public void setParametervaluechangedtime(Date parametervaluechangedtime) {
        this.parametervaluechangedtime = parametervaluechangedtime;
    }

    public context.arch.logging.hibernate.ERParameter getERParameter() {
        return this.ERParameter;
    }

    public void setERParameter(context.arch.logging.hibernate.ERParameter ERParameter) {
        this.ERParameter = ERParameter;
    }

    public context.arch.logging.hibernate.EnactorRegistration getEnactorRegistration() {
        return this.EnactorRegistration;
    }

    public void setEnactorRegistration(context.arch.logging.hibernate.EnactorRegistration EnactorRegistration) {
        this.EnactorRegistration = EnactorRegistration;
    }

    public Set<PVCParamAttribute> getPVCParamAttributes() {
        return this.PVCParamAttributes;
    }

    public void setPVCParamAttributes(Set<PVCParamAttribute> PVCParamAttributes) {
        this.PVCParamAttributes = PVCParamAttributes;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("parametervaluechangedid", getParametervaluechangedid())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof ParameterValueChanged) ) return false;
        ParameterValueChanged castOther = (ParameterValueChanged) other;
        return new EqualsBuilder()
            .append(this.getParametervaluechangedid(), castOther.getParametervaluechangedid())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getParametervaluechangedid())
            .toHashCode();
    }

}
